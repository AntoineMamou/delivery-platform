package com.delivery.datafetcher;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.delivery.core.model.Graph;
import com.delivery.core.ports.GraphFetcherPort;
import com.google.gson.Gson;

public class GraphFetcherService implements GraphFetcherPort{

	private final Gson gson = new Gson();

	@Override
	public Graph fetchGraph(String resourceName) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName);
        if (is == null) throw new RuntimeException("Resource not found: " + resourceName);
        return gson.fromJson(new InputStreamReader(is), Graph.class);
	}
}
