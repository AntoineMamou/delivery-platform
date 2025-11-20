package GraphReader.graph;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class GraphView extends Pane{

	private final int NB_NODES = 20;
	private final int NB_EDGES = 45;
	
	private double width, height;
	
	private Node[] nodes;
	private Edge[] edges;
	
	private Circle[] nodeCircles;
	private Line[] edgeLines;
	
	private IntegerProperty selectedNodeId;
	private IntegerProperty warehouseNodeId;
	
	private double mouseOldX, mouseOldY;
	
	public GraphView(double width, double height, IntegerProperty selectedNodeId, IntegerProperty warehouseNodeId)
	{
		this.width = width;
		this.height = height;
		
		this.selectedNodeId = selectedNodeId;
		this.warehouseNodeId = warehouseNodeId;
		
		createGraph();
	}
	
	public Circle[] getNodeCircles() { return nodeCircles; }
	
	public Line[] getEdgeLines() { return edgeLines; }
	
	public void createGraph()
	{
    	nodes = createNodes();
    	edges = createEdges();
    	
    	nodeCircles = createNodeCircles();
    	edgeLines = createEdgeLines();
    	
    	setPrefSize(width, height);
        
        getChildren().addAll(edgeLines);
        getChildren().addAll(nodeCircles);
        
        InitMouseEvents();
	}
	
	private void InitMouseEvents()
	{
		setOnMousePressed((MouseEvent e) -> {
			  mouseOldX = e.getSceneX();
			  mouseOldY = e.getSceneY();
		});
		
      setOnMouseDragged((MouseEvent e) -> handleDrag(e));

      setOnScroll((ScrollEvent e) -> handleZoom(e));
	}
	
	private void handleNodeClick(int index, Circle[] nodeCircles)
	{
		if (index == warehouseNodeId.get()) return;
        // Deselect previously selected node if any
        if (selectedNodeId.get() != -1 && selectedNodeId.get() != warehouseNodeId.get()) {
            nodeCircles[selectedNodeId.get()].setFill(Color.BLACK);
        }

        // Toggle selection: if clicking the same node, deselect it
        if (selectedNodeId.get() == index) {
            selectedNodeId.setValue(-1);
        } else {
            selectedNodeId.setValue(index);
            nodeCircles[index].setFill(Color.BLUE);
        }
	}
	
	private void handleDrag(MouseEvent event)
    {
    	double deltaX = event.getSceneX() - mouseOldX;
		double deltaY = event.getSceneY() - mouseOldY;
	  
		setTranslateX(getTranslateX() + deltaX);
		setTranslateY(getTranslateY() + deltaY);
	  
		mouseOldX = event.getSceneX(); mouseOldY = event.getSceneY();
    }
    
    private void handleZoom(ScrollEvent event)
    {
    	event.consume();

        // scale factor
        double factor = (event.getDeltaY() > 0) ? 1.12 : 1 / 1.12;
        double oldScale = getScaleX();
        double newScale = oldScale * factor;

        // 1) compute mouse point in graph-local coordinates BEFORE scaling
        Point2D mouseInLocalBefore = sceneToLocal(event.getSceneX(), event.getSceneY());

        // 2) apply new scale
        setScaleX(newScale);
        setScaleY(newScale);

        // 3) find where that same local point now sits in scene coordinates
        Point2D mouseInSceneAfter = localToScene(mouseInLocalBefore);

        // 4) compute how much it moved, then shift graph to compensate
        double deltaX = event.getSceneX() - mouseInSceneAfter.getX();
        double deltaY = event.getSceneY() - mouseInSceneAfter.getY();

        setTranslateX(getTranslateX() + deltaX);
        setTranslateY(getTranslateY() + deltaY);
    }
    
    private Line createEdgeLine(Circle c1, Circle c2) {
        Line line = new Line();
        line.startXProperty().bind(c1.centerXProperty());
        line.startYProperty().bind(c1.centerYProperty());
        line.endXProperty().bind(c2.centerXProperty());
        line.endYProperty().bind(c2.centerYProperty());
        line.setStrokeWidth(c1.getRadius());
        line.setStroke(Color.WHITE);
        return line;
    }
    
    private Line[] createEdgeLines() {
        edgeLines = new Line[edges.length];
        
        for (int i = 0; i < edges.length; i++)
    	{
    		edgeLines[i] = createEdgeLine(nodeCircles[edges[i].GetFirstNodeId()], nodeCircles[edges[i].GetSecondNodeId()]);
    	}
        return edgeLines;
    }
    
    private Circle[] createNodeCircles()
    {
    	nodeCircles = new Circle[nodes.length];
    	
    	for (int i = 0; i < nodes.length; i++) {
    	    nodeCircles[i] = new Circle(nodes[i].GetX(), nodes[i].GetY(), 20, Color.BLACK);

    	    final int index = i;
    	    nodeCircles[i].setOnMouseClicked(e -> handleNodeClick(index, nodeCircles));
    	}
    	// Set the base warehouseLocation
    	nodeCircles[warehouseNodeId.get()].setFill(Color.RED);
    	
    	return nodeCircles;
    }
    
	 private Node[] createNodes()
	    {
	    	Node[] nodes = new Node[NB_NODES];

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
	    	
	    	return nodes;
	    }
	    
	    private Edge[] createEdges()
	    {
	    	Edge[] edges = new Edge[NB_EDGES];
	    	
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
	    	
	    	return edges;
	    }
}
