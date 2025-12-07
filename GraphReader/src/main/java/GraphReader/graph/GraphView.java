package GraphReader.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.delivery.core.eventbus.DeliveryAddedEvent;
import com.delivery.core.eventbus.EventBus;
import com.delivery.core.model.*;
import graph_reader.GraphReader;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class GraphView extends Pane{

	private final EventBus eventBus = EventBus.getInstance();
	
	private double width, height;
	
	private double containerWidth, containerHeight;
	
	private float CIRCLE_RADIUS = 20;
	
	private Graph graph;
	
	 private final Pane routeLayer;
	 private final Pane edgeLayer;
	 private final Pane nodeLayer;
	 private final Pane labelLayer;
	
	private Text[] nodeLabels;
	private Circle[] nodeCircles;
	private List<Circle> deliveryCircles;
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
		
		routeLayer = new Pane();
		edgeLayer  = new Pane();
		nodeLayer  = new Pane();
		labelLayer = new Pane();
		
		routeLayer.setMouseTransparent(true);
		edgeLayer.setMouseTransparent(true);
		labelLayer.setMouseTransparent(true);

		
		deliveryCircles = new ArrayList<>();
		
		setPrefSize(width, height);
		
		InitMouseEvents();
		
		EventBus.getInstance().subscribe(DeliveryAddedEvent.class, event -> {
		    markNodeHasDelivery(event.nodeId());
		});
	}
	public void setGraph(Graph graph) { this.graph = graph; }
	
	public Graph getGraph() { return this.graph; }
	
	public void setContainerWidth(double width) { containerWidth = width; }
	
	public void setContainerHeight(double height) { containerHeight = height; }
	
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
        	if (deliveryCircles.contains(nodeCircles[selectedNodeId.get()])) nodeCircles[selectedNodeId.get()].setFill(Color.GOLD);
        	else nodeCircles[selectedNodeId.get()].setFill(Color.BLACK);
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

        double factor = (event.getDeltaY() > 0) ? 1.12 : 1 / 1.12;
        double newScale = getScaleX() * factor;

        Point2D mouseInLocalBefore = sceneToLocal(event.getSceneX(), event.getSceneY());

        setScaleX(newScale); setScaleY(newScale);

        Point2D mouseInSceneAfter = localToScene(mouseInLocalBefore);

        double deltaX = event.getSceneX() - mouseInSceneAfter.getX();
        double deltaY = event.getSceneY() - mouseInSceneAfter.getY();

        setTranslateX(getTranslateX() + deltaX); setTranslateY(getTranslateY() + deltaY);
    }
    
    public void markNodeHasDelivery(int nodeId)
    {
    	deliveryCircles.add(nodeCircles[nodeId]);
    	
    	nodeCircles[nodeId].setFill(Color.GOLD);
    }
    
    public void setWarehouseNode() {
        if (selectedNodeId.get() != -1 && !deliveryCircles.contains(nodeCircles[selectedNodeId.get()])) {
            // Set the old warehouseNode back to black
            nodeCircles[warehouseNodeId.get()].setFill(Color.BLACK);

            nodeCircles[selectedNodeId.get()].setFill(Color.RED);
            warehouseNodeId.setValue(selectedNodeId.get());
            selectedNodeId.setValue(-1);
        }
    }
    
    public void displayRoutes(List<Route> routes)
    {
    	routeLayer.getChildren().clear();
    	
    	float lineWidth = CIRCLE_RADIUS / routes.size();
    	
    	for (int i = 0; i < routes.size(); i ++)
    	{
    		Color routeColor = colorForRoute(i, routes.size());
    		
    		double offset = (i - (routes.size() - 1) / 2.0) * lineWidth;
    		
    		List<Integer> route = routes.get(i).getPath();
    		
    		List<Line> lines = new ArrayList<>();
    		 
    		for (int d = 0; d < route.size() - 1; d++)
    		{
    			Circle c1 = nodeCircles[route.get(d)];
    			Circle c2 = nodeCircles[route.get(d + 1)];
    			
    			Line line = new Line();
    			line.startXProperty().bind(
    	                c1.centerXProperty().add(offsetX(c1, c2, offset))
    	            );
    	            line.startYProperty().bind(
    	                c1.centerYProperty().add(offsetY(c1, c2, offset))
    	            );
    	            line.endXProperty().bind(
    	                c2.centerXProperty().add(offsetX(c1, c2, offset))
    	            );
    	            line.endYProperty().bind(
    	                c2.centerYProperty().add(offsetY(c1, c2, offset))
    	            );
                line.setStrokeWidth(lineWidth);
                line.setStroke(routeColor);
                
                lines.add(line);
    		}
    		routeLayer.getChildren().addAll(lines);
    	}
    }
    
    public void testDisplayRoutes(List<List<Integer>> routes) {

        routeLayer.getChildren().clear();

        float lineWidth = CIRCLE_RADIUS / routes.size();

        for (int i = 0; i < routes.size(); i++) {

            Color routeColor = colorForRoute(i, routes.size());
            double offset = (i - (routes.size() - 1) / 2.0) * lineWidth;
            List<Integer> deliveries = routes.get(i);

            List<Line> lines = new ArrayList<>();

            for (int d = 0; d < deliveries.size() - 1; d++) {

                Circle c1 = nodeCircles[deliveries.get(d)];
                Circle c2 = nodeCircles[deliveries.get(d + 1)];

                Line line = new Line();

                line.startXProperty().bind(
                    c1.centerXProperty().add(offsetX(c1, c2, offset))
                );
                line.startYProperty().bind(
                    c1.centerYProperty().add(offsetY(c1, c2, offset))
                );
                line.endXProperty().bind(
                    c2.centerXProperty().add(offsetX(c1, c2, offset))
                );
                line.endYProperty().bind(
                    c2.centerYProperty().add(offsetY(c1, c2, offset))
                );

                line.setStrokeWidth(lineWidth);
                line.setStroke(routeColor);

                lines.add(line);
            }

            routeLayer.getChildren().addAll(lines);
        }
    }
    
    private DoubleBinding offsetX(Circle c1, Circle c2, double offset) {
        return Bindings.createDoubleBinding(() -> {
            double dx = c2.getCenterX() - c1.getCenterX();
            double dy = c2.getCenterY() - c1.getCenterY();
            double len = Math.hypot(dx, dy);
            if (len == 0) return 0.0;
            return -dy / len * offset;
        }, c1.centerXProperty(), c1.centerYProperty(),
           c2.centerXProperty(), c2.centerYProperty());
    }

    private DoubleBinding offsetY(Circle c1, Circle c2, double offset) {
        return Bindings.createDoubleBinding(() -> {
            double dx = c2.getCenterX() - c1.getCenterX();
            double dy = c2.getCenterY() - c1.getCenterY();
            double len = Math.hypot(dx, dy);
            if (len == 0) return 0.0;
            return dx / len * offset;
        }, c1.centerXProperty(), c1.centerYProperty(),
           c2.centerXProperty(), c2.centerYProperty());
    }
    
    public static Color colorForRoute(int routeIndex, int totalRoutes) {
        double hue = (360.0 / totalRoutes) * routeIndex;
        return Color.hsb(hue, 0.9, 0.9);
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
        nodeLabels = new Text[nodes.size()];

        for (int i = 0; i < nodes.size(); i++) {
            Circle circle = new Circle(
                nodes.get(i).getX(),
                nodes.get(i).getY(),
                CIRCLE_RADIUS,
                Color.BLACK
            );
            nodeCircles[i] = circle;

            Text label = new Text(String.valueOf(i));
            label.setFill(Color.WHITE);
            label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            label.setMouseTransparent(true);

            label.setTextOrigin(VPos.CENTER); 
            
            label.setTextAlignment(TextAlignment.CENTER);

            label.layoutXProperty().bind(
                Bindings.createDoubleBinding(() -> 
                    circle.getCenterX() - (label.getLayoutBounds().getWidth() / 2.0),
                    label.layoutBoundsProperty(),
                    circle.centerXProperty()
                )
            );

            label.layoutYProperty().bind(circle.centerYProperty());

            nodeLabels[i] = label;

            final int index = i;
            circle.setOnMouseClicked(e -> handleNodeClick(index, nodeCircles));
        }

        nodeCircles[warehouseNodeId.get()].setFill(Color.RED);

        return nodeCircles;
    }
    
    public void renderGraph() {

        routeLayer.getChildren().clear();
        edgeLayer.getChildren().clear();
        nodeLayer.getChildren().clear();
        labelLayer.getChildren().clear();

        nodeCircles = createNodeCircles();
        edgeLines   = createEdgeLines();

        edgeLayer.getChildren().addAll(edgeLines.values());
        nodeLayer.getChildren().addAll(nodeCircles);
        labelLayer.getChildren().addAll(nodeLabels);
        
        getChildren().addAll(
        	    edgeLayer,
        	    routeLayer,
        	    nodeLayer,
        	    labelLayer
        	);
    }
    

}
