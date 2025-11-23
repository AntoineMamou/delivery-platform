package GraphReader.graph;

public class Node {
	
	private int id;
	private	double X;
	private	double Y;
		
	public Node(int id, double X, double Y)
	{
		this.id = id;
		this.X = X;
		this.Y = Y;
	}
	
	public int GetId() { return id; }
		
	public double GetX() {return X; }
		
	public double GetY() {return Y; }
		

}
