package com.delivery.core.model;

public class Delivery {
    private String client;
    private String address;
    private int windowStart;
    private int windowEnd;

    public Delivery(String client, String address, int windowStart, int windowEnd) {
        this.client = client;
        this.address = address;
        this.windowStart = windowStart;
        this.windowEnd = windowEnd;
    }

    // Getters et setters
    public String getClient() { return client; }
    public String getAddress() { return address; }
    public int getWindowStart() { return windowStart; }
    public int getWindowEnd() { return windowEnd; }
}
