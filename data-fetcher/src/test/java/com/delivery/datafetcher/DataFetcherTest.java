package com.delivery.datafetcher;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.model.Delivery;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataFetcherTest {
	/*
    @Test
    void testMockDataFetcher() {
        MockDataFetcher fetcher = new MockDataFetcher();
        List<Delivery> deliveries = fetcher.fetchDeliveries();
        assertEquals(2, deliveries.size());
        assertEquals("Client1", deliveries.get(0).getClient());
    }

    @Test
    void testEventBusPublishing() {
        EventBus bus = new EventBus();
        DataFetcherService service = new DataFetcherService(new MockDataFetcher(), bus);

        final boolean[] received = {false};
        bus.subscribe("delivery.loaded", (data) -> {
            List<Delivery> deliveries = (List<Delivery>) data;
            if (deliveries.size() == 2) received[0] = true;
        });

        service.loadDeliveries();
        assertTrue(received[0], "Les livraisons n'ont pas été publiées !");
    }
    */
}
