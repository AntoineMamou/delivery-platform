package com.delivery.core.ports;

import com.delivery.core.model.Route;
import java.util.List;

public interface ModelBuilderPort {
    List<Route> buildRoutes();
}
