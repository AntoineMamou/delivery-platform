package com.delivery.core.model;

import java.util.List;

public class Route {
    private Truck vehicle;
    private List<Delivery> deliveries;
    private List<Integer> path;

    public Route(Truck vehicle, int warehouseNodeId, List<Delivery> deliveries, List<Integer> path) {
        this.vehicle = vehicle;
        this.deliveries = deliveries;
        this.path = path;
    }

    public Truck getVehicle() { return vehicle; }
    public List<Delivery> getDeliveries() { return deliveries; }
    public List<Integer> getPath() { return path; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Camion ").append(vehicle.getId()).append("\n");

        for (Delivery d : deliveries) {
            sb.append("Livraison")
              .append(d.getId())
              .append(" -> Noeud ").append(d.getAddressNodeId())
              .append(" (").append(d.getEarliestDeliveryTime())
              .append(" - ").append(d.getlatestDeliveryTime()).append(")")
              .append("\n");
        }

        return sb.toString();
    }


}
