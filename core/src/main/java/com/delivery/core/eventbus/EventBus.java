package com.delivery.core.eventbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class EventBus {

    private static final EventBus INSTANCE = new EventBus();

    private final Map<Class<?>, List<Consumer<?>>> subscribers = new HashMap<>();

    private EventBus() {
        // private constructor = singleton
    }

    public static EventBus getInstance() {
        return INSTANCE;
    }

    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        subscribers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        List<Consumer<?>> listeners = subscribers.get(event.getClass());
        if (listeners != null) {
            for (Consumer<?> listener : listeners) {
                ((Consumer<T>) listener).accept(event);
            }
        }
    }
}
