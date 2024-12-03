package com.example.rapidosm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Intent;

public class Signup extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private CheckBox termsCheckbox;
    private TextView tvTerms;
    private Button signUpButton;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        termsCheckbox = findViewById(R.id.termsCheckbox);
        tvTerms = findViewById(R.id.tvTerms);
        signUpButton = findViewById(R.id.signUpButton);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        tvTerms.setOnClickListener(v -> {
            // Open Terms and Conditions Activity
            Intent intent = new Intent(Signup.this, TermsAndConditionsActivity.class);
            startActivity(intent);
        });

        signUpButton.setOnClickListener(v -> signUpUser());
    }

    private void signUpUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validation checks
        if (!termsCheckbox.isChecked()) {
            Toast.makeText(Signup.this, "You must agree to the Terms and Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(Signup.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(Signup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Simple Email Format Check
        if (!email.matches("^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            Toast.makeText(Signup.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password strength check (optional)
        if (password.length() < 6) {
            Toast.makeText(Signup.this, "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication to create a new user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        // Store additional user data
                        Store newStoreUser = new Store();
                        newStoreUser.setUsername(username);
                        newStoreUser.setUserId(userId);

                        // Save to Firebase Realtime Database
                        databaseReference.child(userId).setValue(newStoreUser)
                                .addOnCompleteListener(saveTask -> {
                                    if (saveTask.isSuccessful()) {
                                        Toast.makeText(Signup.this, "Sign-up successful", Toast.LENGTH_SHORT).show();
                                        // Redirect to login or main activity
                                        Intent loginIntent = new Intent(Signup.this, Signin.class);
                                        startActivity(loginIntent);
                                        finish(); // Close the signup activity
                                    } else {
                                        Toast.makeText(Signup.this, "Failed to save user details", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        // Specific error messages based on exception type
                        String errorMessage = task.getException().getMessage();
                        if (errorMessage.contains("The email address is already in use")) {
                            Toast.makeText(Signup.this, "Email is already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Signup.this, "Authentication failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
