package com.delivery.core.model;

public class Vehicle {
    private String id;
    private int capacity;
    private String currentLocation;

    public Vehicle(String id, int capacity, String currentLocation) {
        this.id = id;
        this.capacity = capacity;
        this.currentLocation = currentLocation;
    }

    public String getId() { return id; }
    public int getCapacity() { return capacity; }
    public String getCurrentLocation() { return currentLocation; }
}
