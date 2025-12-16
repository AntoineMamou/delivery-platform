package com.delivery.core_model;

import java.time.*;

public class Delivery {
	private int id;
	private int addressNodeId;
	private LocalTime earliestDeliveryTime;
	private LocalTime latestDeliveryTime;
	private LocalTime estimatedArrivalTime;

	
	
	public Delivery(int id, int addressNodeId, LocalTime earliestDeliveryTime, LocalTime latestDeliveryTime)
	{
		this.id = id;
		this.addressNodeId = addressNodeId;
		this.earliestDeliveryTime = earliestDeliveryTime;
		this.latestDeliveryTime = latestDeliveryTime;
	}
	
	public int getId() { return id; }
	
	public void setId(int id) { this.id = id; }
	
	public int getAddressNodeId() { return addressNodeId; }
	
	public LocalTime getEarliestDeliveryTime() { return earliestDeliveryTime; }
	
	public LocalTime getlatestDeliveryTime() { return latestDeliveryTime; }
	
	public void setEstimatedArrivalTime(LocalTime time) { this.estimatedArrivalTime = time; }
	public LocalTime getEstimatedArrivalTime() { return estimatedArrivalTime; }
	
	@Override
	public String toString() {
	    String timeStr = (estimatedArrivalTime != null) ? " [Estimated delivery time: " + estimatedArrivalTime + "]" : "";
	    return String.format("Id: %d | Address: %d | Window: %s-%s%s",
	            getId(), getAddressNodeId(), getEarliestDeliveryTime(), getlatestDeliveryTime(), timeStr);
	}
}
