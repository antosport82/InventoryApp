package com.example.anfio.inventoryapp;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.anfio.inventoryapp.data.ItemContract.ItemEntry;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.anfio.inventoryapp.data.StoreProvider.LOG_TAG;

/**
 * Created by anfio on 05/07/2017.
 */

public class EditorActivity extends AppCompatActivity {

    private ImageView mImageView;
    private String mImageUriString;
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierEmailEditText;
    private static final int SELECT_PHOTO = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        getIntent();

        // Find all relevant views that we will need to read user input from
        mImageView = (ImageView) findViewById(R.id.item_picture);
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_item_description);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_item_supplier_name);
        mSupplierEmailEditText = (EditText) findViewById(R.id.edit_item_supplier_email);

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(v);
            }
        });

    }

    public void chooseImage(View v) {
        openGallery();
    }

    private void openGallery() {
        Intent photoPickerIntent;

        if (Build.VERSION.SDK_INT < 19) {
            photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            photoPickerIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, SELECT_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri mUriSelectedImage;
                    mUriSelectedImage = data.getData();
                    if (mUriSelectedImage != null) {
                        mImageUriString = mUriSelectedImage.toString();
                        mImageView.setImageBitmap(getBitmapFromUri(mUriSelectedImage));
                    }
                }
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        // Get the dimensions of the View
        int targetWidth = mImageView.getWidth();
        int targetHeight = mImageView.getHeight();

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

    private boolean saveItem() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String pictureString = mImageUriString;
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierEmailString = mSupplierEmailEditText.getText().toString().trim();

        // Check
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(descriptionString) || TextUtils.isEmpty(quantityString) ||
                TextUtils.isEmpty(priceString) || TextUtils.isEmpty(supplierNameString) ||
                TextUtils.isEmpty(supplierEmailString) || TextUtils.isEmpty(pictureString)) {
            return false;
        }

        // Create a ContentValues object where column names are the keys,
        // and item attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_DESCRIPTION, descriptionString);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(ItemEntry.COLUMN_ITEM_NAME_SUPPLIER, supplierNameString);
        values.put(ItemEntry.COLUMN_ITEM_EMAIL_SUPPLIER, supplierEmailString);
        values.put(ItemEntry.COLUMN_ITEM_PICTURE, pictureString);

        // Insert a new item into the provider, returning the content URI for the new item.
        Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, getString(R.string.insert_item_failed),
                    Toast.LENGTH_SHORT).show();
            return false;
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.insert_item_successful),
                    Toast.LENGTH_SHORT).show();
            return true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                if (saveItem()) {
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
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}