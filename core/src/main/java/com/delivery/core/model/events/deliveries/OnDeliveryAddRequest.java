package com.delivery.core_model.events.deliveries;

public record OnDeliveryAddRequest(int addressId, String earlyTimeString, String lateTimeString) {}
