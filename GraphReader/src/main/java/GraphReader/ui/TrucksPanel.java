package GraphReader.ui;

import java.util.ArrayList;

import com.delivery.core.model.Truck;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

public class TrucksPanel extends VBox{
	
	private double width, height;
	
	private ListView<String> trucksList;
	
	private ArrayList<Truck> trucks = new ArrayList<>();
	
	public TrucksPanel(double width, double height)
	{
		this.width = width;
		this.height = height;
		
		createTrucksPanel();
	}
	
	private void createTrucksPanel()
	{	
    	setPrefSize(width, height);
    	
    	VBox truckListBox = createTruckListBox();
        
        HBox truckInputBox = createTruckInputBox();
        
        getChildren().addAll(truckListBox, truckInputBox);
        setAlignment(Pos.TOP_CENTER);
	}
	
	private VBox createTruckListBox()
	{
		VBox truckListBox = new VBox();
		
    	trucksList = new ListView<>();
        trucksList.setPrefWidth(width);
        
        ScrollPane trucksScroll = new ScrollPane(trucksList);
        trucksScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        trucksScroll.setMaxWidth(width);
        
        truckListBox.getChildren().addAll(new Label("Trucks"), trucksScroll);
        
        return truckListBox;
	}
	
	private HBox createTruckInputBox()
	{
		IntegerProperty truckCount = new SimpleIntegerProperty(0);
		
		HBox truckInputBox = new HBox(5);
		truckInputBox.setAlignment(Pos.CENTER);
		
		TextField truckIdField = new TextField();
        truckIdField.textProperty().bindBidirectional(truckCount, new NumberStringConverter());
        truckIdField.setPrefWidth(35);
        truckIdField.setEditable(false);
        
        TextField deliveryCapacityField = createInputField("10");
        TextField maxDistanceField = createInputField("100");
        
        Button addTruckButton = new Button("Add Truck");

        addTruckButton.setOnAction(e -> addTruck(truckCount, deliveryCapacityField.getText(), maxDistanceField.getText()));
        
        truckInputBox.getChildren().addAll(
                new Label("Id:"), truckIdField,
                new Label("Delivery capacity:"), deliveryCapacityField,
                new Label("Max distance (km):"), maxDistanceField,
                addTruckButton
        );
        
        return truckInputBox;
	}
	
	private TextField createInputField(String baseValue)
	{
        TextField inputField = new TextField(baseValue);
        inputField.setPrefWidth(35);
        
        inputField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // only allow digits
            	inputField.setText(oldValue);
            }
        });
        
        return inputField;
	}
	
	private void addTruck(IntegerProperty truckCount, String deliveryCapacityString, String maxDistanceString)
	{
		// Data wasn't provided correctly, we dont create a delivery.
        if (deliveryCapacityString.isEmpty() || maxDistanceString.isEmpty()) return;
        
        int deliveryCapacity = Integer.parseInt(deliveryCapacityString);
        float maxDistance = Float.parseFloat(maxDistanceString);
        
        if (deliveryCapacity <= 0 || maxDistance <= 0) return;

        Truck truck = new Truck(truckCount.get(), deliveryCapacity, maxDistance);

        trucksList.getItems().add(truck.toString());
            
        trucks.add(truck);
        
        truckCount.setValue(truckCount.get() + 1);
	}
	
	public ArrayList<Truck> getTrucks() {
        return new ArrayList<>(trucks); // return a copy
    }

}
