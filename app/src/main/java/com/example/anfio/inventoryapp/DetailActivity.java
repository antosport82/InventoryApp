package com.example.anfio.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anfio.inventoryapp.data.ItemContract.ItemEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Locale;

import static com.example.anfio.inventoryapp.data.StoreProvider.LOG_TAG;

/**
 * Created by anfio on 13/07/2017.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, ViewTreeObserver.OnPreDrawListener {

    private static final int DETAIL_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;
    private ImageView mPictureImageView;
    private TextView mNameTextView;
    private TextView mDescriptionTextView;
    private TextView mPriceTextView;
    private TextView mQuantityTextView;
    private TextView mNameSupplierTextView;
    private TextView mEmailSupplierTextView;
    private EditText mVariation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        getLoaderManager().initLoader(DETAIL_ITEM_LOADER, null, this);

        mPictureImageView = (ImageView) findViewById(R.id.detail_picture);
        mNameTextView = (TextView) findViewById(R.id.detail_name);
        mDescriptionTextView = (TextView) findViewById(R.id.detail_description);
        mPriceTextView = (TextView) findViewById(R.id.detail_price);
        mQuantityTextView = (TextView) findViewById(R.id.detail_quantity);
        mNameSupplierTextView = (TextView) findViewById(R.id.detail_supplier_name);
        mEmailSupplierTextView = (TextView) findViewById(R.id.detail_supplier_email);
        Button mMinusButton = (Button) findViewById(R.id.minus_button);
        Button mPlusButton = (Button) findViewById(R.id.plus_button);
        mVariation = (EditText) findViewById(R.id.variation);
        Button mOrderMore = (Button) findViewById(R.id.order_more);
        Button mDeleteItem = (Button) findViewById(R.id.delete_item);

        mPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mQuantity = mQuantityTextView.getText().toString();
                int quantity = Integer.parseInt(mQuantity);
                if (mVariation.getText().toString().trim().length() == 0) {
                    quantity++;
                } else {
                    String stringVariation = mVariation.getText().toString();
                    int variation = Integer.parseInt(stringVariation);
                    quantity = quantity + variation;
                }

                mQuantityTextView.setText(String.format(Locale.US, "%d", quantity));
            }
        });

        mMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mQuantity = mQuantityTextView.getText().toString();
                int quantity = Integer.parseInt(mQuantity);
                if (mVariation.getText().toString().trim().length() == 0) {
                    if (quantity > 0) {
                        quantity--;
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.wrong_quantity, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String stringVariation = mVariation.getText().toString();
                    int variation = Integer.parseInt(stringVariation);
                    if (quantity > variation) {
                        quantity = quantity - variation;
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.wrong_quantity, Toast.LENGTH_SHORT).show();
                    }
                }
                mQuantityTextView.setText(String.format(Locale.US, "%d", quantity));
            }
        });

        mOrderMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mName = mNameTextView.getText().toString();
                String mNameSupplier = mNameSupplierTextView.getText().toString();
                String mEmailSupplier = mEmailSupplierTextView.getText().toString();
                String message = createOrder(mName, mNameSupplier);
                sendOrder(message, mEmailSupplier);
            }
        });

        mDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    private String createOrder(String name, String supplierName) {
        String message = getResources().getString(R.string.order_text_one) + supplierName + ", \n";
        message += getResources().getString(R.string.order_text_two) + name + ".\n";
        message += getResources().getString(R.string.order_text_three);
        return message;
    }

    private void sendOrder(String message, String supplierEmail) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + supplierEmail));
        intent.putExtra(intent.EXTRA_SUBJECT, getResources().getString(R.string.new_order));
        intent.putExtra(intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
        // Show a toast message depending on whether or not the delete was successful.
        if (rowsDeleted == 0) {
            // If no rows were deleted, then there was an error with the delete.
            Toast.makeText(this, getString(R.string.delete_item_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.delete_item_successful),
                    Toast.LENGTH_SHORT).show();
        }
        // Close the activity
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                if (updateQuantity()) {
                    // Exit activity
                    finish();
                    return true;
                } else {
                    Toast.makeText(this, getString(R.string.please_fill_in),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.

                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean updateQuantity() {
        ContentValues values = new ContentValues();
        String quantity = mQuantityTextView.getText().toString();
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        getContentResolver().update(mCurrentItemUri, values, null, null);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ItemEntry.COLUMN_ITEM_PICTURE,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_DESCRIPTION,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_NAME_SUPPLIER,
                ItemEntry.COLUMN_ITEM_EMAIL_SUPPLIER};

        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()) {
            int pictureColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PICTURE);
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_DESCRIPTION);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int nameSupplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME_SUPPLIER);
            int emailSupplierColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_EMAIL_SUPPLIER);

            String picture = cursor.getString(pictureColumnIndex);
            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            Float price = cursor.getFloat(priceColumnIndex);
            DecimalFormat df = new DecimalFormat("0.00");
            df.setMaximumFractionDigits(2);
            String priceAsString = df.format(price);
            priceAsString = priceAsString.replace(",", ".");
            String quantity = cursor.getString(quantityColumnIndex);
            String nameSupplier = cursor.getString(nameSupplierColumnIndex);
            String emailSupplier = cursor.getString(emailSupplierColumnIndex);

            final Uri mPictureUri = Uri.parse(picture);
            mNameTextView.setText(name);
            mDescriptionTextView.setText(description);
            mPriceTextView.setText(priceAsString);
            mQuantityTextView.setText(quantity);
            mNameSupplierTextView.setText(nameSupplier);
            mEmailSupplierTextView.setText(emailSupplier);

            mPictureImageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    try {
                        mPictureImageView.setImageBitmap(getBitmapFromUri(mPictureUri));
                        return true;
                    } finally {
                        mPictureImageView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                }
            });
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        // Get the dimensions of the View
        int targetWidth = mPictureImageView.getWidth();
        int targetHeight = mPictureImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoWidth = bmOptions.outWidth;
            int photoHeight = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoWidth / targetWidth, photoHeight / targetHeight);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameTextView.setText("");
        mDescriptionTextView.setText("");
        mPriceTextView.setText("");
        mQuantityTextView.setText("");
        mNameSupplierTextView.setText("");
        mEmailSupplierTextView.setText("");
    }

    @Override
    public boolean onPreDraw() {
        return false;
    }
}