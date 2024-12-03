package com.example.rapidosm;

public class Store {

    private String storeName;
    private String location;
    private String storeHours;
    private String contacts;
    private String supplyType;
    private String supplyStatus;
    private String userId;
    private String username;
    private String id;
    // Default constructor (required for Firebase)
    public Store() {}

    // Constructor to initialize the store details
    public Store(String storeName, String location, String storeHours, String contacts, String supplyType, String supplyStatus, String userId) {
        this.storeName = storeName;
        this.location = location;
        this.storeHours = storeHours;
        this.contacts = contacts;
        this.supplyType = supplyType;
        this.supplyStatus = supplyStatus;
        this.userId = userId; // Assign the userId
    }

    // Getter and Setter methods for each property
    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStoreHours() {
        return storeHours;
    }

    public void setStoreHours(String storeHours) {
        this.storeHours = storeHours;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getSupplyType() {
        return supplyType;
    }

    public void setSupplyType(String supplyType) {
        this.supplyType = supplyType;
    }

    public String getSupplyStatus() {
        return supplyStatus;
    }

    public void setSupplyStatus(String supplyStatus) {
        this.supplyStatus = supplyStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return location;
    }

}
