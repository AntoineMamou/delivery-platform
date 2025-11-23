package com.delivery.datafetcher;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.model.Delivery;
import java.util.List;

public class DataFetcherService {
    private final DataFetcher fetcher;
    private final EventBus eventBus;

    public DataFetcherService(DataFetcher fetcher, EventBus eventBus) {
        this.fetcher = fetcher;
        this.eventBus = eventBus;
    }

    public void loadDeliveries() {
        List<Delivery> deliveries = fetcher.fetchDeliveries();
       // eventBus.publish("delivery.loaded", deliveries);
    }
}
