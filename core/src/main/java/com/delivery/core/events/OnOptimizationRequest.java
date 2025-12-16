package com.delivery.core.events;

import java.util.List;

import com.delivery.core.model.Delivery;
import com.delivery.core.model.Graph;
import com.delivery.core.model.Truck;

public record OnOptimizationRequest(
    List<Delivery> deliveries,
    List<Truck> trucks,
    int warehouseNodeId,
    Graph graph
) {}