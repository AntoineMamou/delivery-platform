package com.delivery.core.model.events.deliveries;

public record OnDeliveryAddRequest(int addressId, String earlyTimeString, String lateTimeString) {}
