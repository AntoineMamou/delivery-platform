package GraphReader;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

public class MapCanvas extends Application {
	
	public final int NB_NODES = 20;
	public final int NB_EDGES = 45;

    private double mouseOldX, mouseOldY;

    @Override
    public void start(Stage stage) {
    	
    	// 20 nodes
    	Node[] nodes = new Node[20];

    	// Node coordinates (id, x, y)
    	nodes[0] = new Node(0, 100, 100);
    	nodes[1] = new Node(1, 200, 100);
    	nodes[2] = new Node(2, 300, 100);
    	nodes[3] = new Node(3, 400, 100);
    	nodes[4] = new Node(4, 500, 100);

    	nodes[5] = new Node(5, 100, 200);
    	nodes[6] = new Node(6, 200, 200);
    	nodes[7] = new Node(7, 300, 200);
    	nodes[8] = new Node(8, 400, 200);
    	nodes[9] = new Node(9, 500, 200);

    	nodes[10] = new Node(10, 100, 300);
    	nodes[11] = new Node(11, 200, 300);
    	nodes[12] = new Node(12, 300, 300);
    	nodes[13] = new Node(13, 400, 300);
    	nodes[14] = new Node(14, 500, 300);

    	nodes[15] = new Node(15, 150, 400);
    	nodes[16] = new Node(16, 250, 400);
    	nodes[17] = new Node(17, 350, 400);
    	nodes[18] = new Node(18, 450, 400);
    	nodes[19] = new Node(19, 300, 500);

    	// 45 edges (firstNodeId, secondNodeId, length)
    	Edge[] edges = new Edge[45];
    	edges[0] = new Edge(0, 1, 100);
    	edges[1] = new Edge(1, 2, 100);
    	edges[2] = new Edge(2, 3, 100);
    	edges[3] = new Edge(3, 4, 100);
    	edges[4] = new Edge(0, 5, 100);
    	edges[5] = new Edge(1, 6, 100);
    	edges[6] = new Edge(2, 7, 100);
    	edges[7] = new Edge(3, 8, 100);
    	edges[8] = new Edge(4, 9, 100);
    	edges[9] = new Edge(5, 6, 100);
    	edges[10] = new Edge(6, 7, 100);
    	edges[11] = new Edge(7, 8, 100);
    	edges[12] = new Edge(8, 9, 100);
    	edges[13] = new Edge(5, 10, 100);
    	edges[14] = new Edge(6, 11, 100);
    	edges[15] = new Edge(7, 12, 100);
    	edges[16] = new Edge(8, 13, 100);
    	edges[17] = new Edge(9, 14, 100);
    	edges[18] = new Edge(10, 11, 100);
    	edges[19] = new Edge(11, 12, 100);
    	edges[20] = new Edge(12, 13, 100);
    	edges[21] = new Edge(13, 14, 100);
    	edges[22] = new Edge(10, 15, 120);
    	edges[23] = new Edge(11, 16, 120);
    	edges[24] = new Edge(12, 17, 120);
    	edges[25] = new Edge(13, 18, 120);
    	edges[26] = new Edge(15, 16, 100);
    	edges[27] = new Edge(16, 17, 100);
    	edges[28] = new Edge(17, 18, 100);
    	edges[29] = new Edge(15, 19, 150);
    	edges[30] = new Edge(16, 19, 120);
    	edges[31] = new Edge(17, 19, 120);
    	edges[32] = new Edge(0, 6, 150);
    	edges[33] = new Edge(1, 7, 150);
    	edges[34] = new Edge(2, 8, 150);
    	edges[35] = new Edge(3, 12, 150);
    	edges[36] = new Edge(4, 13, 150);
    	edges[37] = new Edge(5, 11, 120);
    	edges[38] = new Edge(6, 12, 120);
    	edges[39] = new Edge(7, 13, 120);
    	edges[40] = new Edge(8, 14, 120);
    	edges[41] = new Edge(10, 16, 120);
    	edges[42] = new Edge(11, 17, 120);
    	edges[43] = new Edge(12, 18, 120);
    	edges[44] = new Edge(13, 19, 130);
    	
    	Circle[] nodeCircles = new Circle[nodes.length];
    	Line[] edgeLines = new Line[edges.length];
    	
        // Create node circles
    	for (int i = 0; i < nodes.length; i++)
    	{
    		nodeCircles[i] = new Circle (nodes[i].GetX(), nodes[i].GetY(), 20, Color.BLACK);
    	}

        // Create edge lines
    	for (int i = 0; i < edges.length; i++)
    	{
    		edgeLines[i] = new Line();
    		edgeLines[i].startXProperty().bind(nodeCircles[edges[i].GetFirstNodeId()].centerXProperty());
    		edgeLines[i].startYProperty().bind(nodeCircles[edges[i].GetFirstNodeId()].centerYProperty());
    		edgeLines[i].endXProperty().bind(nodeCircles[edges[i].GetSecondNodeId()].centerXProperty());
            edgeLines[i].endYProperty().bind(nodeCircles[edges[i].GetSecondNodeId()].centerYProperty());
            
            edgeLines[i].setStrokeWidth(nodeCircles[0].getRadius()); // line as thick as node diameter
            edgeLines[i].setStroke(Color.WHITE);         // optional: color
    	}

        // Group all nodes and edges
        Group graph = new Group();
        
        for (Line edgeLine : edgeLines)
        {
        	graph.getChildren().add(edgeLine);
        }
        
        for (Circle nodeCircle : nodeCircles)
        {
        	graph.getChildren().add(nodeCircle);
        }

        // --- Pane to hold the graph ---
        Pane root = new Pane(graph);
        Scene scene = new Scene(root, 800, 600, Color.LIGHTGRAY);
        
        // --- Panning ---
		
		  scene.setOnMousePressed((MouseEvent e) -> {
			  mouseOldX = e.getSceneX();
			  mouseOldY = e.getSceneY();
		  });
		 

		
		  scene.setOnMouseDragged((MouseEvent e) -> {
			  double deltaX = e.getSceneX() - mouseOldX;
			  double deltaY = e.getSceneY() - mouseOldY;
		  
			  graph.setTranslateX(graph.getTranslateX() + deltaX);
			  graph.setTranslateY(graph.getTranslateY() + deltaY);
		  
			  mouseOldX = e.getSceneX(); mouseOldY = e.getSceneY(); });
		 

		// true mouse-centered zoom (robust method)
	        scene.setOnScroll((ScrollEvent e) -> {
	            e.consume();

	            // scale factor
	            double factor = (e.getDeltaY() > 0) ? 1.12 : 1 / 1.12;
	            double oldScale = graph.getScaleX();
	            double newScale = oldScale * factor;

	            // 1) compute mouse point in graph-local coordinates BEFORE scaling
	            Point2D mouseInLocalBefore = graph.sceneToLocal(e.getSceneX(), e.getSceneY());

	            // 2) apply new scale
	            graph.setScaleX(newScale);
	            graph.setScaleY(newScale);

	            // 3) find where that same local point now sits in scene coordinates
	            Point2D mouseInSceneAfter = graph.localToScene(mouseInLocalBefore);

	            // 4) compute how much it moved, then shift graph to compensate
	            double deltaX = e.getSceneX() - mouseInSceneAfter.getX();
	            double deltaY = e.getSceneY() - mouseInSceneAfter.getY();

	            graph.setTranslateX(graph.getTranslateX() + deltaX);
	            graph.setTranslateY(graph.getTranslateY() + deltaY);
	        });

        stage.setScene(scene);
        stage.setTitle("Pan & Zoom Graph - True Mouse-Centered Zoom");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
