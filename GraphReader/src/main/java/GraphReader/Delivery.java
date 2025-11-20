package GraphReader;

import java.time.*;

public class Delivery {
	private int id;
	private int addressNodeId;
	private LocalTime earliestDeliveryTime;
	private LocalTime latestDeliveryTime;
	
	public Delivery(int id, int addressNodeId, LocalTime earliestDeliveryTime, LocalTime latestDeliveryTime)
	{
		this.id = id;
		this.addressNodeId = addressNodeId;
		this.earliestDeliveryTime = earliestDeliveryTime;
		this.latestDeliveryTime = latestDeliveryTime;
	}
	
	public int getId() { return id; }
	
	public int getAddressNodeId() { return addressNodeId; }
	
	public LocalTime getEarliestDeliveryTime() { return earliestDeliveryTime; }
	
	public LocalTime getlatestDeliveryTime() { return latestDeliveryTime; }
	
	public String toString()
	{
		return String.format("Id: %d | Address: %d | Delivery window: %s to %s",
                id, addressNodeId, earliestDeliveryTime, latestDeliveryTime);
	}
}
