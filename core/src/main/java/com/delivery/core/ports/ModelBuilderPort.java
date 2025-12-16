package com.delivery.core.ports;

import java.util.List;

import com.delivery.core.model.Route;

public interface ModelBuilderPort {
    List<Route> buildRoutes();
}
