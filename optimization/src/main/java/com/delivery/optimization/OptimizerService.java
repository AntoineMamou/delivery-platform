package com.delivery.optimization;

import java.util.*;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.eventbus.OptimizationRequest;
import com.delivery.core.eventbus.OptimizationResult;
import com.delivery.core.model.Delivery;
import com.delivery.core.model.Route;
import com.delivery.core.model.Truck;

public class OptimizerService {

    private final EventBus eventBus;

    public OptimizerService(EventBus eventBus) {
        this.eventBus = eventBus;
        subscribe();
    }

    private void subscribe() {
        eventBus.subscribe(OptimizationRequest.class, this::handleOptimization);
    }

    private void handleOptimization(OptimizationRequest request) {

        System.out.println("[Optimizer] OptimizationRequest recu.");

        List<Route> routes = optimize(
                request.deliveries(),
                request.trucks(),
                request.warehouseNodeId()
        );
        
        // Conversion Route -> String
        List<String> readableSteps = routes.stream()
                .map(Route::toString) // ou un format personnalisé
                .toList();

        eventBus.publish(new OptimizationResult(readableSteps));
    }
    
    
    private List<Route> optimize(List<Delivery> deliveries, List<Truck> trucks, int warehouseId) {

        List<Route> result = new ArrayList<>();

        // Pour le moment : distribuer équitablement les livraisons entre les camions
        int deliveriesPerTruck = (int) Math.ceil(deliveries.size() / (double) trucks.size());

        int index = 0;

        for (Truck truck : trucks) {

            List<Delivery> assigned = new ArrayList<>();

            for (int i = 0; i < deliveriesPerTruck && index < deliveries.size(); i++) {
                assigned.add(deliveries.get(index++));
            }

            // Créer une "route"
            Route r = new Route(truck, warehouseId, assigned);
            result.add(r);
        }

        return result;
    }

    
    
    
    
    
}
