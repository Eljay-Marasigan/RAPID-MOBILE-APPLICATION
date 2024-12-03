package com.example.rapidosm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;

import java.io.IOException;
import java.util.List;

public class Dashboard extends AppCompatActivity {

    private MapView mapView;
    private IMapController mapController;
    private DatabaseReference databaseReference;
    private String currentFilter = "all"; // Default to show all markers

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("stores");

        // Initialize OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        // Initialize the MapView
        mapView = findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);  // OpenStreetMap tiles
        mapView.setBuiltInZoomControls(true);  // Disable default zoom controls
        mapView.setMultiTouchControls(true);    // Enable multi-touch

        // Initialize the IMapController to control zoom and map center
        mapController = mapView.getController();
        mapController.setZoom(10);  // Set the initial zoom level
        GeoPoint startPoint = new GeoPoint(13.786019, 121.0739506); // Starting coordinates
        mapController.setCenter(startPoint);

        // Load all registered stores and display them on the map
        loadRegisteredStores();

        // Set up the Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                return true; // Already in Dashboard, no action needed
            } else if (itemId == R.id.nav_registerstore) {
                startActivity(new Intent(this, RegisterStore.class));
                return true;
            } else if (itemId == R.id.nav_locator) {
                startActivity(new Intent(this, LocatorMap.class));
                return true;
            } else if (itemId == R.id.nav_alertcenter) {
                startActivity(new Intent(this, AlertCenter.class));
                return true;
            } else if (itemId == R.id.nav_about) {
                startActivity(new Intent(this, About.class));
                return true;
            }
            return false;
        });


        // Filter Buttons
        ImageButton button1 = findViewById(R.id.button1); // Water button
        ImageButton button2 = findViewById(R.id.button2); // Gas button
        ImageButton button3 = findViewById(R.id.button3); // Medical button
        ImageButton button4 = findViewById(R.id.button4); // Food button

        // Set listeners for filtering
        button1.setOnClickListener(v -> {
            currentFilter = "water";
            loadRegisteredStores(); // Reload stores based on the selected filter
        });
        button2.setOnClickListener(v -> {
            currentFilter = "gas";
            loadRegisteredStores();
        });
        button3.setOnClickListener(v -> {
            currentFilter = "medicine";
            loadRegisteredStores();
        });
        button4.setOnClickListener(v -> {
            currentFilter = "food";
            loadRegisteredStores();
        });
    }

    private void loadRegisteredStores() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mapView.getOverlays().clear(); // Clear previous markers

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Store store = snapshot.getValue(Store.class);
                    if (store != null && store.getLocation() != null && !store.getLocation().isEmpty()) {
                        Log.d("Dashboard", "Store address: " + store.getLocation());

                        // Only show markers that match the selected filter
                        if (!currentFilter.equals("all") && !store.getSupplyType().equalsIgnoreCase(currentFilter)) {
                            continue;
                        }

                        // Use Geocoder to get latitude and longitude from address
                        Geocoder geocoder = new Geocoder(Dashboard.this);
                        try {
                            List<Address> addresses = geocoder.getFromLocationName(store.getLocation(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                double latitude = address.getLatitude();
                                double longitude = address.getLongitude();

                                // Log the parsed latitude and longitude
                                Log.d("Dashboard", "Parsed Coordinates: " + latitude + ", " + longitude);

                                // Create a GeoPoint for the store location
                                GeoPoint storeLocation = new GeoPoint(latitude, longitude);

                                // Create a new marker for this store
                                Marker marker = new Marker(mapView);
                                marker.setPosition(storeLocation);
                                marker.setTitle(store.getStoreName());

                                // Set the marker icon based on supply type
                                setMarkerColorBasedOnSupplyType(marker, store.getSupplyType());

                                marker.setOnMarkerClickListener((m, map) -> {
                                    showStoreDetailsDialog(store);
                                    return true;
                                });


                                mapView.getOverlays().add(marker);

                            } else {
                                Log.w("Dashboard", "Unable to geocode address for store: " + store.getStoreName());
                            }
                        } catch (IOException e) {
                            Log.e("Dashboard", "Geocoding failed for address: " + store.getLocation(), e);
                        }
                    } else {
                        Log.w("Dashboard", "Store location is missing or invalid.");
                    }
                }

                // After adding all markers, adjust the zoom level to fit all markers
                mapView.zoomToBoundingBox(mapView.getBoundingBox(), true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Dashboard.this, "Failed to load registered stores", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setMarkerColorBasedOnSupplyType(Marker marker, String supplyType) {
        // Set marker icon based on the supply type
        switch (supplyType.toLowerCase()) {
            case "food":
                marker.setIcon(resizeDrawable(getResources().getDrawable(R.drawable.food_icon), 50, 50));  // Resize to 50x50
                break;
            case "water":
                marker.setIcon(resizeDrawable(getResources().getDrawable(R.drawable.water_icon), 50, 50));  // Resize to 50x50
                break;
            case "medicine":
                marker.setIcon(resizeDrawable(getResources().getDrawable(R.drawable.medic_icon), 50, 50));  // Resize to 50x50
                break;
            case "gas":
                marker.setIcon(resizeDrawable(getResources().getDrawable(R.drawable.gas_icon), 50, 50));  // Resize to 50x50
                break;
            default:
                marker.setIcon(resizeDrawable(getResources().getDrawable(R.drawable.black), 50, 50));  // Resize to 50x50
                break;
        }
    }

    // Helper method to resize the drawable icon
    private Drawable resizeDrawable(Drawable drawable, int width, int height) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return new BitmapDrawable(getResources(), resizedBitmap);
    }

    private void showStoreDetailsDialog(Store store) {
        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_store_details, null);

        // Bind views from the custom layout
        TextView storeName = dialogView.findViewById(R.id.store_name);
        TextView storeSupplyType = dialogView.findViewById(R.id.store_supply_type);
        TextView storeStatus = dialogView.findViewById(R.id.store_status);
        TextView storeLocation = dialogView.findViewById(R.id.store_location);
        Button closeButton = dialogView.findViewById(R.id.btn_close); // Access the Close button

        // Set the data
        storeName.setText("Name: " + store.getStoreName());
        storeSupplyType.setText("Supply Type: " + store.getSupplyType());
        storeStatus.setText("Status: " + store.getSupplyStatus());
        storeLocation.setText("Location: " + store.getLocation());

        // Build the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        // Set close button functionality
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }



}
