package GraphReader.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.delivery.core.eventbus.DeliveryAddedEvent;
import com.delivery.core.eventbus.DeliveryDeletedEvent;
import com.delivery.core.eventbus.EventBus;
import com.delivery.core.model.*;

import GraphReader.ui.DeliveryManager;
import graph_reader.GraphReader;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class GraphView extends Pane {

    private double width, height;
    private double containerWidth, containerHeight;
    private float CIRCLE_RADIUS = 20;
    private Graph graph;

    // Layers
    private Pane overlayPane;  
    private Line scaleBar;
    private Label scaleLabel;
    private Line leftTick;
    private Line rightTick;
    
    private final Pane routeLayer;
    private final Pane edgeLayer;
    private final Pane nodeLayer;
    private final Pane labelLayer;

    // NEW: group for zoomable/movable content
    private final Pane contentPane;

    private Text[] nodeLabels;
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

        DeliveryManager.setWarehouseNodeId(warehouseNodeId);

        routeLayer = new Pane();
        edgeLayer  = new Pane();
        nodeLayer  = new Pane();
        labelLayer = new Pane();
        
        routeLayer.setMouseTransparent(true);
		edgeLayer.setMouseTransparent(true);
		labelLayer.setMouseTransparent(true);

        contentPane = new Pane();
        contentPane.getChildren().addAll(edgeLayer, routeLayer, nodeLayer, labelLayer);
        getChildren().add(contentPane);

        overlayPane = new Pane();
        overlayPane.setMouseTransparent(true);
        overlayPane.setPickOnBounds(false);
        getChildren().add(overlayPane);

        setPrefSize(width, height);

        InitMouseEvents();
        createLegend();
        
        updateScaleBar(1);

        EventBus.getInstance().subscribe(DeliveryAddedEvent.class, event -> updateDeliveryNodes());
        EventBus.getInstance().subscribe(DeliveryDeletedEvent.class, event -> updateDeliveryNodes());
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
        	if (isDeliveryNode(selectedNodeId.get())) nodeCircles[selectedNodeId.get()].setFill(Color.GOLD);
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
	
	private void handleDrag(MouseEvent event) {

        double deltaX = event.getSceneX() - mouseOldX;
        double deltaY = event.getSceneY() - mouseOldY;

        contentPane.setTranslateX(contentPane.getTranslateX() + deltaX);
        contentPane.setTranslateY(contentPane.getTranslateY() + deltaY);

        mouseOldX = event.getSceneX();
        mouseOldY = event.getSceneY();
    }
    
	private void handleZoom(ScrollEvent event) {
        double factor = (event.getDeltaY() > 0) ? 1.12 : 1 / 1.12;
        double newScale = contentPane.getScaleX() * factor;

        Point2D mouseInLocalBefore = contentPane.sceneToLocal(event.getSceneX(), event.getSceneY());
        contentPane.setScaleX(newScale);
        contentPane.setScaleY(newScale);
        Point2D mouseInSceneAfter = contentPane.localToScene(mouseInLocalBefore);

        double deltaX = event.getSceneX() - mouseInSceneAfter.getX();
        double deltaY = event.getSceneY() - mouseInSceneAfter.getY();

        contentPane.setTranslateX(contentPane.getTranslateX() + deltaX);
        contentPane.setTranslateY(contentPane.getTranslateY() + deltaY);

        updateScaleBar(newScale);
    }
    
	private void createLegend() {
	    scaleBar = new Line();
	    scaleBar.setStrokeWidth(3);
	    scaleBar.setStroke(Color.BLACK);

	    leftTick = new Line();
	    leftTick.setStrokeWidth(3);
	    leftTick.setStroke(Color.BLACK);

	    rightTick = new Line();
	    rightTick.setStrokeWidth(3);
	    rightTick.setStroke(Color.BLACK);

	    scaleLabel = new Label();
	    scaleLabel.setStyle("-fx-text-fill: black; -fx-font-size: 12;");

	    overlayPane.getChildren().addAll(scaleBar, leftTick, rightTick, scaleLabel);
	}
    
    private void updateScaleBar(double zoom) {
        double pixelLength = 100; // fixed on screen
        double realDistanceKm = pixelLength / zoom / 100.0; // adjust for zoom

        if (realDistanceKm < 1) {
            scaleLabel.setText(String.format("%.0f m", realDistanceKm * 1000));
        } else {
            scaleLabel.setText(String.format("%.3f km", realDistanceKm));
        }
    }
    
    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        double padding = 15;
        double x = padding;
        double y = getHeight() - padding;
        double length = 100; // fixed pixel length

        // Main horizontal line
        scaleBar.setStartX(x);
        scaleBar.setStartY(y);
        scaleBar.setEndX(x + length);
        scaleBar.setEndY(y);

        // Left tick
        leftTick.setStartX(x);
        leftTick.setStartY(y - 5);
        leftTick.setEndX(x);
        leftTick.setEndY(y + 5);

        // Right tick
        rightTick.setStartX(x + length);
        rightTick.setStartY(y - 5);
        rightTick.setEndX(x + length);
        rightTick.setEndY(y + 5);

        // Label
        scaleLabel.setLayoutX(x + length / 2 - scaleLabel.getWidth() / 2);
        scaleLabel.setLayoutY(y - 20);
    }
    
    public void updateDeliveryNodes()
    {
    	List<Delivery> deliveries = DeliveryManager.getDeliveries();
    	
    	for (int i = 0; i < nodeCircles.length; i++)
    	{
    		if (i == selectedNodeId.get()) nodeCircles[i].setFill(Color.BLUE);
    		else if (i == warehouseNodeId.get()) nodeCircles[i].setFill(Color.RED);
    		else nodeCircles[i].setFill(Color.BLACK);
    	}
    	
    	for (Delivery delivery : deliveries)
    	{
    		nodeCircles[delivery.getAddressNodeId()].setFill(Color.GOLD);
    	}
    }
    
    public boolean isDeliveryNode(int index)
    {
    	for (Delivery delivery : DeliveryManager.getDeliveries())
    	{
    		if (delivery.getAddressNodeId() == index) return true;
    	}
    	
    	return false;
    }
    
    public void setWarehouseNode() {
        if (selectedNodeId.get() != -1 && !isDeliveryNode(selectedNodeId.get())) {
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
            circle.setOnMouseClicked(e -> {
            	handleNodeClick(index, nodeCircles);
            	e.consume();
            });
        }

        nodeCircles[warehouseNodeId.get()].setFill(Color.RED);

        return nodeCircles;
    }
    
    public void renderGraph() {
        // Keep everything as before, just add layers to contentPane instead of this
        routeLayer.getChildren().clear();
        edgeLayer.getChildren().clear();
        nodeLayer.getChildren().clear();
        labelLayer.getChildren().clear();

        nodeCircles = createNodeCircles();
        edgeLines = createEdgeLines();

        edgeLayer.getChildren().addAll(edgeLines.values());
        nodeLayer.getChildren().addAll(nodeCircles);
        labelLayer.getChildren().addAll(nodeLabels);

        // Add layers to contentPane (not 'this')
        contentPane.getChildren().setAll(edgeLayer, routeLayer, nodeLayer, labelLayer);
    }
    

}
