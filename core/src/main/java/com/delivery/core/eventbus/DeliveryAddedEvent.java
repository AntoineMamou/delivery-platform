package com.delivery.core.eventbus;

import com.delivery.core.model.Delivery;

public record DeliveryAddedEvent(Delivery delivery) {}