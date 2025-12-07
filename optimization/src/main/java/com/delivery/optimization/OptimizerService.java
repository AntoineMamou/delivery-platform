package com.delivery.optimization;

import java.util.*;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.eventbus.OptimizationRequest;
import com.delivery.core.eventbus.OptimizationResult;
import com.delivery.core.model.Delivery;
import com.delivery.core.model.Route;
import com.delivery.core.model.Truck;
import com.delivery.optimization.*;

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
        System.out.println("[Optimizer] Calcul en cours...");

        // 1. Calculer les routes (Vue par Camion)
        List<Route> routes = DeliveryOptimizer.optimize(
                request.deliveries(),
                request.trucks(),
                request.warehouseNodeId(),
                request.graph()
        );
        
        // 2. Créer la liste chronologique globale (Vue Temporelle)
        // On prend toutes les livraisons de toutes les routes, on les met à plat, et on trie par heure
        List<Delivery> chronologicalDeliveries = routes.stream()
                .flatMap(route -> route.getDeliveries().stream())
                .sorted(Comparator.comparing(Delivery::getEstimatedArrivalTime)) // Trier par heure d'arrivée
                .toList();

        // 3. Publier le résultat structuré
        eventBus.publish(new OptimizationResult(routes, chronologicalDeliveries));
    }
 
    
    
    
}
