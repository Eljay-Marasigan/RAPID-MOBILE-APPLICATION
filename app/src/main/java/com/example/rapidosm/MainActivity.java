package com.example.rapidosm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button findWayButton = findViewById(R.id.findWayButton);
        findWayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to SignInActivity
                Intent intent = new Intent(MainActivity.this, Signin.class);
                startActivity(intent);
            }
        });
     }
}
