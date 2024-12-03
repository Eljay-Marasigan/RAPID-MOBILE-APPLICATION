package com.example.rapidosm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterStore extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&q=";

    private DatabaseReference databaseReference;
    private FusedLocationProviderClient fusedLocationClient;
    private AutoCompleteTextView locationAutoCompleteTextView;
    private Switch locationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_store);

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("stores");

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Setup AutoComplete for location input
        locationAutoCompleteTextView = findViewById(R.id.edit_text_location);
        locationAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (charSequence.length() >= 2) {
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(() -> fetchLocationSuggestions(charSequence.toString()));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Setup location switch
        locationSwitch = findViewById(R.id.location_switch);
        locationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                getLocation();
            } else {
                locationAutoCompleteTextView.setEnabled(true);
                locationAutoCompleteTextView.setText("");
            }
        });

        // Setup Save Button
        setupSaveButton();

        // Set up Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_registerstore);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                startActivity(new Intent(this, Dashboard.class));
                return true;
            } else if (itemId == R.id.nav_registerstore) {
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

        Button viewStoresButton = findViewById(R.id.view_stores_button);
        viewStoresButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewRegisteredStoresActivity.class);
            startActivity(intent);
        });
    }

    private void setupSaveButton() {
        Button saveButton = findViewById(R.id.save_button);
        EditText storeNameEditText = findViewById(R.id.edit_text_store_name);
        AutoCompleteTextView locationAutoCompleteTextView = findViewById(R.id.edit_text_location);
        EditText storeHoursEditText = findViewById(R.id.edit_text_store_hours);
        EditText contactsEditText = findViewById(R.id.edit_text_contacts);
        RadioGroup supplyTypeGroup = findViewById(R.id.radio_group_supply_type);
        RadioGroup supplyStatusGroup = findViewById(R.id.radio_group_supply_status);

        saveButton.setOnClickListener(v -> {
            String storeName = storeNameEditText.getText().toString().trim();
            String address = locationAutoCompleteTextView.getText().toString().trim();
            String storeHours = storeHoursEditText.getText().toString().trim();
            String contacts = contactsEditText.getText().toString().trim();

            if (storeName.isEmpty() || address.isEmpty() || storeHours.isEmpty() || contacts.isEmpty()) {
                Toast.makeText(RegisterStore.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedSupplyTypeId = supplyTypeGroup.getCheckedRadioButtonId();
            String supplyType = ((RadioButton) findViewById(selectedSupplyTypeId)).getText().toString();

            int selectedSupplyStatusId = supplyStatusGroup.getCheckedRadioButtonId();
            String supplyStatus = ((RadioButton) findViewById(selectedSupplyStatusId)).getText().toString();

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get the current user's ID
            Store store = new Store(storeName, address, storeHours, contacts, supplyType, supplyStatus, userId);

            databaseReference.push().setValue(store).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterStore.this, "Store registered successfully!", Toast.LENGTH_SHORT).show();

                    // Clear fields
                    storeNameEditText.setText("");
                    locationAutoCompleteTextView.setText("");
                    storeHoursEditText.setText("");
                    contactsEditText.setText("");
                    supplyTypeGroup.clearCheck();
                    supplyStatusGroup.clearCheck();
                } else {
                    Toast.makeText(RegisterStore.this, "Failed to register store!", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.requestLocationUpdates(
                    new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY),
                    locationCallback,
                    null /* Looper */
            );
        }
    }

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null && location.getAccuracy() <= 50) { // Only accept high accuracy locations
                        updateLocationField(location);
                        fusedLocationClient.removeLocationUpdates(locationCallback);  // Stop updates after getting location
                    }
                }
            }
        }
    };

    private void updateLocationField(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        getAddressFromLocation(latitude, longitude);  // Reverse geocode to get a human-readable address
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String address = addresses.get(0).getAddressLine(0); // Get the full address
                locationAutoCompleteTextView.setText(address);
                locationAutoCompleteTextView.setEnabled(false);  // Disable manual input
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchLocationSuggestions(String query) {
        ArrayList<String> suggestions = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(NOMINATIM_URL + encodedQuery);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            JSONArray jsonArray = new JSONArray(response.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject location = jsonArray.getJSONObject(i);
                String address = location.getString("display_name");
                suggestions.add(address);
            }

            runOnUiThread(() -> {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterStore.this, android.R.layout.simple_dropdown_item_1line, suggestions);
                locationAutoCompleteTextView.setAdapter(adapter);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                locationSwitch.setChecked(false);
            }
        }
    }
}
