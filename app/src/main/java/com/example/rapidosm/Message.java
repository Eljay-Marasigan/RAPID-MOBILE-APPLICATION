package com.example.rapidosm;

public class Message {
    private String message;
    private String senderId;
    private String senderUsername;
    private long timestamp;

    // Default constructor
    public Message() {}

    // Constructor
    public Message(String message, String senderId, String senderUsername, long timestamp) {
        this.message = message;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

