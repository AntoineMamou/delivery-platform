package com.delivery.datafetcher;

import com.delivery.core.model.Delivery;
import java.util.List;
import java.util.ArrayList;

public class MockDataFetcher implements DataFetcher {

    @Override
    public List<Delivery> fetchDeliveries() {
        List<Delivery> deliveries = new ArrayList<>();
        //deliveries.add(new Delivery("Client1", "Adresse1", 9, 12));
        //deliveries.add(new Delivery("Client2", "Adresse2", 10, 13));
        return deliveries;
    }
}
