package com.delivery.core.managers;

import java.util.ArrayList;
import java.util.List;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core_model.Truck;
import com.delivery.core_model.events.trucks.OnTruckAddRequest;
import com.delivery.core_model.events.trucks.OnTruckDeleteRequest;
import com.delivery.core_model.events.trucks.OnUpdatedTrucks;

public class TruckManager {
	private static EventBus eventBus;
	
	private static List<Truck> trucks = new ArrayList<>();
	
	private static int truckCount = 0;
	
	public static void init(EventBus eventBus) {
		TruckManager.eventBus = eventBus;
		
		eventBus.subscribe(OnTruckAddRequest.class, event -> addTruck(event.deliveryCapacityString(), event.maxDistanceString()));
		eventBus.subscribe(OnTruckDeleteRequest.class, event -> removeTruck(event.id()));
	}
	
	public static ArrayList<Truck> getTrucks() {
        return new ArrayList<>(trucks);
    }
	
	public static int getTruckCount() { return truckCount; }
	
	private static void addTruck(String deliveryCapacityString, String maxDistanceString) {
        if (deliveryCapacityString.isEmpty() || maxDistanceString.isEmpty()) return;

        int deliveryCapacity = Integer.parseInt(deliveryCapacityString);
        float maxDistance = Float.parseFloat(maxDistanceString);

        if (deliveryCapacity <= 0 || maxDistance <= 0) return;

        Truck truck = new Truck(truckCount, deliveryCapacity, maxDistance);

        trucks.add(truck);

        updateTruckCount();
        
        publishUpdate();
    }
	
	private static void removeTruck(int index)
	{
		if (index < 0 || index >= trucks.size()) return;
		
		trucks.remove(index);
		
		updateTruckCount();
		
		//refresh the ids after deletion
		for (int i = index; i < trucks.size(); i++)
		{
			trucks.get(i).setId(i);
		}
		
		publishUpdate();
	}
	
	private static void updateTruckCount()
	{
		truckCount = trucks.size();
	}
	
	private static void publishUpdate()
	{
		List<String> truckStrings =
        	    trucks.stream()
        	              .map(Truck::toString)
        	              .toList();
        
        eventBus.publish(new OnUpdatedTrucks(truckStrings));
	}
}
