package com.example.rapidosm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class AlertCenter extends AppCompatActivity {

    private ListView listView;
    private ArrayList<Store> storeList;
    private StoreAdapter storeAdapter;

    private String selectedSupplyType = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_center);

        listView = findViewById(R.id.listView);
        storeList = new ArrayList<>();
        storeAdapter = new StoreAdapter(this, storeList);
        listView.setAdapter(storeAdapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_alertcenter);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (itemId == R.id.nav_registerstore) {
                startActivity(new Intent(this, RegisterStore.class));
                return true;
            } else if (itemId == R.id.nav_locator) {
                startActivity(new Intent(this, LocatorMap.class));
                return true;
            } else if (itemId == R.id.nav_alertcenter) {
                return true;
            } else if (itemId == R.id.nav_about) {
                startActivity(new Intent(this, About.class));
                return true;
            }
            return false;
        });

        // Set up image button click listeners
        setupImageListeners();

        // Fetch store data
        fetchStoreData();
    }

    private void setupImageListeners() {
        ImageView imageAll = findViewById(R.id.imageAll);
        ImageView imageWater = findViewById(R.id.imageWater);
        ImageView imageGas = findViewById(R.id.imageGas);
        ImageView imageMedical = findViewById(R.id.imageMedical);
        ImageView imageFood = findViewById(R.id.imageFood);

        View.OnClickListener clickListener = v -> {
            // Trigger animation
            animateButton(v);

            // Update selectedSupplyType based on the button clicked
            if (v.getId() == R.id.imageAll) {
                selectedSupplyType = "All";
            } else if (v.getId() == R.id.imageWater) {
                selectedSupplyType = "Water";
            } else if (v.getId() == R.id.imageGas) {
                selectedSupplyType = "Gas";
            } else if (v.getId() == R.id.imageMedical) {
                selectedSupplyType = "Medicine";
            } else if (v.getId() == R.id.imageFood) {
                selectedSupplyType = "Food";
            }

            // Fetch data
            fetchStoreData();
        };

        // Set the same click listener for all buttons
        imageAll.setOnClickListener(clickListener);
        imageWater.setOnClickListener(clickListener);
        imageGas.setOnClickListener(clickListener);
        imageMedical.setOnClickListener(clickListener);
        imageFood.setOnClickListener(clickListener);
    }

    // Method to apply a scale animation
    private void animateButton(View view) {
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(150) // Animation duration for scale up
                .withEndAction(() -> {
                    // Scale back to normal size
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150) // Animation duration for scale back
                            .start();
                })
                .start();
    }


    // Fetch store data and apply filtering based on the selected supply type
    private void fetchStoreData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference storesRef = database.getReference("stores");

        storesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                storeList.clear(); // Clear existing list

                for (DataSnapshot storeSnapshot : dataSnapshot.getChildren()) {
                    Store store = storeSnapshot.getValue(Store.class);

                    if (store != null) {
                        boolean matchesSupplyType = selectedSupplyType.equals("All") || store.getSupplyType().equalsIgnoreCase(selectedSupplyType);
                        boolean matchesStatus = store.getSupplyStatus().equals("Limited") || store.getSupplyStatus().equals("Out of Stock");

                        if (matchesSupplyType && matchesStatus) {
                            storeList.add(store);
                        }
                    }
                }

                storeAdapter.notifyDataSetChanged(); // Notify adapter of changes
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AlertCenter", "Failed to read stores data: " + databaseError.getMessage());
            }
        });
    }
}
