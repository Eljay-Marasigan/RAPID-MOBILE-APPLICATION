package com.example.rapidosm;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class StoreAdapter extends BaseAdapter {

    private Activity context;
    private ArrayList<Store> storeList;

    public StoreAdapter(Activity context, ArrayList<Store> storeList) {
        this.context = context;
        this.storeList = storeList;
    }

    @Override
    public int getCount() {
        return storeList.size();
    }

    @Override
    public Object getItem(int position) {
        return storeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the item view if not already inflated
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.store_item, null);
        }

        // Get current store
        Store store = storeList.get(position);

        // Find the views and set data
        TextView storeName = convertView.findViewById(R.id.storeName);
        TextView supplyType = convertView.findViewById(R.id.supplyType);
        TextView supplyStatus = convertView.findViewById(R.id.supplyStatus);

        // Set the store's name, supply type, and status
        storeName.setText(store.getStoreName());
        supplyType.setText("Supply Type: " + store.getSupplyType());
        supplyStatus.setText("Status: " + store.getSupplyStatus());

        // Handle item click
        convertView.setOnClickListener(v -> {
            // Pass the store's unique ID (key) to the ChatActivity
            String storeId = store.getStoreName();  // Assuming the store object has a `storeId` field
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("store_id", storeId);  // Use storeId here
            context.startActivity(intent);
        });

        return convertView;
    }
}
