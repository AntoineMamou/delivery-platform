package com.delivery.datafetcher;

import com.delivery.core.model.Delivery;
import java.util.List;

public interface DataFetcher {
    List<Delivery> fetchDeliveries();
}
