package com.delivery.core.events;

import java.util.List;

import com.delivery.core.model.Delivery;
import com.delivery.core.model.Route;


public record OnOptimizationResult(List<Route> routes, List<Delivery> chronologicalDeliveries) {}
