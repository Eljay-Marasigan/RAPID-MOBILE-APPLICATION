package com.example.rapidosm;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserChat extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Store> storeList;
    private StoreAdapter storeAdapter;
    private String UserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        listView = findViewById(R.id.listView);
        storeList = new ArrayList<>();
        storeAdapter = new StoreAdapter(this, storeList);
        listView.setAdapter(storeAdapter);

        // Get the current user ID from Intent or SharedPreferences
        UserId = getIntent().getStringExtra("user_id"); // or use SharedPreferences

        // Fetch store data excluding the user's own store
        fetchStoreData();
    }

    // Fetch store data and display all registered stores, excluding the current user's own store
    private void fetchStoreData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference storesRef = database.getReference("stores");

        storesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeList.clear(); // Clear existing list

                for (DataSnapshot storeSnapshot : dataSnapshot.getChildren()) {
                    Store store = storeSnapshot.getValue(Store.class);

                    if (store != null && !store.getUserId().equals(UserId)) {
                        storeList.add(store); // Add store if it's not owned by the current user
                    }
                }

                storeAdapter.notifyDataSetChanged(); // Notify adapter of changes
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UserChat", "Failed to read stores data: " + databaseError.getMessage());
            }
        });
    }
}
