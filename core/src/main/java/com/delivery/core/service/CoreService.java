package com.delivery.core.service;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.events.OnOptimizationRequest;

public class CoreService {

    private final EventBus eventBus;

    public CoreService(EventBus eventBus) {
        this.eventBus = eventBus;
        subscribeEvents();
    }

    private void subscribeEvents() {
        eventBus.subscribe(OnOptimizationRequest.class, request -> {
            System.out.println("[Core] OptimizationRequest reçu");
            System.out.println("Deliveries: " + request.deliveries().size());
            System.out.println("Trucks: " + request.trucks().size());
            
            // Ici, plus tard, tu appelleras l'optimizer
            // List<Delivery> optimized = optimizer.optimize(...);
            // eventBus.publish(new DeliveryOptimizedEvent(optimized));
        });
    }
}
