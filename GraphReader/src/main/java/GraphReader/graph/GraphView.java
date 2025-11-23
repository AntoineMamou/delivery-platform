package GraphReader.graph;

import java.util.List;

import com.delivery.core.model.Graph;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class GraphView extends Pane{

	
	private double width, height;
	
	private Node[] nodes;
	private Edge[] edges;
	
	private Circle[] nodeCircles;
	private Line[] edgeLines;
	
	private IntegerProperty selectedNodeId;
	private IntegerProperty warehouseNodeId;
	
	private double mouseOldX, mouseOldY;
	
	public GraphView(double width, double height,
        IntegerProperty selectedNodeId,
        IntegerProperty warehouseNodeId) {

		this.width = width;
		this.height = height;
		
		this.selectedNodeId = selectedNodeId;
		this.warehouseNodeId = warehouseNodeId;
		
		setPrefSize(width, height);
		
		InitMouseEvents();
	}

	
	public Circle[] getNodeCircles() { return nodeCircles; }
	
	public Line[] getEdgeLines() { return edgeLines; }
	
	
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
    
    public void renderGraph(Graph graph) {
        getChildren().clear();
        
        //Convert core Nodes to GraphReader Nodes
        List<com.delivery.core.model.Node> coreNodes = graph.nodes();
        this.nodes = new GraphReader.graph.Node[coreNodes.size()];

        for (int i = 0; i < coreNodes.size(); i++) {
            com.delivery.core.model.Node n = coreNodes.get(i);
            this.nodes[i] = new GraphReader.graph.Node(n.id(), n.x(), n.y());
        }
        
        //Convert core Edges to GraphReader Edges
        List<com.delivery.core.model.Edge> coreEdges = graph.edges();
        this.edges = new GraphReader.graph.Edge[coreEdges.size()];

        for (int i = 0; i < coreEdges.size(); i++) {
            com.delivery.core.model.Edge e = coreEdges.get(i);
            this.edges[i] = new GraphReader.graph.Edge(e.from(), e.to(), e.cost());
        }

        nodeCircles = createNodeCircles();
        edgeLines = createEdgeLines();

        getChildren().addAll(edgeLines);
        getChildren().addAll(nodeCircles);
    }
    

}
