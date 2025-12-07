package com.delivery.core.eventbus;

import java.util.List;

import com.delivery.core.model.Delivery;
import com.delivery.core.model.Route;


public record OptimizationResult(List<Route> routes, List<Delivery> chronologicalDeliveries) {}
