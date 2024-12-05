package com.example.rapidosm;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.location.Location;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Call;
import okhttp3.Callback;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import android.location.Address;
import android.location.Geocoder;

public class LocatorMap extends AppCompatActivity {

    private MapView mapView;
    private AutoCompleteTextView startingPointInput;
    private Switch switchLocation;
    private Spinner spinnerResources;
    private Button buttonOptimizeRoute, buttonResetRoute;
    private DatabaseReference databaseReference;
    private IMapController mapController;
    private Store nearestStoreDetails;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locator_map);

        // Initialize views
        mapView = findViewById(R.id.mapView);
        startingPointInput = findViewById(R.id.startingPointInput);
        switchLocation = findViewById(R.id.switchLocation);
        spinnerResources = findViewById(R.id.spinnerResources);
        buttonOptimizeRoute = findViewById(R.id.buttonOptimizeRoute);
        buttonResetRoute = findViewById(R.id.buttonResetRoute);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("stores");
        mapView.setTileSource(TileSourceFactory.MAPNIK); // Ensure a tile source is set
        mapView.setBuiltInZoomControls(true); // Enable zoom controls
        mapView.setMultiTouchControls(true); // Enable multitouch

        mapController = mapView.getController();
        mapController.setZoom(17);  // Set the initial zoom level
        GeoPoint initialPoint = new GeoPoint(13.786019, 121.0739506); // Starting coordinates
        mapController.setCenter(initialPoint);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up the spinner adapter
        String[] supplyTypes = new String[]{"Water", "Food", "Gas", "Medicine"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, supplyTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerResources.setAdapter(adapter);

        switchLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Request location updates
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    return;
                }

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                currentLocation = location;

                                // Reverse geocode to get address
                                Geocoder geocoder = new Geocoder(this);
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        String addressName = addresses.get(0).getAddressLine(0); // Full address
                                        startingPointInput.setText(addressName); // Set the address in the input field
                                    } else {
                                        Toast.makeText(this, "Unable to fetch address for the current location", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Toast.makeText(this, "Geocoding error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // Clear the input field when location is turned off
                startingPointInput.setText("");
            }
        });


        // Set up bottom navigation view
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_locator);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (itemId == R.id.nav_registerstore) {
                startActivity(new Intent(this, RegisterStore.class));
                return true;
            } else if (itemId == R.id.nav_locator) {
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

        // Optimize route button
        buttonOptimizeRoute.setOnClickListener(v -> {
            String startingLocation = startingPointInput.getText().toString();
            String selectedSupply = spinnerResources.getSelectedItem().toString();

            if (startingLocation.isEmpty() && !switchLocation.isChecked()) {
                Toast.makeText(LocatorMap.this, "Please enter a starting point or enable GPS", Toast.LENGTH_SHORT).show();
                return;
            }

            GeoPoint startPoint;
            if (switchLocation.isChecked()) {
                // Use current GPS location as the starting point
                if (currentLocation != null) {
                    startPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                    optimizeRoute(startPoint, selectedSupply);
                } else {
                    Toast.makeText(LocatorMap.this, "Unable to fetch GPS location", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Geocode the entered address as the starting point
                startPoint = geocodeAddress(startingLocation);
                if (startPoint != null) {
                    optimizeRoute(startPoint, selectedSupply);
                }
            }
        });


        buttonResetRoute.setOnClickListener(v -> resetRoute());

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            currentLocation = location;
                        }
                    });
        }
    }

    //Reset map
    private void resetRoute() {
        // Clear all map overlays (removes markers and routes)
        mapView.getOverlays().clear();
        mapView.invalidate(); // Refresh the map view

        // Clear the starting point input field
        startingPointInput.setText("");

        // Reset the spinner to the first item (Water)
        spinnerResources.setSelection(0);

        // Clear directions from the TextView
        TextView directionsTextView = findViewById(R.id.directionsTextView);
        directionsTextView.setText(""); // Clear the directions text

        // Optional: Display a toast message confirming reset
        Toast.makeText(this, "Route reset", Toast.LENGTH_SHORT).show();
    }


    // Geocode address to get GeoPoint (latitude, longitude)
    private GeoPoint geocodeAddress(String address) {
        Geocoder geocoder = new Geocoder(LocatorMap.this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                double latitude = addr.getLatitude();
                double longitude = addr.getLongitude();
                return new GeoPoint(latitude, longitude);
            } else {
                Toast.makeText(LocatorMap.this, "Address not found", Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (IOException e) {
            Toast.makeText(LocatorMap.this, "Geocoding error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return null;
        }
    }

    private void optimizeRoute(GeoPoint startPoint, String supplyType) {
        databaseReference.orderByChild("supplyType").equalTo(supplyType).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                double minDistance = Double.MAX_VALUE;
                GeoPoint nearestSupply = null;
                String nearestStore = "";
                final Store[] nearestStoreDetails = new Store[1];  // Use an array to hold the store reference

                // Find the nearest supply store based on distance
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Store store = snapshot.getValue(Store.class);
                    if (store != null && store.getLocation() != null) {
                        // Only consider stores where the supply status is 'Available' or 'Limited'
                        if (store.getSupplyStatus().equals("Available") || store.getSupplyStatus().equals("Limited")) {
                            GeoPoint storeLocation = geocodeAddress(store.getLocation());
                            if (storeLocation != null) {
                                double distance = startPoint.distanceToAsDouble(storeLocation);
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    nearestSupply = storeLocation;
                                    nearestStore = store.getStoreName();

                                    // Update the nearestStoreDetails (store is effectively final here)
                                    nearestStoreDetails[0] = store;  // Store details inside array (effectively final)
                                }
                            }
                        }
                    }
                }

                if (nearestSupply != null) {
                    // Display the starting point on the map
                    Marker startMarker = new Marker(mapView);
                    startMarker.setPosition(startPoint);
                    startMarker.setTitle("Starting Point");
                    startMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker));
                    mapView.getOverlays().add(startMarker);

                    // Display the nearest supply store on the map
                    Marker supplyMarker = new Marker(mapView);
                    supplyMarker.setPosition(nearestSupply);
                    supplyMarker.setTitle("Nearest " + supplyType + " - " + nearestStore);
                    supplyMarker.setIcon(getResources().getDrawable(R.drawable.ic_marker2));
                    mapView.getOverlays().add(supplyMarker);

                    // Set a click listener on the supply marker to show store details
                    supplyMarker.setOnMarkerClickListener((marker, mapView) -> {
                        // Show the store details dialog when the marker is clicked
                        showStoreDetails(nearestStoreDetails[0]);  // Access the store details from array
                        return true; // Indicate that the event was handled
                    });

                    // Set the onClickListener to show the dialog when the marker is clicked
                    startMarker.setOnMarkerClickListener((marker, mapView) -> {
                        showStartingPositionDialog(startPoint);  // Pass the start point to the dialog
                        return true; // Indicate the event is handled
                    });

                    // Fetch route using external service (replace with actual route fetching logic)
                    fetchRoute(startPoint, nearestSupply);

                    // Show a toast message with the optimized route information
                    Toast.makeText(LocatorMap.this, "Optimized route to " + nearestStore, Toast.LENGTH_SHORT).show();
                } else {
                    // Show a toast if no stores were found for the given supply type
                    Toast.makeText(LocatorMap.this, "No available stores found for " + supplyType, Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Show a toast if there is an error fetching data from the database
                Toast.makeText(LocatorMap.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void showStoreDetails(Store store) {
        // Inflate the custom layout for the dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_store_details1, null);

        // Find the TextViews in the dialog layout
        TextView storeNameText = dialogView.findViewById(R.id.store_name);
        TextView supplyTypeText = dialogView.findViewById(R.id.store_supply_type);
        TextView supplyStatusText = dialogView.findViewById(R.id.store_status);
        TextView storeLocationText = dialogView.findViewById(R.id.store_location);
        Button closeButton = dialogView.findViewById(R.id.btn_close);

        // Set dynamic data from the Store object to the TextViews
        storeNameText.setText(store.getStoreName());
        supplyTypeText.setText("Supply Type: " + store.getSupplyType());
        supplyStatusText.setText("Supply Status: " + store.getSupplyStatus());
        storeLocationText.setText("Location: " + store.getLocation());

        // Create the AlertDialog and set the custom view
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView); // Set the custom layout for the dialog
        AlertDialog dialog = builder.create();

        // Set click listener on the close button to dismiss the dialog
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }

    private void showStartingPositionDialog(GeoPoint startPoint) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_starting_position, null);

        // Find the TextViews and button in the dialog layout
        TextView titleTextView = dialogView.findViewById(R.id.starting_position_title);
        TextView positionTextView = dialogView.findViewById(R.id.starting_position_text);
        Button closeButton = dialogView.findViewById(R.id.btn_close);

        // Set the starting position information in the TextViews
        String positionInfo = "Latitude: " + startPoint.getLatitude() + "\nLongitude: " + startPoint.getLongitude();
        positionTextView.setText(positionInfo);

        // Create and show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView); // Set the custom layout for the dialog
        AlertDialog dialog = builder.create();

        // Set the close button to dismiss the dialog
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show the dialog
        dialog.show();
    }



    // Fetch route from GraphHopper
    private void fetchRoute(GeoPoint startPoint, GeoPoint endPoint) {
        OkHttpClient client = new OkHttpClient();
        String url = "https://graphhopper.com/api/1/route?point=" + startPoint.getLatitude() + "," + startPoint.getLongitude()
                + "&point=" + endPoint.getLatitude() + "," + endPoint.getLongitude()
                + "&vehicle=foot&instructions=true&key=2b5b55ec-b6ee-4ab6-88f0-e628665338f3";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LocatorMap.this, "Route fetch error", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d("RouteResponse", responseData);
                    List<GeoPoint> routePoints = parseRouteData(responseData);
                    List<String> directions = parseInstructions(responseData);

                    runOnUiThread(() -> {
                        if (routePoints != null && !routePoints.isEmpty()) {
                            // Add polyline for the route on the map
                            Polyline route = new Polyline();
                            for (GeoPoint point : routePoints) {
                                route.addPoint(point);
                            }
                            route.setWidth(7);
                            route.setColor(0xFFFF0000); // Red color
                            mapView.getOverlays().add(route);
                            mapView.invalidate(); // Refresh map

                            // Display detailed directions
                            showDirections(directions);
                        } else {
                            Toast.makeText(LocatorMap.this, "No route found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    // Parse the route data from the response
    private List<GeoPoint> parseRouteData(String responseData) {
        List<GeoPoint> routePoints = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray paths = jsonObject.getJSONArray("paths");
            if (paths.length() > 0) {
                JSONObject path = paths.getJSONObject(0);
                String pointsEncoded = path.getString("points");

                // Decode polyline points
                routePoints.addAll(decodePolyline(pointsEncoded));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return routePoints;
    }

    // Decode polyline to GeoPoints
    private List<GeoPoint> decodePolyline(String encoded) {
        List<GeoPoint> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int deltaLat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += deltaLat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int deltaLng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += deltaLng;

            GeoPoint point = new GeoPoint((lat / 1E5), (lng / 1E5));
            polyline.add(point);
        }
        return polyline;
    }

    private List<String> parseInstructions(String responseData) {
        List<String> instructions = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            JSONArray paths = jsonObject.getJSONArray("paths");
            if (paths.length() > 0) {
                JSONObject path = paths.getJSONObject(0);
                JSONArray instructionsArray = path.getJSONArray("instructions");

                for (int i = 0; i < instructionsArray.length(); i++) {
                    JSONObject instruction = instructionsArray.getJSONObject(i);
                    String text = instruction.getString("text");
                    int distance = instruction.getInt("distance");
                    instructions.add(text + " (" + distance + " meters)");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instructions;
    }

    private void showDirections(List<String> directions) {
        TextView directionsTextView = findViewById(R.id.directionsTextView);

        if (directions.isEmpty()) {
            directionsTextView.setText("No directions available");
            return;
        }

        StringBuilder directionsText = new StringBuilder();
        for (String step : directions) {
            directionsText.append(step).append("\n");
        }

        // Display directions in the TextView
        directionsTextView.setText(directionsText.toString());
    }



}
