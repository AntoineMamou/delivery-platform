package com.delivery.core.model;

import java.util.List;

public class Route {
    private Truck vehicle;
    private List<Delivery> deliveries;

    public Route(Truck vehicle, int warehouseNodeId, List<Delivery> deliveries) {
        this.vehicle = vehicle;
        this.deliveries = deliveries;
    }

    public Truck getVehicle() { return vehicle; }
    public List<Delivery> getDeliveries() { return deliveries; }
    
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
