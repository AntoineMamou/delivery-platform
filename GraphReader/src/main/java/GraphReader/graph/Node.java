package GraphReader.graph;

public class Node {
	
	private int id;
	private	float X;
	private	float Y;
		
	public Node(int id, float X, float Y)
	{
		this.id = id;
		this.X = X;
		this.Y = Y;
	}
	
	public int GetId() { return id; }
		
	public float GetX() {return X; }
		
	public float GetY() {return Y; }
		

}
