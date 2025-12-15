package GraphReader.ui;


import com.delivery.core.eventbus.EventBus;
import com.delivery.core.eventbus.TruckAddRequestEvent;
import com.delivery.core.eventbus.TruckAddedEvent;
import com.delivery.core.eventbus.TruckDeletedEvent;

import com.delivery.core.model.Truck;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.converter.NumberStringConverter;

public class TrucksPanel extends VBox {

    private double width, height;

    private ListView<ListItem> trucksList;

    public TrucksPanel(double width, double height) {
        this.width = width;
        this.height = height;

        createTrucksPanel();
        
        EventBus.getInstance().subscribe(TruckAddedEvent.class, event -> {
        	addTruckListItem(event.truck());
		});
		
		EventBus.getInstance().subscribe(TruckDeletedEvent.class, event -> {
		    updateTruckList();
		});
    }
    
    private void updateTruckList()
	{
		trucksList.getItems().clear();
		
		for (Truck truck : TruckManager.getTrucks())
		{
			addTruckListItem(truck);
		}
	}
	
	private void addTruckListItem(Truck truck) {
		ListItem truckItem = new ListItem(truck.toString());
		
		trucksList.getItems().add(truckItem);
	}

    private void createTrucksPanel() {
        setPrefSize(width, height);

        VBox truckListBox = createTruckListBox();
        HBox truckInputBox = createTruckInputBox();

        getChildren().addAll(truckListBox, truckInputBox);
        setAlignment(Pos.TOP_CENTER);
    }

    private VBox createTruckListBox() {
        VBox truckListBox = new VBox(5);
        truckListBox.setAlignment(Pos.TOP_CENTER);

        trucksList = new ListView<ListItem>();
	    
	    trucksList.setCellFactory(param -> new TruckListCell()); 
        trucksList.setPrefWidth(width);

        // Make the ListView expand vertically
        VBox.setVgrow(trucksList, Priority.ALWAYS);

        truckListBox.getChildren().addAll(new Label("Trucks"), trucksList);

        return truckListBox;
    }

    private HBox createTruckInputBox() {
        HBox truckInputBox = new HBox(10);
        truckInputBox.setAlignment(Pos.CENTER);

        // Id field
        HBox idGroup = new HBox(5);
        idGroup.setAlignment(Pos.CENTER_LEFT);
        TextField truckIdField = new TextField();
        truckIdField.textProperty().bindBidirectional(TruckManager.getTruckCount(), new NumberStringConverter());
        truckIdField.setPrefWidth(35);
        truckIdField.setEditable(false);
        idGroup.getChildren().addAll(new Label("Id:"), truckIdField);

        // Delivery capacity field
        HBox capacityGroup = new HBox(5);
        capacityGroup.setAlignment(Pos.CENTER_LEFT);
        TextField deliveryCapacityField = createInputField("10");
        capacityGroup.getChildren().addAll(new Label("Delivery capacity:"), deliveryCapacityField);

        // Max distance field
        HBox distanceGroup = new HBox(5);
        distanceGroup.setAlignment(Pos.CENTER_LEFT);
        TextField maxDistanceField = createInputField("100");
        distanceGroup.getChildren().addAll(new Label("Max distance (km):"), maxDistanceField);

        // Add button
        HBox buttonGroup = new HBox();
        buttonGroup.setAlignment(Pos.CENTER);
        Button addTruckButton = new Button("Add Truck");
        addTruckButton.setOnAction(e -> {
        	EventBus.getInstance().publish(new TruckAddRequestEvent(deliveryCapacityField.getText(), maxDistanceField.getText()));
        	System.out.println("Publishing truck add request event");
        });
        buttonGroup.getChildren().add(addTruckButton);

        truckInputBox.getChildren().addAll(idGroup, capacityGroup, distanceGroup, buttonGroup);

        return truckInputBox;
    }

    private TextField createInputField(String baseValue) {
        TextField inputField = new TextField(baseValue);
        inputField.setPrefWidth(35);

        inputField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // only allow digits
                inputField.setText(oldValue);
            }
        });

        return inputField;
    }
}
