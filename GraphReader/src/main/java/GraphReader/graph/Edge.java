package GraphReader.graph;

public class Edge {
	private	int firstNodeId;
	private	int secondNodeId;
	private double EdgeLength;
		
	public Edge(int firstNodeId, int secondNodeId, double EdgeLength)
	{
		this.firstNodeId = firstNodeId;
		this.secondNodeId = secondNodeId;
		this.EdgeLength = EdgeLength;
	}
	
	public int GetFirstNodeId() { return firstNodeId; }
		
	public int GetSecondNodeId() {return secondNodeId; }
		
	public double GetEdgeLength() {return EdgeLength; }
}
