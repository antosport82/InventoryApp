package com.example.anfio.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.anfio.inventoryapp.data.ItemContract.ItemEntry;

/**
 * Created by anfio on 05/07/2017.
 */

public class StoreProvider extends ContentProvider {

    // Tag for the log messages
    public static final String LOG_TAG = StoreProvider.class.getSimpleName();

    // Database helper object
    private StoreDbHelper mDbHelper;

    // URI matcher code for the content URI for the items table

    private static final int ITEMS = 100;

    // URI matcher code for the content URI for a single item in the items table
    private static final int ITEM_ID = 101;

    // UriMatcher object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer
    static {
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS, ITEMS);
        sUriMatcher.addURI(ItemContract.CONTENT_AUTHORITY, ItemContract.PATH_ITEMS + "/#", ITEM_ID);
    }

    // Initialize the provider and the database helper object to gain access to the items database.
    @Override
    public boolean onCreate() {
        /* Set the variable as a global variable, so it can be referenced from other
         ContentProvider methods.*/
        mDbHelper = new StoreDbHelper(getContext());
        return true;
    }

    //  Perform the query for the given URI.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                // For the ITEMS code, query the items table directly with the given
                // projection, selection, selection arguments, and sort order
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // This will perform a query on the items table to return a
                // Cursor containing that row of the table.
                cursor = database.query(ItemEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), ItemEntry.CONTENT_URI);
        return cursor;
    }

    // Insert new data into the provider with the given ContentValues
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Insert an item into the database with the given content values
    private Uri insertItem(Uri uri, ContentValues values) {
        // Validation of data
        String name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("The item requires a name");
        }

        String description = values.getAsString(ItemEntry.COLUMN_ITEM_DESCRIPTION);
        if (description == null) {
            throw new IllegalArgumentException("The item requires a description");
        }

        Float price = values.getAsFloat(ItemEntry.COLUMN_ITEM_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("The item requires a price");
        }

        Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("The item requires a quantity");
        }

        String supplier_name = values.getAsString(ItemEntry.COLUMN_ITEM_NAME_SUPPLIER);
        if (supplier_name == null) {
            throw new IllegalArgumentException("The item requires the supplier name");
        }

        String supplier_email = values.getAsString(ItemEntry.COLUMN_ITEM_EMAIL_SUPPLIER);
        if (supplier_email == null) {
            throw new IllegalArgumentException("The item requires the supplier email");
        }

        String picture = values.getAsString(ItemEntry.COLUMN_ITEM_PICTURE);
        if (picture == null) {
            throw new IllegalArgumentException("The item requires a picture");
        }

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new item with the given values
        long id = database.insert(ItemEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(ItemEntry.CONTENT_URI, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM_ID:
                // For the ITEM_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateItemQuantity(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateItemQuantity(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.containsKey(ItemEntry.COLUMN_ITEM_QUANTITY)) {
            Integer quantity = values.getAsInteger(ItemEntry.COLUMN_ITEM_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Item requires valid quantity");
            }
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsUpdated = database.update(ItemEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Track the number of rows that were deleted
        int rowsDeleted;
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM_ID:
                // Delete a single row given by the ID in the URI
                selection = ItemEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(ItemEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEMS:
                return ItemEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return ItemEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}