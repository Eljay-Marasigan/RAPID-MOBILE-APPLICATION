package com.example.rapidosm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ViewRegisteredStoresActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private LinearLayout registeredStoreListLayout;


    private static final String ADMIN_EMAIL = "admin@example.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_registered_stores);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("stores");

        // Find the layout where the stores will be listed
        registeredStoreListLayout = findViewById(R.id.registered_store_list_layout);

        // Load all registered stores based on user type (admin or regular user)
        loadAllRegisteredStores();
    }

    private void loadAllRegisteredStores() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Get current user's email

        // If the user is an admin, show all stores, otherwise show only their stores
        if (currentUserEmail != null && currentUserEmail.equals(ADMIN_EMAIL)) {
            // Admin can see all stores
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("ViewRegisteredStores", "DataSnapshot size: " + dataSnapshot.getChildrenCount());
                    registeredStoreListLayout.removeAllViews(); // Clear the layout

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Store store = snapshot.getValue(Store.class);
                        String storeId = snapshot.getKey(); // Get the store ID for editing or deleting

                        if (store != null) {
                            createStoreView(store, storeId);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ViewRegisteredStores", "Database error: " + databaseError.getMessage());
                    Toast.makeText(ViewRegisteredStoresActivity.this, "Failed to load registered stores", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Regular user can only see their own stores
            databaseReference.orderByChild("userId").equalTo(currentUserId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            registeredStoreListLayout.removeAllViews(); // Clear the layout

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Store store = snapshot.getValue(Store.class);
                                String storeId = snapshot.getKey(); // Get the store ID for editing or deleting

                                if (store != null) {
                                    createStoreView(store, storeId);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(ViewRegisteredStoresActivity.this, "Failed to load registered stores", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createStoreView(Store store, String storeId) {
        // Inflate the custom layout for the store item
        View storeItemView = getLayoutInflater().inflate(R.layout.store_item_layout, registeredStoreListLayout, false);

        // Get references to the TextViews and buttons
        TextView storeNameTextView = storeItemView.findViewById(R.id.store_name);
        TextView storeInfoTextView = storeItemView.findViewById(R.id.store_info);
        Button editButton = storeItemView.findViewById(R.id.edit_button);
        Button deleteButton = storeItemView.findViewById(R.id.delete_button);

        // Set the store details dynamically
        String storeInfo = "Address: " + store.getLocation() +
                "\nStore Hours: " + store.getStoreHours() +
                "\nSupply Type: " + store.getSupplyType() +
                "\nSupply Status: " + store.getSupplyStatus();

        storeNameTextView.setText(store.getStoreName());
        storeInfoTextView.setText(storeInfo);

        // Set up the Edit button
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(ViewRegisteredStoresActivity.this, EditStoreActivity.class);
            intent.putExtra("storeId", storeId);
            startActivity(intent);
        });

        // Set up the Delete button
        deleteButton.setOnClickListener(v -> deleteStore(storeId));

        // Add the store view to the layout
        registeredStoreListLayout.addView(storeItemView);
    }


    private void deleteStore(String storeId) {
        // Delete the store from Firebase by store ID
        databaseReference.child(storeId).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ViewRegisteredStoresActivity.this, "Store deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ViewRegisteredStoresActivity.this, "Failed to delete store", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
