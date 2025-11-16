package com.delivery.core.model;

public class Constraints {
    private int maxDistance;
    private int maxLoad;

    public Constraints(int maxDistance, int maxLoad) {
        this.maxDistance = maxDistance;
        this.maxLoad = maxLoad;
    }

    public int getMaxDistance() { return maxDistance; }
    public int getMaxLoad() { return maxLoad; }
}
