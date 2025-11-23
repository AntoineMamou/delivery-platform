package com.delivery.core.model;

import java.util.List;

public class Route {
    private Truck vehicle;
    private List<Delivery> deliveries;

    public Route(Truck vehicle, List<Delivery> deliveries) {
        this.vehicle = vehicle;
        this.deliveries = deliveries;
    }

    public Truck getVehicle() { return vehicle; }
    public List<Delivery> getDeliveries() { return deliveries; }
}
