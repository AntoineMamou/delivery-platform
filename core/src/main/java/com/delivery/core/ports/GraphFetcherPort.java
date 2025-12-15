package com.delivery.core.ports;

import com.delivery.core_model.Graph;

public interface GraphFetcherPort {
	Graph fetchGraph(String resourceName);
}
