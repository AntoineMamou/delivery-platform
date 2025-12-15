package com.delivery.core_model.events;

import java.util.List;

import com.delivery.core_model.Delivery;
import com.delivery.core_model.Route;


public record OnOptimizationResult(List<Route> routes, List<Delivery> chronologicalDeliveries) {}
