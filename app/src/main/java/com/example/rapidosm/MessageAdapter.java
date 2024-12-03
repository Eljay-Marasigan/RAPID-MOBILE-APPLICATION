package com.example.rapidosm;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message> {
    private Context context;
    private List<Message> messages;
    private String currentUserId;

    public MessageAdapter(Context context, List<Message> messages, String currentUserId) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);

        // Inflate the layout if not already done
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }

        // Retrieve views
        TextView messageText = convertView.findViewById(R.id.messageText);
        TextView usernameText = convertView.findViewById(R.id.usernameText);
        LinearLayout messageLayout = convertView.findViewById(R.id.messageLayout);

        // Set text content
        messageText.setText(message.getMessage());
        usernameText.setText(message.getSenderUsername());

        // Programmatically adjust layout for the current user
        if (message.getSenderId().equals(currentUserId)) {
            // If it's the current user's message:
            // 1. Align to the right
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) messageLayout.getLayoutParams();
            layoutParams.gravity = Gravity.END; // Align right
            messageLayout.setLayoutParams(layoutParams);

            // 2. Use the "bubble_right" design
            messageText.setBackgroundResource(R.drawable.bubble_right);

            // 3. Adjust padding for better positioning
            messageText.setPadding(20, 20, 20, 20); // Add more right padding

            // 4. Hide the username (optional for the current user)
            usernameText.setVisibility(View.GONE);
        } else {
            // If it's another user's message:
            // 1. Align to the left
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) messageLayout.getLayoutParams();
            layoutParams.gravity = Gravity.START; // Align left
            messageLayout.setLayoutParams(layoutParams);

            // 2. Use the "bubble_left" design
            messageText.setBackgroundResource(R.drawable.bubble_left);

            // 3. Adjust padding for better positioning
            messageText.setPadding(20, 20, 20, 20); // Add more left padding

            // 4. Show the username
            usernameText.setVisibility(View.VISIBLE);
        }

        return convertView;
    }


}

