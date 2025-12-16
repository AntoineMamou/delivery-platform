package com.delivery.datafetcher;

import java.util.List;

import com.delivery.core.model.Delivery;

public interface DataFetcher {
    List<Delivery> fetchDeliveries();
}
