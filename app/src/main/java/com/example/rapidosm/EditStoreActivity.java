package com.example.rapidosm;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditStoreActivity extends AppCompatActivity {

    private EditText storeNameEditText, locationEditText, storeHoursEditText, contactsEditText;
    private RadioGroup supplyTypeRadioGroup, supplyStatusRadioGroup;
    private DatabaseReference databaseReference;
    private String storeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_store);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("stores");

        // Initialize EditText views
        storeNameEditText = findViewById(R.id.store_name_edit_text);
        locationEditText = findViewById(R.id.location_edit_text);
        storeHoursEditText = findViewById(R.id.store_hours_edit_text);
        contactsEditText = findViewById(R.id.contacts_edit_text);

        // Initialize RadioGroups for supply type and status
        supplyTypeRadioGroup = findViewById(R.id.supply_type_radio_group);
        supplyStatusRadioGroup = findViewById(R.id.supply_status_radio_group);

        // Retrieve store ID passed from previous activity (e.g., ViewRegisteredStoresActivity)
        storeId = getIntent().getStringExtra("storeId");
        loadStoreDetails(storeId);
    }

    private void loadStoreDetails(String storeId) {
        // Fetch the store details using storeId and set values in the EditTexts and RadioGroups
        databaseReference.child(storeId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Store store = task.getResult().getValue(Store.class);
                if (store != null) {
                    storeNameEditText.setText(store.getStoreName());
                    locationEditText.setText(store.getLocation());
                    storeHoursEditText.setText(store.getStoreHours());
                    contactsEditText.setText(store.getContacts());

                    // Set the supply type RadioButton selection based on store's supply type
                    selectSupplyType(store.getSupplyType());

                    // Set the supply status RadioButton selection based on store's supply status
                    selectSupplyStatus(store.getSupplyStatus());
                }
            }
        });
    }

    private void selectSupplyType(String supplyType) {
        switch (supplyType) {
            case "Water":
                supplyTypeRadioGroup.check(R.id.radio_water);
                break;
            case "Gas":
                supplyTypeRadioGroup.check(R.id.radio_gas);
                break;
            case "Food":
                supplyTypeRadioGroup.check(R.id.radio_food);
                break;
            case "Medicine":
                supplyTypeRadioGroup.check(R.id.radio_medicine);
                break;
        }
    }

    private void selectSupplyStatus(String supplyStatus) {
        switch (supplyStatus) {
            case "Available":
                supplyStatusRadioGroup.check(R.id.radio_available);
                break;
            case "Limited":
                supplyStatusRadioGroup.check(R.id.radio_limited);
                break;
            case "Out of Stock":
                supplyStatusRadioGroup.check(R.id.radio_out_of_stock);
                break;
        }
    }

    public void updateStoreDetails(View view) {
        // Get the updated values from EditTexts and RadioGroups
        String storeName = storeNameEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String storeHours = storeHoursEditText.getText().toString().trim();
        String contacts = contactsEditText.getText().toString().trim();
        String supplyType = getSelectedSupplyType();
        String supplyStatus = getSelectedSupplyStatus();

        // Validate the inputs
        if (storeName.isEmpty() || location.isEmpty() || storeHours.isEmpty() || contacts.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
        } else {
            // Create a Store object with the updated details
            Store updatedStore = new Store(storeName, location, storeHours, contacts, supplyType, supplyStatus, FirebaseAuth.getInstance().getCurrentUser().getUid());

            // Update the store details in the Firebase database
            databaseReference.child(storeId).setValue(updatedStore).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditStoreActivity.this, "Store details updated successfully", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                } else {
                    Toast.makeText(EditStoreActivity.this, "Failed to update store details", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String getSelectedSupplyType() {
        int selectedId = supplyTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";
    }

    private String getSelectedSupplyStatus() {
        int selectedId = supplyStatusRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = findViewById(selectedId);
        return selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";
    }
}
