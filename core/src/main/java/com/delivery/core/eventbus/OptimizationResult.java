package com.delivery.core.eventbus;

import java.util.List;


public record OptimizationResult(List<String> steps) {

    public OptimizationResult {
        steps = List.copyOf(steps);
    }

    public List<String> readableSteps() {
        return steps;
    }
    
    
}
