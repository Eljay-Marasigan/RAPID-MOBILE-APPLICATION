package com.example.rapidosm;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class TermsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        ImageButton btnClose = findViewById(R.id.btnClose);

        // Set a listener for the close button (ImageButton)
        btnClose.setOnClickListener(v -> finish());
    }
}
