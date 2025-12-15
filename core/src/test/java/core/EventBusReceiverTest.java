package core;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core_model.events.OnOptimizationRequest;

public class EventBusReceiverTest {

    public static void main(String[] args) {
        EventBus eventBus = EventBus.getInstance();

        // S’abonner à l’événement
        eventBus.subscribe(OnOptimizationRequest.class, request -> {
            System.out.println("=== Event reçu dans Core ===");
            System.out.println("Warehouse Node: " + request.warehouseNodeId());
            System.out.println("Nb deliveries: " + request.deliveries().size());
            System.out.println("Nb trucks: " + request.trucks().size());
        });

    }
}
