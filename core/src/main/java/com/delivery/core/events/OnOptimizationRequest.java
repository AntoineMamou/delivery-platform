package com.delivery.core_model.events;

import java.util.List;

import com.delivery.core_model.Delivery;
import com.delivery.core_model.Graph;
import com.delivery.core_model.Truck;

public record OnOptimizationRequest(
    List<Delivery> deliveries,
    List<Truck> trucks,
    int warehouseNodeId,
    Graph graph
) {}