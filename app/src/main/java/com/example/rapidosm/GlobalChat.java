package com.example.rapidosm;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ListView;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class GlobalChat extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private ListView messageListView;
    private ArrayList<Message> messagesList = new ArrayList<>();
    private MessageAdapter messagesAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference messagesDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global_chat);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        messageListView = findViewById(R.id.messageListView);

        mAuth = FirebaseAuth.getInstance();
        messagesDatabase = FirebaseDatabase.getInstance().getReference("Messages");

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Initialize the adapter
        messagesAdapter = new MessageAdapter(this, messagesList, currentUserId);
        messageListView.setAdapter(messagesAdapter);

        sendButton.setOnClickListener(v -> sendMessage());

        // Listen for new messages
        listenForMessages();
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            String senderId = mAuth.getCurrentUser().getUid();

            getUsernameFromUserId(senderId, new UsernameCallback() {
                @Override
                public void onUsernameReceived(String username) {
                    Message newMessage = new Message(message, senderId, username, System.currentTimeMillis());

                    // Send message to the Firebase Realtime Database
                    messagesDatabase.push().setValue(newMessage)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    messageInput.setText(""); // Clear the input field

                                    // Scroll to the latest message after sending
                                    messageListView.smoothScrollToPosition(messagesAdapter.getCount() - 1);
                                } else {
                                    Toast.makeText(GlobalChat.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }


    private void listenForMessages() {
        messagesDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message != null) {
                    messagesList.add(message);
                    messagesAdapter.notifyDataSetChanged();

                    // Scroll to the latest message
                    messageListView.smoothScrollToPosition(messagesAdapter.getCount() - 1);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void getUsernameFromUserId(String userId, final UsernameCallback callback) {
        DatabaseReference usersDatabase = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        usersDatabase.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Store storeUser = task.getResult().getValue(Store.class);
                if (storeUser != null) {
                    callback.onUsernameReceived(storeUser.getUsername());
                } else {
                    callback.onUsernameReceived("Unknown User");
                }
            } else {
                callback.onUsernameReceived("Error retrieving username");
            }
        });
    }

    public interface UsernameCallback {
        void onUsernameReceived(String username);
    }
}
