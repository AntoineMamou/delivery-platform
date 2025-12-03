package graph_reader;

import com.delivery.core.model.Graph;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GraphReader {

    private static final Gson gson = new Gson();

    public static Graph readGraph(String resourceName) {
        InputStream is = GraphReader.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) throw new RuntimeException("Resource not found: " + resourceName);
        return gson.fromJson(new InputStreamReader(is), Graph.class);
    }
    
    /*
    public static void main (String[] args)
    {
    	Graph graph = readGraph("graph.json");
    	
    	Node[] nodes = graph.getNodes();
    	Edge[] edges = graph.getEdges();
    	
    	System.out.println("Nodes: ");
    	for (Node node : nodes)
    	{
    		System.out.println("NodeId: " + node.getId() + ", (" + node.getX() + ", " + node.getY() + ")");
    	}
    	
    	System.out.println("Edges: ");
    	for (Edge edge : edges)
    	{
    		System.out.println("From: " + edge.getSourceNodeId() + ", To: " + edge.getTargetNodeId() + ", Length: " + edge.getLength());
    	}
    }
    */
}
