package user_interface;


import com.delivery.core.eventbus.EventBus;
import com.delivery.core.events.OnOptimizationResult;
import com.delivery.core.model.Graph;
import com.delivery.core.model.events.deliveries.OnUpdatedDeliveries;
import com.delivery.core.model.events.trucks.OnUpdatedTrucks;
import com.delivery.core.ui.DeliveryOptimizerView;

import application.DeliveryOptimizerApp;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import user_interface.components.DeliveriesPanel;
import user_interface.components.DeliverySchedulePanel;
import user_interface.components.GraphView;
import user_interface.components.TrucksPanel;

public class UserInterface extends Application implements DeliveryOptimizerView{
	
	private DeliveryOptimizerApp app;

    public final double SCENE_WIDTH = UI.Sizes.WINDOW_WIDTH;
    public final double SCENE_HEIGHT = UI.Sizes.WINDOW_HEIGHT;

    private IntegerProperty selectedNodeId = new SimpleIntegerProperty(-1);
    private IntegerProperty warehouseNodeId = new SimpleIntegerProperty(0);
    
    private GraphView graphView;
    private Button optimizeDeliveryRouteButton;
    private DeliverySchedulePanel deliverySchedulePanel;
    
    private EventBus eventBus = EventBus.getInstance();
    
    @Override
	public void showOptimizationResult(OnOptimizationResult result) {
    	deliverySchedulePanel.updateSchedule(result);
	}

	@Override
	public void setOptimizeAction(Runnable action) {
		optimizeDeliveryRouteButton.setOnAction(e -> action.run());
	}

	@Override
	public int getWarehouseNodeId() {
		return warehouseNodeId.get();
	}

	@Override
	public Graph getGraph() {
		return graphView.getGraph();
	}

    @Override
    public void start(Stage stage) {	 
    	// Root layout
        BorderPane root = new BorderPane();
        
        //GraphView
    	graphView = new GraphView(EventBus.getInstance(), selectedNodeId, warehouseNodeId);

        StackPane graphContainer = new StackPane();
        graphContainer.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, null)));
        graphContainer.prefWidthProperty().bind(root.widthProperty().multiply(UI.SizeRatios.GRAPH_WIDTH_RATIO));
        graphContainer.prefHeightProperty().bind(root.heightProperty().multiply(UI.SizeRatios.GRAPH_HEIGHT_RATIO));

        // Deliveries and trucks panels
        DeliveriesPanel deliveriesPanel = new DeliveriesPanel(EventBus.getInstance(), selectedNodeId);
        deliveriesPanel.prefWidthProperty().bind(root.widthProperty().multiply(UI.SizeRatios.PANEL_WIDTH_RATIO));
        deliveriesPanel.prefHeightProperty().bind(root.heightProperty().multiply(UI.SizeRatios.PANEL_HEIGHT_RATIO));
        
        TrucksPanel trucksPanel = new TrucksPanel(EventBus.getInstance());
        trucksPanel.prefWidthProperty().bind(root.widthProperty().multiply(UI.SizeRatios.PANEL_WIDTH_RATIO));
        trucksPanel.prefHeightProperty().bind(root.heightProperty().multiply(UI.SizeRatios.PANEL_HEIGHT_RATIO));

        // Warehouse button
        Button setWarehouseNodeButton = new Button("Set warehouse location");
        setWarehouseNodeButton.setOnAction(e -> graphView.setWarehouseNode());
        
        StackPane.setAlignment(setWarehouseNodeButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(setWarehouseNodeButton, UI.InsetsValues.MEDIUM);
        
        graphContainer.getChildren().addAll(graphView, setWarehouseNodeButton);

        //OptimizeRoute button
        optimizeDeliveryRouteButton = new Button("Optimize Delivery Route");
        optimizeDeliveryRouteButton.prefWidthProperty().bind(root.widthProperty().multiply(UI.SizeRatios.BUTTON_WIDTH_RATIO));
        optimizeDeliveryRouteButton.prefHeightProperty().bind(root.heightProperty().multiply(UI.SizeRatios.BUTTON_HEIGHT_RATIO));

        // Bottom HBox for controls
        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox.setPadding(UI.InsetsValues.SMALL);
        controlsBox.getChildren().addAll(deliveriesPanel, trucksPanel, optimizeDeliveryRouteButton);
        controlsBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        HBox.setHgrow(deliveriesPanel, Priority.ALWAYS);
        HBox.setHgrow(trucksPanel, Priority.ALWAYS);

        // DeliverySchedule
        deliverySchedulePanel = new DeliverySchedulePanel();
      
        deliverySchedulePanel.prefWidthProperty().bind(root.widthProperty().multiply(UI.SizeRatios.SCHEDULE_WIDTH_RATIO));
        
        VBox.setVgrow(deliverySchedulePanel, Priority.ALWAYS);

        // Assemble root
        root.setCenter(graphContainer);
        root.setBottom(controlsBox);
        root.setRight(deliverySchedulePanel);

        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.LIGHTGRAY);
        stage.setScene(scene);
        stage.setTitle("Delivery Simulator");
        stage.show();
        
        app = new DeliveryOptimizerApp(eventBus, this);
        app.start();
        
        subscribeToEvents(deliveriesPanel, trucksPanel);
    }

    public static void main(String[] args) {
    	launch();
    }
    
    private void subscribeToEvents(DeliveriesPanel deliveriesPanel, TrucksPanel trucksPanel)
    {
    	eventBus.subscribe(OnUpdatedDeliveries.class, event -> {
		    deliveriesPanel.updateDeliveryList(event.deliveryStrings());
		});
    	
    	eventBus.subscribe(OnUpdatedTrucks.class, event -> {
		    trucksPanel.updateTruckList(event.truckStrings());
		});
    }
}
