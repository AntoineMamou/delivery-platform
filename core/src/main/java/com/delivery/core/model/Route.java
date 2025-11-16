package com.delivery.core.model;

import java.util.List;

public class Route {
    private Vehicle vehicle;
    private List<Delivery> deliveries;

    public Route(Vehicle vehicle, List<Delivery> deliveries) {
        this.vehicle = vehicle;
        this.deliveries = deliveries;
    }

    public Vehicle getVehicle() { return vehicle; }
    public List<Delivery> getDeliveries() { return deliveries; }
}
