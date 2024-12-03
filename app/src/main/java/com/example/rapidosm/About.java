package com.example.rapidosm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class About extends AppCompatActivity {

    private TextView usernameTextView;
    private Button logoutButton, globalChatButton, userChatButton, faqButton, aboutRapidButton;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // Initialize FirebaseAuth and DatabaseReference
        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Find the TextView and Buttons by ID
        usernameTextView = findViewById(R.id.tvUsername);
        logoutButton = findViewById(R.id.logoutButton);
        globalChatButton = findViewById(R.id.btnGlobalChat);;
        aboutRapidButton = findViewById(R.id.aboutRapidButton);

        // Get the current user
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            // Fetch username from the database
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Store storeUser = dataSnapshot.getValue(Store.class);
                        if (storeUser != null && storeUser.getUsername() != null) {
                            usernameTextView.setText(storeUser.getUsername());
                        } else {
                            usernameTextView.setText("User");
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    usernameTextView.setText("Error fetching username");
                }
            });
        } else {
            usernameTextView.setText("Guest");
        }

        // Set up the logout button click listener
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(About.this, Signin.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Set up the button click listeners
        globalChatButton.setOnClickListener(v -> {
            // Navigate to Global Chat screen
            startActivity(new Intent(About.this, GlobalChat.class));
        });

        aboutRapidButton.setOnClickListener(v -> {
            // Navigate to About Rapid screen
            startActivity(new Intent(About.this, AboutRapid.class));
        });

        // Bottom navigation setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_about);

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
                startActivity(new Intent(this, AlertCenter.class));
                return true;
            } else if (itemId == R.id.nav_about) {
                return true; // Already in About, no action needed
            }
            return false;
        });
    }
}
