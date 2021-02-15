package com.example.thedietitianfyp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FoodListAdapter extends CursorAdapter implements Filterable {

    public FoodListAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        notifyDataSetChanged();
    }

    // The newView method is used to inflate a new view and return it,
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.fragment_food_list_item, parent, false);
    }

    // The bindView method is used to bind all data to ListView (foods)
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView textViewListName = (TextView) view.findViewById(R.id.textViewListName);
        TextView textViewListNumber = (TextView) view.findViewById(R.id.textViewListNumber);
        TextView textViewSub = (TextView) view.findViewById(R.id.textViewSub);

        //Extract properties from cursor
        int getID = cursor.getInt(cursor.getColumnIndexOrThrow("_id"));
        String getName = cursor.getString(cursor.getColumnIndexOrThrow("food_name"));
        String getBrandName = cursor.getString(cursor.getColumnIndexOrThrow("food_manufacturer_name"));
        int getCalories = cursor.getInt(cursor.getColumnIndexOrThrow("food_calories")); // by using int we drop decimals
        int getProtein = cursor.getInt(cursor.getColumnIndexOrThrow("food_proteins"));
        int getCarbohydrate = cursor.getInt(cursor.getColumnIndexOrThrow("food_carbohydrates"));
        int getFat = cursor.getInt(cursor.getColumnIndexOrThrow("food_fat"));
        String getQuantity = cursor.getString(cursor.getColumnIndexOrThrow("food_serving_size"));
        String getUnit = cursor.getString(cursor.getColumnIndexOrThrow("food_serving_measurement"));

        if(getUnit == null) {
            getUnit = " n/a";
        }

        if(getQuantity == null) {
            getQuantity = "n/a";
        }

        String subLine = getBrandName + ", " + "P: " + getProtein + ", C: " +
                getCarbohydrate + ", F: " + getFat + ", " + getQuantity + ", " + getUnit;

//        // Populate fields with extracted properties
        textViewListName.setText(getName);
        textViewListNumber.setText(String.valueOf(getCalories));// shows calories for each food
        textViewSub.setText(String.valueOf(subLine));
    }

    @Override
    public FilterQueryProvider getFilterQueryProvider() {
        return super.getFilterQueryProvider();
    }
}




