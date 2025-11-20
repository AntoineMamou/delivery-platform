package GraphReader;

public class Truck {
	private int id;
	private int deliveryCapacity;
	private float maxDistance;
	
	public Truck(int id, int deliveryCapacity, float maxDistance)
	{
		this.id = id;
		this.deliveryCapacity = deliveryCapacity;
		this.maxDistance = maxDistance;
	}
	
	public int getId() { return id; }
	
	public int getDeliveryCapacity() { return deliveryCapacity; }
	
	public float getMaxDistance() { return maxDistance; }
	
	public String toString()
	{
		return String.format("Id: %d | Delivery capacity: %d | Max distance: %.2f",
                id, deliveryCapacity, maxDistance);
	}
}
