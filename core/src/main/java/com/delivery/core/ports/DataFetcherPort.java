package com.delivery.core.ports;

import com.delivery.core.model.Delivery;
import java.util.List;

public interface DataFetcherPort {
    List<Delivery> fetchDeliveries();
}
