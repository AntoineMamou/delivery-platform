package com.delivery.core.eventbus;

import java.util.*;
import java.util.function.Consumer;

public class EventBus {
    private Map<String, List<Consumer<Object>>> listeners = new HashMap<>();

    public <T> void subscribe(String eventName, Consumer<T> handler) {
        listeners.computeIfAbsent(eventName, k -> new ArrayList<>())
                 .add((Consumer<Object>) handler);
    }

    public void publish(String eventName, Object data) {
        listeners.getOrDefault(eventName, List.of())
                 .forEach(listener -> listener.accept(data));
    }
}
