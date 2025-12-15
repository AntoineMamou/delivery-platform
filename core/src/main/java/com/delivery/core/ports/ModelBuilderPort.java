package com.delivery.core.ports;

import java.util.List;

import com.delivery.core_model.Route;

public interface ModelBuilderPort {
    List<Route> buildRoutes();
}
