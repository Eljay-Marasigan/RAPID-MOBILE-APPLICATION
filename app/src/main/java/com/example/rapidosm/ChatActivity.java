package com.example.rapidosm;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;

public class ChatActivity extends AppCompatActivity {

    private TextView messagesTextView;
    private EditText messageInput;
    private Button sendButton;

    private String storeId;  // Store ID passed from the previous activity
    private DatabaseReference chatRef;
    private DatabaseReference storeRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize views
        messagesTextView = findViewById(R.id.messagesTextView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Get store ID from the Intent
        storeId = getIntent().getStringExtra("store_id");

        // Get the current user ID
        currentUserId = getIntent().getStringExtra("user_id"); // Or use SharedPreferences to get the user ID

        // Set up Firebase reference for this store's chat
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(storeId);
        storeRef = FirebaseDatabase.getInstance().getReference("stores").child(storeId);

        // Check if the current user is the owner of this store
        checkStoreOwnership();

        // Listen for new messages in Firebase
        listenForMessages();

        // Send message when the button is clicked
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void checkStoreOwnership() {
        // Check the store's userId to ensure it's not the current user's store
        storeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Store store = dataSnapshot.getValue(Store.class);
                if (store != null && store.getUserId().equals(currentUserId)) {
                    // The current user is the store owner, so prevent access to the chat
                    Toast.makeText(ChatActivity.this, "You cannot chat for your own store", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Failed to load store data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            // Push message to Firebase under this store's chat reference
            String messageId = chatRef.push().getKey();
            if (messageId != null) {
                chatRef.child(messageId).setValue(message)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                                messageInput.setText(""); // Clear input
                            } else {
                                Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }
    }

    private void listenForMessages() {
        // Listen for changes in the chat reference to display messages
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder messages = new StringBuilder();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String message = snapshot.getValue(String.class);
                    if (message != null) {
                        messages.append(message).append("\n"); // Append each message to display
                    }
                }
                messagesTextView.setText(messages.toString()); // Update the TextView with all messages
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
