package GraphReader.ui;

import java.util.ArrayList;
import java.util.List;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.eventbus.TruckAddRequestEvent;
import com.delivery.core.eventbus.TruckAddedEvent;
import com.delivery.core.eventbus.TruckDeleteRequestEvent;
import com.delivery.core.eventbus.TruckDeletedEvent;
import com.delivery.core.model.Truck;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class TruckManager {
	private static List<Truck> trucks = new ArrayList<>();
	
	private static IntegerProperty truckCount = new SimpleIntegerProperty(0);
	
	public static void init() {
		EventBus.getInstance().subscribe(TruckAddRequestEvent.class, event -> {
			System.out.println("Received add request");
            addTruck(event.deliveryCapacityString(), event.maxDistanceString());
        });
		
		EventBus.getInstance().subscribe(TruckDeleteRequestEvent.class, event -> {
			System.out.println("Received remove request");
			removeTruck(event.id());
        });
	}
	
	public static ArrayList<Truck> getTrucks() {
        return new ArrayList<>(trucks); // return a copy
    }
	
	public static IntegerProperty getTruckCount() { return truckCount; }
	
	private static void addTruck(String deliveryCapacityString, String maxDistanceString) {
		System.out.println("Attempting to add truck");
        if (deliveryCapacityString.isEmpty() || maxDistanceString.isEmpty()) return;

        int deliveryCapacity = Integer.parseInt(deliveryCapacityString);
        float maxDistance = Float.parseFloat(maxDistanceString);

        if (deliveryCapacity <= 0 || maxDistance <= 0) return;

        Truck truck = new Truck(truckCount.get(), deliveryCapacity, maxDistance);

        trucks.add(truck);

        truckCount.setValue(truckCount.get() + 1);
        
        EventBus.getInstance().publish(new TruckAddedEvent(truck));
    }
	
	private static void removeTruck(int index)
	{
		System.out.println("Attempting to remove truck");
		if (trucks.size() <= 0) return;
		
		trucks.remove(index);
		
		truckCount.setValue(truckCount.get() - 1);
		
		//refresh the ids after deletion
		for (int i = index; i < trucks.size(); i++)
		{
			trucks.get(i).setId(i);
		}
		
		EventBus.getInstance().publish(new TruckDeletedEvent(index));
	}
}
