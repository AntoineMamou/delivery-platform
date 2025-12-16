package com.delivery.core.ui;

import com.delivery.core_model.Graph;
import com.delivery.core_model.events.OnOptimizationResult;

public interface DeliveryOptimizerView {
    void showOptimizationResult(OnOptimizationResult result);
    void setOptimizeAction(Runnable action);
    int getWarehouseNodeId();
    Graph getGraph();
}
