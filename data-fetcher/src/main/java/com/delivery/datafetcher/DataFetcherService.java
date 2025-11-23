package com.delivery.datafetcher;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.events.FetchGraphRequest;
import com.delivery.core.events.GraphLoadedEvent;
import com.delivery.core.model.Graph;


public class DataFetcherService {

    private final EventBus eventBus;

    public DataFetcherService(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void start() {

        // Abonnement à la demande de chargement du graph
        eventBus.subscribe(FetchGraphRequest.class, request -> {
            System.out.println("[DataFetcher] FetchGraphRequest reçu");
            
            GraphLoader loader = new GraphLoader();
            Graph graph = loader.load();   // Ta classe de chargement
            System.out.println("[DataFetcher] Graph chargé, publication de GraphLoadedEvent");

            // Publication de l'évènement vers les autres modules
            eventBus.publish(new GraphLoadedEvent(graph));
        });
    }
}
