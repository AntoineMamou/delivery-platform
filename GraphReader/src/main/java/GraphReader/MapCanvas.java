package GraphReader;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.util.converter.NumberStringConverter;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MapCanvas extends Application {
	
	public final int NB_NODES = 20;
	public final int NB_EDGES = 45;
	
	public final int SCENE_WIDTH = 1500;
	public final int SCENE_HEIGHT = 800;
	
	public IntegerProperty selectedNodeId = new SimpleIntegerProperty(-1);
	public int warehouseNodeId = 19;
	
	public ArrayList<Delivery> deliveries = new ArrayList<>();
	public ArrayList<Truck> trucks = new ArrayList<>();

    private double mouseOldX, mouseOldY;

    @Override
    public void start(Stage stage) {
    	
    	// 20 nodes
    	Node[] nodes = createNodes();

    	// 45 edges
    	Edge[] edges = createEdges();
    	
    	Circle[] nodeCircles = new Circle[nodes.length];
    	Line[] edgeLines = new Line[edges.length];
    	
        // Create node circles
    	for (int i = 0; i < nodes.length; i++) {
    	    nodeCircles[i] = new Circle(nodes[i].GetX(), nodes[i].GetY(), 20, Color.BLACK);

    	    final int index = i;
    	    nodeCircles[i].setOnMouseClicked(e -> {
    	    	if (index == warehouseNodeId) return;
    	        // Deselect previously selected node if any
    	        if (selectedNodeId.get() != -1 && selectedNodeId.get() != warehouseNodeId) {
    	            nodeCircles[selectedNodeId.get()].setFill(Color.BLACK);
    	        }

    	        // Toggle selection: if clicking the same node, deselect it
    	        if (selectedNodeId.get() == index) {
    	            selectedNodeId.setValue(-1);
    	        } else {
    	            selectedNodeId.setValue(index);
    	            nodeCircles[index].setFill(Color.BLUE);
    	        }
    	    });
    	}
    	// Set the base warehouseLocation
    	nodeCircles[warehouseNodeId].setFill(Color.RED);

        // Create edge lines
    	for (int i = 0; i < edges.length; i++)
    	{
    		edgeLines[i] = createEdge(nodeCircles[edges[i].GetFirstNodeId()], nodeCircles[edges[i].GetSecondNodeId()]);
    	}
    	
        // Group all nodes and edges
        Pane graphPane = new Pane();
        graphPane.setPrefWidth(SCENE_WIDTH * 0.8);   // ~4/5 width
        graphPane.setPrefHeight(SCENE_HEIGHT * 0.66); // ~2/3 height
        
        graphPane.getChildren().addAll(edgeLines);
        graphPane.getChildren().addAll(nodeCircles);
        
        // Handling graph/map mouse events
        graphPane.setOnMousePressed((MouseEvent e) -> {
			  mouseOldX = e.getSceneX();
			  mouseOldY = e.getSceneY();
		});
		
        graphPane.setOnMouseDragged((MouseEvent e) -> handleDrag(e, graphPane));

        graphPane.setOnScroll((ScrollEvent e) -> handleZoom(e, graphPane));
        
        // UI Elements
        
        VBox deliveriesContainer = createDeliveriesInputs();

        VBox trucksContainer = createTrucksInputs();
        
        
        // Warehouse location input
        Button setWarehouseNodeButton = new Button("Set warehouse location");
        
        setWarehouseNodeButton.setOnAction(e -> {
        	if (selectedNodeId.get() != -1)
        	{	
        		// Set the old warehouseNode back to black
        		nodeCircles[warehouseNodeId].setFill(Color.BLACK);
        			
        		nodeCircles[selectedNodeId.get()].setFill(Color.RED);
        		warehouseNodeId = selectedNodeId.get();
        		selectedNodeId.setValue(-1);
        		
        	}
        });
        
        Button optimizeDeliveryRouteButton = new Button("Optimize Delivery Route");
        
        optimizeDeliveryRouteButton.setOnAction(e -> {
        	System.out.print("Optimizing delivery route");
        });
        
        
        //Final path output
        ListView<String> finalPath = new ListView<>();
        finalPath.getItems().addAll("Delivery B, Truck A", "Delivery C, Truck C", "Delivery A, Truck B");
        
        
        VBox finalPathContainer = new VBox(new Label("Final Path"), finalPath);
        finalPathContainer.setAlignment(Pos.TOP_CENTER);
        finalPathContainer.setPrefSize(SCENE_WIDTH * 0.2, SCENE_HEIGHT * 0.66);
        finalPathContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), null)));
        
        
        // Layout separation
        
        HBox dataEntries = new HBox(10);
        dataEntries.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT - graphPane.getPrefHeight());
        dataEntries.setAlignment(Pos.CENTER_LEFT);
        dataEntries.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), null)));
        dataEntries.getChildren().addAll(deliveriesContainer, trucksContainer, setWarehouseNodeButton, optimizeDeliveryRouteButton);
        
        Pane root = new Pane();
        graphPane.relocate(0, 0);
        finalPathContainer.relocate(graphPane.getPrefWidth(), 0);
        dataEntries.relocate(0, graphPane.getPrefHeight());
        
        root.getChildren().addAll(graphPane, finalPathContainer, dataEntries);
        
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.LIGHTGRAY);
        
        
        //Show the scene
        stage.setScene(scene);
        stage.setTitle("Delivery Simulator");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
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
    	
    	return edges;
    }
    
    private void handleDrag(MouseEvent event, Pane graphPane)
    {
    	double deltaX = event.getSceneX() - mouseOldX;
		double deltaY = event.getSceneY() - mouseOldY;
	  
		graphPane.setTranslateX(graphPane.getTranslateX() + deltaX);
		graphPane.setTranslateY(graphPane.getTranslateY() + deltaY);
	  
		mouseOldX = event.getSceneX(); mouseOldY = event.getSceneY();
    }
    
    private void handleZoom(ScrollEvent event, Pane graphPane)
    {
    	event.consume();

        // scale factor
        double factor = (event.getDeltaY() > 0) ? 1.12 : 1 / 1.12;
        double oldScale = graphPane.getScaleX();
        double newScale = oldScale * factor;

        // 1) compute mouse point in graph-local coordinates BEFORE scaling
        Point2D mouseInLocalBefore = graphPane.sceneToLocal(event.getSceneX(), event.getSceneY());

        // 2) apply new scale
        graphPane.setScaleX(newScale);
        graphPane.setScaleY(newScale);

        // 3) find where that same local point now sits in scene coordinates
        Point2D mouseInSceneAfter = graphPane.localToScene(mouseInLocalBefore);

        // 4) compute how much it moved, then shift graph to compensate
        double deltaX = event.getSceneX() - mouseInSceneAfter.getX();
        double deltaY = event.getSceneY() - mouseInSceneAfter.getY();

        graphPane.setTranslateX(graphPane.getTranslateX() + deltaX);
        graphPane.setTranslateY(graphPane.getTranslateY() + deltaY);
    }
    
    private VBox createDeliveriesInputs() {
        AtomicInteger deliveryCount = new AtomicInteger(0);
        VBox deliveriesContainer = new VBox(5);
        deliveriesContainer.setPrefWidth(600);
        
        Label deliveriesLabel = new Label("Deliveries");
        ListView<String> deliveriesList = new ListView<>();
        deliveriesList.setPrefWidth(450);
        
        ScrollPane deliveriesScroll = new ScrollPane(deliveriesList);
        deliveriesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        deliveriesScroll.setMaxWidth(450);
        
        HBox addDeliveryBox = new HBox(5);
        addDeliveryBox.setAlignment(Pos.CENTER);

        TextField deliveryIdField = new TextField("" + deliveryCount.get());
        deliveryIdField.setPrefWidth(35);
        deliveryIdField.setEditable(false);

        TextField nodeIdField = new TextField();
        nodeIdField.textProperty().bindBidirectional(selectedNodeId, new NumberStringConverter());
        nodeIdField.setPrefWidth(35);
        nodeIdField.setEditable(false);

        TextField earlyTimeField = createTimeField("08:00");
        TextField lateTimeField = createTimeField("17:00");

        Button addDeliveryButton = new Button("Add Delivery");
        addDeliveryButton.setOnAction(e -> {
        	// Data wasn't provided correctly, we dont create a delivery.
            if (earlyTimeField.getText().isEmpty() || lateTimeField.getText().isEmpty() || selectedNodeId.get() == -1) return;
            
            LocalTime earlyTime = getTimeFromString(earlyTimeField.getText());
            LocalTime lateTime = getTimeFromString(lateTimeField.getText());

            Delivery delivery = new Delivery(deliveryCount.get(), selectedNodeId.get(), earlyTime, lateTime);

            deliveryIdField.setText("" + deliveryCount.incrementAndGet());
            deliveriesList.getItems().add(delivery.toString());
                
            deliveries.add(delivery);
            
        });

        addDeliveryBox.getChildren().addAll(
                new Label("Id:"), deliveryIdField,
                new Label("Address:"), nodeIdField,
                new Label("Delivery window:"), earlyTimeField,
                new Label("to"), lateTimeField,
                addDeliveryButton
        );

        deliveriesContainer.getChildren().addAll(deliveriesLabel, deliveriesScroll, addDeliveryBox);
        deliveriesContainer.setAlignment(Pos.TOP_CENTER);
        return deliveriesContainer;
    }
    
    private VBox createTrucksInputs()
    {
    	AtomicInteger truckCount = new AtomicInteger(0);
    	
    	VBox trucksContainer = new VBox(5);
    	trucksContainer.setPrefWidth(450);
    	
    	Label trucksLabel = new Label("Trucks");
    	ListView<String> trucksList = new ListView<>();
        trucksList.setPrefWidth(450);
        
        ScrollPane trucksScroll = new ScrollPane(trucksList);
        trucksScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        trucksScroll.setMaxWidth(450);
        
        HBox addTruckBox = new HBox(5);
        addTruckBox.setAlignment(Pos.CENTER);
        
        TextField truckIdField = new TextField("" + truckCount.get());
        truckIdField.setPrefWidth(35);
        truckIdField.setEditable(false);
        
        TextField deliveryCapacityField = new TextField("10");
        deliveryCapacityField.setPrefWidth(35);
        
        deliveryCapacityField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // only allow digits
            	deliveryCapacityField.setText(oldValue);
            }
        });
        
        TextField maxDistanceField = new TextField("100");
        maxDistanceField.setPrefWidth(35);
        
        maxDistanceField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // only allow digits
            	maxDistanceField.setText(oldValue);
            }
        });
        
        Button addTruckButton = new Button("Add Truck");

        addTruckButton.setOnAction(e -> {
        	// Data wasn't provided correctly, we dont create a delivery.
            if (deliveryCapacityField.getText().isEmpty() || maxDistanceField.getText().isEmpty()) return;
            
            int deliveryCapacity = Integer.parseInt(deliveryCapacityField.getText());
            float maxDistance = Float.parseFloat(maxDistanceField.getText());
            
            if (deliveryCapacity <= 0 || maxDistance <= 0) return;

            Truck truck = new Truck(truckCount.get(), deliveryCapacity, maxDistance);

            truckIdField.setText("" + truckCount.incrementAndGet());
            trucksList.getItems().add(truck.toString());
                
            trucks.add(truck);
        });
        
        addTruckBox.getChildren().addAll(
                new Label("Id:"), truckIdField,
                new Label("Delivery capacity:"), deliveryCapacityField,
                new Label("Max distance (km):"), maxDistanceField,
                addTruckButton
        );
        
        trucksContainer.getChildren().addAll(trucksLabel, trucksScroll, addTruckBox);
        trucksContainer.setAlignment(Pos.TOP_CENTER);
    	return trucksContainer;
    }

    // Helper to create a validated time field
    private TextField createTimeField(String defaultText) {
        TextField textField = new TextField(defaultText);
        textField.setPromptText("HH:mm");
        textField.setPrefWidth(60);

        // Allow partial typing, but restrict to digits + colon
        textField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("([01]?\\d?|2[0-3]?)(:[0-5]?\\d?)?")) textField.setText(oldV);
        });

        // Validate on focus lost
        textField.focusedProperty().addListener((obs, oldF, isFocused) -> {
            if (!isFocused && !textField.getText().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            	textField.setText(defaultText);
            }
        });
        return textField;
    }
    
    private LocalTime getTimeFromString(String string)
    {
    	LocalTime time;
    	
    	if (string.length() < 5) return null;
    	
    	int hour = Integer.parseInt(string.substring(0, 2));
        int minute = Integer.parseInt(string.substring(3, 5));
            
        time = LocalTime.of(hour, minute);
        
        return time;
    	
    }
    
    private Line createEdge(Circle c1, Circle c2) {
        Line line = new Line();
        line.startXProperty().bind(c1.centerXProperty());
        line.startYProperty().bind(c1.centerYProperty());
        line.endXProperty().bind(c2.centerXProperty());
        line.endYProperty().bind(c2.centerYProperty());
        line.setStrokeWidth(c1.getRadius());
        line.setStroke(Color.WHITE);
        return line;
    }
}
