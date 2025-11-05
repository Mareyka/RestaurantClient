package com.example.restaurantclient.models;

public class Client {
    private int id;
    private String fullName;
    private String contacts;

    public Client() {}

    public Client(int id, String fullName, String contacts) {
        this.id = id;
        this.fullName = fullName;
        this.contacts = contacts;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getContacts() { return contacts; }
    public void setContacts(String contacts) { this.contacts = contacts; }
}