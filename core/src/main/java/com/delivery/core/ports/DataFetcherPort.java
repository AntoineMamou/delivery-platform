package com.delivery.core.ports;

import java.util.List;

import com.delivery.core_model.Delivery;

public interface DataFetcherPort {
    List<Delivery> fetchDeliveries();
}
