package GraphReader;

import java.util.ArrayList;

import GraphReader.graph.GraphView;
import GraphReader.model.Truck;
import GraphReader.ui.DeliveriesPanel;
import GraphReader.ui.TrucksPanel;
import javafx.application.Application;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MapCanvas extends Application {
	
	public final int SCENE_WIDTH = 1500;
	public final int SCENE_HEIGHT = 800;
	
	private IntegerProperty selectedNodeId = new SimpleIntegerProperty(-1);
	private IntegerProperty warehouseNodeId = new SimpleIntegerProperty(19);

	public ArrayList<Truck> trucks = new ArrayList<>();

    @Override
    public void start(Stage stage) {
    	
    	GraphView graphView = new GraphView(SCENE_WIDTH * 0.8, SCENE_HEIGHT * 0.66, selectedNodeId, warehouseNodeId);
        
        VBox deliveriesContainer = new DeliveriesPanel(SCENE_WIDTH * 0.33, SCENE_HEIGHT * 0.33, selectedNodeId);
        VBox trucksContainer = new TrucksPanel(SCENE_WIDTH * 0.33, SCENE_HEIGHT * 0.33);
       
        // Warehouse location input
        Button setWarehouseNodeButton = new Button("Set warehouse location");
        setWarehouseNodeButton.setOnAction(e -> SetWarehouseNode(graphView));
        
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
        dataEntries.setPrefSize(SCENE_WIDTH, SCENE_HEIGHT - graphView.getPrefHeight());
        dataEntries.setAlignment(Pos.CENTER_LEFT);
        dataEntries.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(0), null)));
        dataEntries.getChildren().addAll(deliveriesContainer, trucksContainer, setWarehouseNodeButton, optimizeDeliveryRouteButton);
        
        Pane root = new Pane();
        graphView.relocate(0, 0);
        finalPathContainer.relocate(graphView.getPrefWidth(), 0);
        dataEntries.relocate(0, graphView.getPrefHeight());
        
        root.getChildren().addAll(graphView, finalPathContainer, dataEntries);
        
        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, Color.LIGHTGRAY);
        
        
        //Show the scene
        stage.setScene(scene);
        stage.setTitle("Delivery Simulator");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
    
    private void SetWarehouseNode(GraphView graphView)
    {
    	if (selectedNodeId.get() != -1)
    	{	
    		// Set the old warehouseNode back to black
    		graphView.getNodeCircles()[warehouseNodeId.get()].setFill(Color.BLACK);
    			
    		graphView.getNodeCircles()[selectedNodeId.get()].setFill(Color.RED);
    		warehouseNodeId.setValue(selectedNodeId.get());
    		selectedNodeId.setValue(-1);
    		
    	}
    }
}
