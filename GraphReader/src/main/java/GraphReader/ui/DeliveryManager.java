package GraphReader.ui;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.delivery.core.eventbus.DeliveryAddRequestEvent;
import com.delivery.core.eventbus.DeliveryAddedEvent;
import com.delivery.core.eventbus.DeliveryDeleteRequestEvent;
import com.delivery.core.eventbus.DeliveryDeletedEvent;
import com.delivery.core.eventbus.EventBus;
import com.delivery.core.events.GraphLoadedEvent;
import com.delivery.core.model.Delivery;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class DeliveryManager {
	private static List<Delivery> deliveries = new ArrayList<>();
	
	private static IntegerProperty deliveryCount = new SimpleIntegerProperty(0);
	private static IntegerProperty warehouseNodeId;
	
	private static int maxNodes;
	
	public static void init(){
		EventBus.getInstance().subscribe(GraphLoadedEvent.class, event -> maxNodes = event.graph().getNodes().size());
		
		EventBus.getInstance().subscribe(DeliveryAddRequestEvent.class, event -> {
            addDelivery(event.addressId(), event.earlyTimeString(), event.lateTimeString());
        });
		
		EventBus.getInstance().subscribe(DeliveryDeleteRequestEvent.class, event -> {
            removeDelivery(event.id());
        });
	}
	
	public static void setWarehouseNodeId(IntegerProperty warehouseNodeId) { DeliveryManager.warehouseNodeId = warehouseNodeId; }
	
	public static ArrayList<Delivery> getDeliveries() {
        return new ArrayList<>(deliveries); // return a copy
    }
	
	public static IntegerProperty getDeliveryCount() { return deliveryCount; }
	
	public static void addDelivery(int addressId, String earlyTimeString, String lateTimeString)
	{
		System.out.print("Trying to add delivery, max nodes: " + maxNodes);
		// Data wasn't provided correctly, we dont create a delivery.                
        if (earlyTimeString.isEmpty() || lateTimeString.isEmpty()
        		|| addressId < 0 || addressId >= maxNodes || addressId == warehouseNodeId.get()) return;
        
        LocalTime earlyTime = getTimeFromString(earlyTimeString);
        LocalTime lateTime = getTimeFromString(lateTimeString);
        
        try {
        	validateTimeWindow(earlyTime, lateTime);
        	
        	Delivery delivery = new Delivery(deliveryCount.get(), addressId, earlyTime, lateTime);
            
        	deliveries.add(delivery);
        	
            //Increment delivery count
            deliveryCount.setValue(deliveryCount.get() + 1);
            
            EventBus.getInstance().publish(new DeliveryAddedEvent(delivery));
            
            System.out.print("Added delivery");
        } catch (InvalidDeliveryWindowException e) {
            System.err.println("Error: " + e.getMessage());
        }
	}
	
	private static void removeDelivery(int index)
	{
		if (deliveries.size() <= 0) return;
		
		deliveries.remove(index);
		
		deliveryCount.setValue(deliveryCount.get() - 1);
		
		//refresh the ids after deletion
		for (int i = index; i < deliveries.size(); i++)
		{
			deliveries.get(i).setId(i);
		}
		
		EventBus.getInstance().publish(new DeliveryDeletedEvent(index));
	}
	
	private static LocalTime getTimeFromString(String string)
    {
    	LocalTime time;
    	
    	if (string.length() < 2) return null;
    	
    	int separationIndex = string.indexOf(":");
    	
    	int hour = Integer.parseInt(string.substring(0, separationIndex));
        int minute = Integer.parseInt(string.substring(separationIndex + 1, 5));
            
        time = LocalTime.of(hour, minute);
        
        return time;
    	
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
