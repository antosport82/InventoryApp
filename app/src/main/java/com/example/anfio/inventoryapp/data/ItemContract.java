package com.example.anfio.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by anfio on 05/07/2017.
 */

public class ItemContract {

    private ItemContract() {
    }

    // Set constant CONTENT_AUTHORITY
    public static final String CONTENT_AUTHORITY = "com.example.anfio.inventoryapp";

    // Set the base of all URI's which the app will use to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible path (appended to base content URI for possible URI's)
    public static final String PATH_ITEMS = "items";

    public static abstract class ItemEntry implements BaseColumns {

        // Set the content URI to access the item data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        // The MIME type of the CONTENT_URI for a list of items
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        // The MIME type of the CONTENT_URI for a single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        public static final String TABLE_NAME = "items";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_DESCRIPTION = "description";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_NAME_SUPPLIER = "name_supplier";
        public static final String COLUMN_ITEM_EMAIL_SUPPLIER = "email_supplier";
        public static final String COLUMN_ITEM_PICTURE = "picture";
    }
}