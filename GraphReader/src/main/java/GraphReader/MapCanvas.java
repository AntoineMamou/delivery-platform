package GraphReader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.eventbus.OptimizationRequest;
import com.delivery.core.eventbus.OptimizationResult;
import com.delivery.core.events.FetchGraphRequest;
import com.delivery.core.events.GraphLoadedEvent;
import com.delivery.core.model.Delivery;
import com.delivery.core.model.Route;
import com.delivery.core.model.Truck;
import com.delivery.datafetcher.DataFetcherService;
import com.delivery.optimization.OptimizerService;

import GraphReader.graph.GraphView;
import GraphReader.ui.DeliveriesPanel;
import GraphReader.ui.TrucksPanel;
import graph_reader.GraphReader;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class MapCanvas extends Application {

    public final int SCENE_WIDTH = 1500;
    public final int SCENE_HEIGHT = 800;

    private IntegerProperty selectedNodeId = new SimpleIntegerProperty(-1);
    private IntegerProperty warehouseNodeId = new SimpleIntegerProperty(19);

    public ArrayList<Truck> trucks = new ArrayList<>();

    private final EventBus eventBus = EventBus.getInstance();

    @Override
    public void start(Stage stage) {

        eventBus.subscribe(OptimizationRequest.class, request -> {
            System.out.println("=== Event reçu ===");
            System.out.println("Deliveries: " + request.deliveries().size());
            System.out.println("Trucks: " + request.trucks().size());
        });

    	
    	
    	// Instancier graphView
    	GraphView graphView = new GraphView(SCENE_WIDTH * 0.8, SCENE_HEIGHT * 0.66, selectedNodeId, warehouseNodeId);

        // Root layout
        BorderPane root = new BorderPane();

        StackPane graphContainer = new StackPane(graphView);
        graphContainer.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, null)));
        graphContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.7));
        graphContainer.prefHeightProperty().bind(root.heightProperty().multiply(0.7));
        
        graphView.setContainerWidth(SCENE_WIDTH * 0.7);
        graphView.setContainerHeight(SCENE_HEIGHT * 0.7);
        
        graphContainer.widthProperty().addListener((obs, oldVal, newVal) -> 
        graphView.setContainerWidth(newVal.doubleValue()));
        graphContainer.heightProperty().addListener((obs, oldVal, newVal) -> 
        graphView.setContainerHeight(newVal.doubleValue()));
        
        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(graphContainer.widthProperty());
        clipRect.heightProperty().bind(graphContainer.heightProperty());
        graphContainer.setClip(clipRect);
        
        eventBus.subscribe(GraphLoadedEvent.class, event -> {
            System.out.println("GRAPH: " + event.graph());
            Platform.runLater(() -> 
            {
            	graphView.setGraph(event.graph());
            	graphView.renderGraph();
            });
        });

        DataFetcherService fetcher = new DataFetcherService(eventBus);
        fetcher.start();
        eventBus.publish(new FetchGraphRequest());

        // Deliveries & trucks panels
        VBox deliveriesContainer = new DeliveriesPanel(SCENE_WIDTH * 0.33, SCENE_HEIGHT * 0.33, selectedNodeId);
        VBox trucksContainer = new TrucksPanel(SCENE_WIDTH * 0.33, SCENE_HEIGHT * 0.33);

        // Buttons
        Button setWarehouseNodeButton = new Button("Set warehouse location");
        setWarehouseNodeButton.setOnAction(e -> graphView.setWarehouseNode());
        
        //demarrer le service d'optimisation
    	OptimizerService optimizer = new OptimizerService(eventBus);

        Button optimizeDeliveryRouteButton = new Button("Optimize Delivery Route");
        optimizeDeliveryRouteButton.setOnAction(e -> {
            System.out.print("Optimizing delivery route");
            ArrayList<Delivery> deliveries = ((DeliveriesPanel) deliveriesContainer).getDeliveries();
            ArrayList<Truck> trucks = ((TrucksPanel) trucksContainer).getTrucks();

            OptimizationRequest request = new OptimizationRequest(
                    deliveries,
                    trucks,
                    warehouseNodeId.get(),
                    graphView.getGraph()
            );

            eventBus.publish(request);
        });

        // Bottom HBox for controls
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.setPadding(new Insets(5));
        controlsBox.getChildren().addAll(deliveriesContainer, trucksContainer, setWarehouseNodeButton, optimizeDeliveryRouteButton);
        controlsBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        HBox.setHgrow(deliveriesContainer, Priority.ALWAYS);
        HBox.setHgrow(trucksContainer, Priority.ALWAYS);

        // Final path container (right)
        ListView<String> finalPath = new ListView<>();
        finalPath.getItems().addAll("Delivery B, Truck A", "Delivery C, Truck C", "Delivery A, Truck B");

        VBox finalPathContainer = new VBox(new Label("Final Path"), finalPath);
        finalPathContainer.setAlignment(Pos.TOP_CENTER);
        finalPathContainer.setSpacing(5);
        finalPathContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.3));
        finalPathContainer.prefHeightProperty().bind(root.heightProperty());
        finalPathContainer.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        // Assemble root
        root.setCenter(graphContainer);
        root.setBottom(controlsBox);
        root.setRight(finalPathContainer);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.LIGHTGRAY);
        stage.setScene(scene);
        stage.setTitle("Delivery Simulator");
        stage.show();
     
        eventBus.subscribe(OptimizationResult.class, result -> {
            Platform.runLater(() -> {
                
                // --- VUE 1 : Affichage par Camion (votre affichage actuel) ---
                finalPath.getItems().clear();
                for (Route route : result.routes()) {
                     finalPath.getItems().add("CAMION " + route.getVehicle().getId());
                     for (Delivery d : route.getDeliveries()) {
                         finalPath.getItems().add("  -> " + d.getEstimatedArrivalTime() + " : Colis " + d.getId());
                     }
                }

                // --- VUE 2 : Timeline Globale (pour Antoine) ---
                System.out.println("--- TIMELINE GLOBALE ---");
                for (Delivery d : result.chronologicalDeliveries()) {
                    System.out.println(d.getEstimatedArrivalTime() + " : Livraison " + d.getId() + " (Noeud " + d.getAddressNodeId() + ")");
                }
            });
        
            List<Route> routes = result.routes();
                                   
            graphView.displayRoutes(routes);
        });
        
    }

    public static void main(String[] args) {
    	System.out.println(
    		    GraphReader.class.getClassLoader().getResource("graph.json")
    		);
    	
        launch();
    }
}
