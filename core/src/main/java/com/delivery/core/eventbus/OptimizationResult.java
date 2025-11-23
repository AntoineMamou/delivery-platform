package com.delivery.core.eventbus;

import java.util.List;

public class OptimizationResult {

    private final List<String> steps;

    public OptimizationResult(List<String> steps) {
        this.steps = steps;
    }

    public List<String> getSteps() {
        return steps;
    }

    // Méthode utilitaire pour l'affichage dans UI
    public List<String> readableSteps() {
        return steps;
    }
}
