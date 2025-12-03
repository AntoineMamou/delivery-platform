package com.delivery.core.eventbus;

import java.util.List;

import com.delivery.core.model.Delivery;
import com.delivery.core.model.Truck;

public record OptimizationRequest(
    List<Delivery> deliveries,
    List<Truck> trucks,
    int warehouseNodeId
) {}