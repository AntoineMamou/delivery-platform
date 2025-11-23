package com.delivery.datafetcher;

import java.util.List;

import com.delivery.core.model.Edge;
import com.delivery.core.model.Graph;
import com.delivery.core.model.Node;

public class GraphLoader {

    public Graph load() {


        List<Node> nodes = List.of(
    	new Node(0, 100, 100),
    	new Node(1, 200, 100),
    	new Node(2, 300, 100),
    	new Node(3, 400, 100),
    	new Node(4, 500, 100),

    	new Node(5, 100, 200),
    	new Node(6, 200, 200),
    	new Node(7, 300, 200),
    	new Node(8, 400, 200),
    	new Node(9, 500, 200),

    	new Node(10, 100, 300),
    	new Node(11, 200, 300),
    	new Node(12, 300, 300),
    	new Node(13, 400, 300),
    	new Node(14, 500, 300),

    	new Node(15, 150, 400),
    	new Node(16, 250, 400),
    	new Node(17, 350, 400),
    	new Node(18, 450, 400),
    	new Node(19, 300, 500)
    	);

        List<Edge> edges = List.of(
        	new Edge(0, 1, 100),
        	new Edge(1, 2, 100),
            new Edge(2, 3, 100),
            new Edge(3, 4, 100),
            new Edge(0, 5, 100),
            new Edge(1, 6, 100),
            new Edge(2, 7, 100),
            new Edge(3, 8, 100),
            new Edge(4, 9, 100),
            new Edge(5, 6, 100),
            	
            new Edge(6, 7, 100),
            new Edge(7, 8, 100),
            new Edge(8, 9, 100),
            new Edge(5, 10, 100),
            new Edge(6, 11, 100),
            new Edge(7, 12, 100),
            new Edge(8, 13, 100),
            new Edge(9, 14, 100),
            new Edge(10, 11, 100),
            new Edge(11, 12, 100),
            	
            new Edge(12, 13, 100),
            new Edge(13, 14, 100),
            new Edge(10, 15, 120),
            new Edge(11, 16, 120),
            new Edge(12, 17, 120),
            new Edge(13, 18, 120),
            new Edge(15, 16, 100),
            new Edge(16, 17, 100),
            new Edge(17, 18, 100),
            new Edge(15, 19, 150),
            
            new Edge(16, 19, 120),
            new Edge(17, 19, 120),
            new Edge(0, 6, 150),
            new Edge(1, 7, 150),
            new Edge(2, 8, 150),
            new Edge(3, 12, 150),
            new Edge(4, 13, 150),
            new Edge(5, 11, 120),
            new Edge(6, 12, 120),
            new Edge(7, 13, 120),
            	
            new Edge(8, 14, 120),
            new Edge(10, 16, 120),
            new Edge(11, 17, 120),
            new Edge(12, 18, 120),
            new Edge(13, 19, 130)            
        );

        return new Graph(nodes, edges);
    }
}
