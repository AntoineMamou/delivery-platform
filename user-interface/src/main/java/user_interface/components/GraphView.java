package user_interface.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.delivery.core.managers.DeliveryManager;
import com.delivery.core.model.Delivery;
import com.delivery.core.model.Edge;
import com.delivery.core.model.Graph;
import com.delivery.core.model.Node;
import com.delivery.core.model.Route;
import com.delivery.core.model.events.deliveries.OnUpdatedDeliveries;
import com.delivery.core.model.events.graph.OnGraphLoaded;
import com.delivery.core.eventbus.EventBus;
import com.delivery.core.events.OnOptimizationResult;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
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
import user_interface.UI;

public class GraphView extends Pane {
	private Graph graph;

	private Pane overlayPane;
	private Line scaleBar;
	private Label scaleLabel;
	private Line leftTick;
	private Line rightTick;

	private Pane routeLayer;
	private Pane edgeLayer;
	private Pane nodeLayer;
	private Pane labelLayer;

	private Pane contentPane;

	private List<Text> nodeLabels;
	private List<Circle> nodeCircles;
	private Map<String, Line> edgeLines;

	private IntegerProperty selectedNodeId;
	private IntegerProperty warehouseNodeId;

	private double mouseOldX, mouseOldY;
	
	private interface Direction {
	    double apply(double dx, double dy);
	}

	public GraphView(EventBus eventBus, IntegerProperty selectedNodeId, IntegerProperty warehouseNodeId) {

		this.selectedNodeId = selectedNodeId;
		this.warehouseNodeId = warehouseNodeId;

		DeliveryManager.setWarehouseNodeId(warehouseNodeId.get());

		createLayers();

		initMouseEvents();
		createLegend();

		updateScaleBar(1);

		eventBus.subscribe(OnUpdatedDeliveries.class, event -> updateDeliveryNodes());
		
		eventBus.subscribe(OnGraphLoaded.class, event -> {
            setGraph(event.graph());
            renderGraph();
        });
		
		eventBus.subscribe(OnOptimizationResult.class, result -> {
			displayRoutes(result.routes());
		});
	}
	
	public Graph getGraph() {
		return this.graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}
	
	public void setWarehouseNode() {
		if (selectedNodeId.get() != -1 && !isDeliveryNode(selectedNodeId.get())) {
			// Set the old warehouseNode back to black
			nodeCircles.get(warehouseNodeId.get()).setFill(Color.BLACK);

			nodeCircles.get(selectedNodeId.get()).setFill(Color.RED);
			warehouseNodeId.setValue(selectedNodeId.get());
			DeliveryManager.setWarehouseNodeId(warehouseNodeId.get());
			selectedNodeId.setValue(-1);
		}
	}
	
	private List<Line> createRoute(List<Integer> route, Color routeColor, double lineWidth, double offset) {
		List<Line> lines = new ArrayList<>();

		for (int d = 0; d < route.size() - 1; d++) {
			Circle c1 = nodeCircles.get(route.get(d));
			Circle c2 = nodeCircles.get(route.get(d + 1));

			Line line = new Line();
			line.startXProperty().bind(c1.centerXProperty().add(offsetX(c1, c2, offset)));
			line.startYProperty().bind(c1.centerYProperty().add(offsetY(c1, c2, offset)));
			line.endXProperty().bind(c2.centerXProperty().add(offsetX(c1, c2, offset)));
			line.endYProperty().bind(c2.centerYProperty().add(offsetY(c1, c2, offset)));
			line.setStrokeWidth(lineWidth);
			line.setStroke(routeColor);

			lines.add(line);
		}

		return lines;
	}

	public void displayRoutes(List<Route> routes) {
		routeLayer.getChildren().clear();

		double routeLineWidth = UI.GraphView.CIRCLE_RADIUS / routes.size();

		for (int i = 0; i < routes.size(); i++) {
			Color routeColor = colorForRoute(i, routes.size());

			double routeOffset = calculateRouteOffset(i, routes.size(), routeLineWidth);

			List<Integer> route = routes.get(i).getPath();

			List<Line> routeLines = createRoute(route, routeColor, routeLineWidth, routeOffset);

			routeLayer.getChildren().addAll(routeLines);
		}
	}
	
	private double calculateRouteOffset(int index, int nbRoutes, double routeLineWidth)
	{
		return (index - (nbRoutes - 1) / 2.0) * routeLineWidth;
	}
	
	private static Color colorForRoute(int routeIndex, int totalRoutes) {
		double hue = (360.0 / totalRoutes) * routeIndex;
		return Color.hsb(hue, UI.GraphView.ROUTE_SATURATION, UI.GraphView.ROUTE_BRIGHTNESS);
	}
	
	private void updateDeliveryNodes() {
		List<Delivery> deliveries = DeliveryManager.getDeliveries();

		resetNodeColors();
		colorDeliveryNodes(deliveries);
	}

	private void resetNodeColors() {
		for (int i = 0; i < nodeCircles.size(); i++) {
			if (i == selectedNodeId.get())
				nodeCircles.get(i).setFill(Color.BLUE);
			else if (i == warehouseNodeId.get())
				nodeCircles.get(i).setFill(Color.RED);
			else
				nodeCircles.get(i).setFill(Color.BLACK);
		}
	}

	private void colorDeliveryNodes(List<Delivery> deliveries) {
		for (Delivery delivery : deliveries) {
			nodeCircles.get(delivery.getAddressNodeId()).setFill(Color.GOLD);
		}
	}

	private void updateScaleBar(double zoom) {
		double pixelLength = UI.GraphView.SCALE_BAR_LENGTH;
		// Adjust distance with zoom
		double realDistanceKm = pixelLength / zoom * UI.GraphView.KM_PER_PIXEL;

		if (realDistanceKm < 1) {
			scaleLabel.setText(String.format("%.0f m", realDistanceKm * 1000));
		} else {
			scaleLabel.setText(String.format("%.3f km", realDistanceKm));
		}
	}

	private boolean isDeliveryNode(int index) {
		for (Delivery delivery : DeliveryManager.getDeliveries()) {
			if (delivery.getAddressNodeId() == index)
				return true;
		}

		return false;
	}

	private DoubleBinding offsetX(Circle c1, Circle c2, double offset) {
	    return createOffsetBinding(c1, c2, offset, (dx, dy) -> -dy);
	}

	private DoubleBinding offsetY(Circle c1, Circle c2, double offset) {
	    return createOffsetBinding(c1, c2, offset, (dx, dy) -> dx);
	}

	private DoubleBinding createOffsetBinding(Circle c1, Circle c2, double offset, Direction direction) {
	    return Bindings.createDoubleBinding(() -> {
	        double dx = c2.getCenterX() - c1.getCenterX();
	        double dy = c2.getCenterY() - c1.getCenterY();
	        double len = Math.hypot(dx, dy);
	        if (len == 0) return 0.0;
	        return direction.apply(dx, dy) / len * offset;
	    }, c1.centerXProperty(), c1.centerYProperty(), c2.centerXProperty(), c2.centerYProperty());
	}
	
	private void handleNodeClick(int index, List<Circle> nodeCircles) {
		if (index == warehouseNodeId.get())
			return;
		// Deselect previously selected node if any
		if (selectedNodeId.get() != -1 && selectedNodeId.get() != warehouseNodeId.get()) {
			if (isDeliveryNode(selectedNodeId.get()))
				nodeCircles.get(selectedNodeId.get()).setFill(Color.GOLD);
			else
				nodeCircles.get(selectedNodeId.get()).setFill(Color.BLACK);
		}

		// Toggle selection: if clicking the same node, deselect it
		if (selectedNodeId.get() == index) {
			selectedNodeId.setValue(-1);
		} else {
			selectedNodeId.setValue(index);
			nodeCircles.get(index).setFill(Color.BLUE);
		}
	}

	private void handleDrag(MouseEvent event) {
		translateContent(event.getSceneX() - mouseOldX, event.getSceneY() - mouseOldY);

		mouseOldX = event.getSceneX();
		mouseOldY = event.getSceneY();
	}

	private void handleZoom(ScrollEvent event) {
		double factor = (event.getDeltaY() > 0) ? 1.12 : 1 / 1.12;
		double newScale = contentPane.getScaleX() * factor;
		
		contentPane.setScaleX(newScale);
		contentPane.setScaleY(newScale);

		Point2D mouseInLocalBefore = contentPane.sceneToLocal(event.getSceneX(), event.getSceneY());
		
		Point2D mouseInSceneAfter = contentPane.localToScene(mouseInLocalBefore);

		translateContent(event.getSceneX() - mouseInSceneAfter.getX(), event.getSceneY() - mouseInSceneAfter.getY());

		updateScaleBar(newScale);
	}
	
	private void translateContent(double deltaX, double deltaY)
	{
		contentPane.setTranslateX(contentPane.getTranslateX() + deltaX);
		contentPane.setTranslateY(contentPane.getTranslateY() + deltaY);
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
			Line line = createEdgeLine(nodeCircles.get(edge.getSourceNodeId()), nodeCircles.get(edge.getTargetNodeId()));

			String key = edge.getSourceNodeId() + "_" + edge.getTargetNodeId();
			edgeMap.put(key, line);
		}

		return edgeMap;
	}
	
	private Circle createNodeCircle(Node node)
	{
		Circle circle = new Circle(node.getX(), node.getY(), UI.GraphView.CIRCLE_RADIUS,
				Color.BLACK);
		
		final int index = node.getId();
		circle.setOnMouseClicked(e -> {
			handleNodeClick(index, nodeCircles);
			e.consume();
		});
		
		return circle;
	}
	
	private Text createNodeLabel(Circle circle, int index)
	{
		Text label = new Text(String.valueOf(index));
		label.setFill(Color.WHITE);
		label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		label.setMouseTransparent(true);

		label.setTextOrigin(VPos.CENTER);

		label.setTextAlignment(TextAlignment.CENTER);

		label.layoutXProperty()
				.bind(Bindings.createDoubleBinding(
						() -> circle.getCenterX() - (label.getLayoutBounds().getWidth() / 2.0),
						label.layoutBoundsProperty(), circle.centerXProperty()));

		label.layoutYProperty().bind(circle.centerYProperty());

		return label;
	}

	private List<Circle> createNodeCircles() {
		List<Node> nodes = graph.getNodes();
		List<Circle> nodeCircles = new ArrayList<>();

		for (int i = 0; i < nodes.size(); i++) {
			Circle circle = createNodeCircle(nodes.get(i));
			nodeCircles.add(circle);
		}

		return nodeCircles;
	}
	
	private List<Text> createNodeLabels()
	{
		List<Node> nodes = graph.getNodes();
		List<Text> nodeLabels = new ArrayList<>();

		for (int i = 0; i < nodes.size(); i++) {
			Text label = createNodeLabel(nodeCircles.get(i), i);
			nodeLabels.add(label);
		}

		return nodeLabels;
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

	public void renderGraph() {
		nodeCircles = createNodeCircles();
		nodeLabels = createNodeLabels();
		edgeLines = createEdgeLines();

		edgeLayer.getChildren().setAll(edgeLines.values());
		nodeLayer.getChildren().setAll(nodeCircles);
		labelLayer.getChildren().setAll(nodeLabels);

		contentPane.getChildren().setAll(edgeLayer, routeLayer, nodeLayer, labelLayer);
		
		updateDeliveryNodes();
	}
	
	private void initMouseEvents() {
		setOnMousePressed((MouseEvent e) -> {
			mouseOldX = e.getSceneX();
			mouseOldY = e.getSceneY();
		});

		setOnMouseDragged((MouseEvent e) -> handleDrag(e));

		setOnScroll((ScrollEvent e) -> handleZoom(e));
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();

		double x = UI.GraphView.SCALE_BAR_PADDING;
		double y = getHeight() - UI.GraphView.SCALE_BAR_PADDING;
		double length = UI.GraphView.SCALE_BAR_LENGTH;

		// Main horizontal line
		scaleBar.setStartX(x);
		scaleBar.setStartY(y);
		scaleBar.setEndX(x + length);
		scaleBar.setEndY(y);

		// Left tick
		leftTick.setStartX(x);
		leftTick.setStartY(y - UI.GraphView.SCALE_TICK_SIZE);
		leftTick.setEndX(x);
		leftTick.setEndY(y + UI.GraphView.SCALE_TICK_SIZE);

		// Right tick
		rightTick.setStartX(x + length);
		rightTick.setStartY(y - UI.GraphView.SCALE_TICK_SIZE);
		rightTick.setEndX(x + length);
		rightTick.setEndY(y + UI.GraphView.SCALE_TICK_SIZE);

		// Label
		scaleLabel.setLayoutX(x + length / 2 - scaleLabel.getWidth() / 2);
		scaleLabel.setLayoutY(y - UI.GraphView.SCALE_LABEL_PADDING);
	}

	private void createLayers() {
		routeLayer = new Pane();
		edgeLayer = new Pane();
		nodeLayer = new Pane();
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
	}

}
