package com.delivery.core.eventbus;

public record DeliveryAddRequestEvent(int addressId, String earlyTimeString, String lateTimeString) {}
