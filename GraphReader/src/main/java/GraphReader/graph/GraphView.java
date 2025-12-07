package GraphReader.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.delivery.core.model.*;
import graph_reader.GraphReader;
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
	
	private double containerWidth, containerHeight;
	
	private Graph graph;
	
	private Circle[] nodeCircles;
	private Map<String, Line> edgeLines;
	
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
	public void setGraph(Graph graph) { this.graph = graph; }
	
	public Graph getGraph() { return this.graph; }
	
	public void setContainerWidth(double width) { containerWidth = width; }
	
	public void setContainerHeight(double height) { containerHeight = height; }
	
	public Circle[] getNodeCircles() { return nodeCircles; }
	
	public Map<String, Line> getEdgeLines() { return edgeLines; }
	
	
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
		
		double translationX = getTranslateX() + deltaX;
		double translationY = getTranslateY() + deltaY;
	  
		setTranslateX(translationX);
		setTranslateY(translationY);
	  
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
    
    public void displayPath(int[] path, Color color)
    {
    	for (int i = 0; i < path.length - 1; i++)
    	{
    		String key = path[i] + "_" + path[i + 1];
            Line edgeLine = edgeLines.get(key);
    	
            if (edgeLine != null)
                edgeLine.setStroke(color);
            else 
                System.err.println("Edge not found: " + key);
    	}
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
    
    private Map<String, Line> createEdgeLines() {
    	List<Edge> edges = graph.getEdges();
        Map<String, Line> edgeMap = new HashMap<>();

        for (Edge edge : edges) {
            Line line = createEdgeLine(
                nodeCircles[edge.getSourceNodeId()],
                nodeCircles[edge.getTargetNodeId()]
            );

            String key = edge.getSourceNodeId() + "_" + edge.getTargetNodeId();
            edgeMap.put(key, line);
        }

        return edgeMap;
    }
    
    private Circle[] createNodeCircles()
    {
    	List<Node> nodes = graph.getNodes();
    	nodeCircles = new Circle[nodes.size()];
    	
    	for (int i = 0; i < nodes.size(); i++) {
    	    nodeCircles[i] = new Circle(nodes.get(i).getX(), nodes.get(i).getY(), 20, Color.BLACK);

    	    final int index = i;
    	    nodeCircles[i].setOnMouseClicked(e -> handleNodeClick(index, nodeCircles));
    	}
    	// Set the base warehouseLocation
    	nodeCircles[warehouseNodeId.get()].setFill(Color.RED);
    	
    	return nodeCircles;
    }
    
    public void renderGraph() {
        getChildren().clear();

        nodeCircles = createNodeCircles();
        edgeLines = createEdgeLines();

        getChildren().addAll(edgeLines.values());
        getChildren().addAll(nodeCircles);
    }
    

}
