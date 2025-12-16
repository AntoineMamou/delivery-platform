package com.delivery.core.ui;

import com.delivery.core.events.OnOptimizationResult;
import com.delivery.core.model.Graph;

public interface DeliveryOptimizerView {
    void showOptimizationResult(OnOptimizationResult result);
    void setOptimizeAction(Runnable action);
    int getWarehouseNodeId();
    Graph getGraph();
}
