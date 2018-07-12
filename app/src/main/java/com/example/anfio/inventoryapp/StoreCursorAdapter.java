package com.example.anfio.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anfio.inventoryapp.data.ItemContract.ItemEntry;

import java.text.DecimalFormat;

import static com.example.anfio.inventoryapp.R.id.price;

/**
 * Created by anfio on 05/07/2017.
 */

/**
 * StoreCursorAdapter is an adapter for a listview
 * that uses a Cursor of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the Cursor
 */
public class StoreCursorAdapter extends CursorAdapter {

    private final Context context;

    public StoreCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        this.context = context;
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView priceView = (TextView) view.findViewById(price);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        Button quantityUpdater = (Button) view.findViewById(R.id.sale_button);

        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

        final int itemId = cursor.getInt(idColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        float itemPrice = cursor.getFloat(priceColumnIndex);
        DecimalFormat df = new DecimalFormat("0.00");
        df.setMaximumFractionDigits(2);
        String priceAsString = df.format(itemPrice);
        priceAsString = priceAsString.replace(",", ".");
        final int itemQuantity = cursor.getInt(quantityColumnIndex);
        String quantityAsString = Integer.toString(itemQuantity);

        quantityUpdater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemQuantity > 0) {
                    updateQuantity(itemId, itemQuantity);
                } else {
                    Toast.makeText(context, R.string.zero_quantity, Toast.LENGTH_SHORT).show();
                }
            }
        });

        nameView.setText(itemName);
        priceView.setText(priceAsString);
        quantityView.setText(quantityAsString);
    }

    private void updateQuantity(int id, int quantity) {
        quantity--;
        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        Uri contentUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, id);
        context.getContentResolver().update(contentUri, values, null, null);
    }
}