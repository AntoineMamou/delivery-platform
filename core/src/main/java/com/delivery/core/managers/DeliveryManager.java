package com.delivery.core.managers;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.events.OnOptimizationResult;
import com.delivery.core.managers.exceptions.InvalidDeliveryWindowException;
import com.delivery.core.model.Delivery;
import com.delivery.core.model.events.deliveries.OnDeliveryAddRequest;
import com.delivery.core.model.events.deliveries.OnDeliveryDeleteRequest;
import com.delivery.core.model.events.deliveries.OnUpdatedDeliveries;
import com.delivery.core.model.events.graph.OnGraphLoaded;

public class DeliveryManager {
	private static EventBus eventBus;
	
	private static List<Delivery> deliveries = new ArrayList<>();
	
	private static int deliveryCount = 0;
	private static int warehouseNodeId = -1;
	
	private static int maxNodes;
	
	public static void init(EventBus eventBus){
		DeliveryManager.eventBus = eventBus;
		
		eventBus.subscribe(OnGraphLoaded.class, event -> maxNodes = event.graph().getNodes().size());
		eventBus.subscribe(OnOptimizationResult.class, event -> publishUpdate());
		eventBus.subscribe(OnDeliveryAddRequest.class, event -> addDelivery(event.addressId(), event.earlyTimeString(), event.lateTimeString()));
		eventBus.subscribe(OnDeliveryDeleteRequest.class, event -> removeDelivery(event.id()));
	}
	
	public static void setWarehouseNodeId(int warehouseNodeId) { DeliveryManager.warehouseNodeId = warehouseNodeId; }
	
	public static ArrayList<Delivery> getDeliveries() {
        return new ArrayList<>(deliveries); // return a copy
    }
	
	public static int getDeliveryCount() { return deliveryCount; }
	
	public static void addDelivery(int addressId, String earlyTimeString, String lateTimeString)
	{
		// Data wasn't provided correctly, we dont create a delivery.                
		if (earlyTimeString.isEmpty() || lateTimeString.isEmpty()
		        || addressId < 0 || addressId >= maxNodes 
		        || (warehouseNodeId != -1 && addressId == warehouseNodeId)) return;
        
        try {
        	LocalTime earlyTime = parseTime(earlyTimeString);
            LocalTime lateTime = parseTime(lateTimeString);
            
        	validateTimeWindow(earlyTime, lateTime);
        	
        	Delivery delivery = new Delivery(deliveryCount, addressId, earlyTime, lateTime);
            
        	deliveries.add(delivery);
        	
        	updateDeliveryCount();
            
            publishUpdate();
        } catch (InvalidDeliveryWindowException | DateTimeParseException e) {
            System.err.println("Error: " + e.getMessage());
        }
	}
	
	private static void removeDelivery(int index)
	{
		if (index < 0 || index >= deliveries.size()) return;
		
		deliveries.remove(index);
		
		updateDeliveryCount();
		
		// Reassign the ids after deletion.
		for (int i = index; i < deliveries.size(); i++) deliveries.get(i).setId(i);

		
		publishUpdate();
	}
	
	private static void updateDeliveryCount()
	{
		deliveryCount = deliveries.size();
	}
	
	public static void publishUpdate()
	{
		List<String> deliveryStrings =
        	    deliveries.stream()
        	              .map(Delivery::toString)
        	              .toList();
        
        eventBus.publish(new OnUpdatedDeliveries(deliveryStrings));
	}
	
	private static LocalTime parseTime(String str) {
		// Expecting "HH:mm" format; throws DateTimeParseException if invalid
        return LocalTime.parse(str);
    }
    
    private static void validateTimeWindow(LocalTime startTime, LocalTime endTime) throws InvalidDeliveryWindowException
    {
    	if (startTime.isAfter(endTime)) {
            throw new InvalidDeliveryWindowException(
                "Invalid delivery window: start time (" + startTime + ") cannot be after end time (" + endTime + ")"
            );
        }
    }
	
	
	
}
