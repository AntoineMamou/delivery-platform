package user_interface.components;


import java.util.List;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.managers.TruckManager;
import com.delivery.core.model.events.trucks.OnTruckAddRequest;
import com.delivery.core.model.events.trucks.OnTruckDeleteRequest;
import com.delivery.core.model.events.trucks.OnUpdatedTrucks;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import user_interface.UI;

public class TrucksPanel extends VBox {

    private ListView<String> trucksList;
    
    private EventBus eventBus;

    public TrucksPanel(EventBus eventBus) {
        
        this.eventBus = eventBus;
        
        this.eventBus.subscribe(OnUpdatedTrucks.class, event -> {
		    updateTruckList(event.truckStrings());
		});
        
        createTrucksPanel();
    }
    
    public void updateTruckList(List<String> truckStrings)
	{
		trucksList.getItems().setAll(truckStrings);
	}

    private void createTrucksPanel() {
        VBox truckListBox = createTruckListBox();
        HBox truckInputBox = createTruckInputBox();

        getChildren().addAll(truckListBox, truckInputBox);
        setAlignment(Pos.TOP_CENTER);
    }

    private VBox createTruckListBox() {
        VBox truckListBox = new VBox(UI.Spacing.SMALL);
        truckListBox.setAlignment(Pos.TOP_CENTER);

        trucksList = new ListView<String>();
        trucksList.setCellFactory(listView ->
        new PanelListCell<String>(index -> 
            EventBus.getInstance().publish(new OnTruckDeleteRequest(index)))
    ); 

        // Make the ListView expand vertically
        VBox.setVgrow(trucksList, Priority.ALWAYS);

        truckListBox.getChildren().addAll(new Label("Trucks"), trucksList);

        return truckListBox;
    }

    private HBox createTruckInputBox() {
        HBox truckInputBox = new HBox(UI.Spacing.MEDIUM);
        truckInputBox.setAlignment(Pos.CENTER);

        // Id field
        HBox idGroup = new HBox(UI.Spacing.SMALL);
        idGroup.setAlignment(Pos.CENTER_LEFT);
        
        TextField truckIdField = new TextField(String.valueOf(TruckManager.getTruckCount()));
        
        // Bind truckIdField to update when trucks are updated
        eventBus.subscribe(OnUpdatedTrucks.class, event -> {
            Platform.runLater(() -> truckIdField.setText(String.valueOf(TruckManager.getTrucks().size())));
        });
        
        truckIdField.setPrefWidth(UI.Sizes.FIELD_SMALL);
        truckIdField.setEditable(false);
        
        idGroup.getChildren().addAll(new Label("Id:"), truckIdField);

        // Delivery capacity field
        HBox capacityGroup = new HBox(UI.Spacing.SMALL);
        capacityGroup.setAlignment(Pos.CENTER_LEFT);
        
        TextField deliveryCapacityField = createInputField("10");
        capacityGroup.getChildren().addAll(new Label("Delivery capacity:"), deliveryCapacityField);

        // Max distance field
        HBox distanceGroup = new HBox(UI.Spacing.SMALL);
        distanceGroup.setAlignment(Pos.CENTER_LEFT);
        
        TextField maxDistanceField = createInputField("100");
        distanceGroup.getChildren().addAll(new Label("Max distance (km):"), maxDistanceField);

        // Add button
        HBox buttonGroup = new HBox();
        buttonGroup.setAlignment(Pos.CENTER);
        
        Button addTruckButton = new Button("Add Truck");
        addTruckButton.setOnAction(e -> {
        	eventBus.publish(new OnTruckAddRequest(deliveryCapacityField.getText(), maxDistanceField.getText()));
        });
        
        buttonGroup.getChildren().add(addTruckButton);

        truckInputBox.getChildren().addAll(idGroup, capacityGroup, distanceGroup, buttonGroup);

        return truckInputBox;
    }

    private TextField createInputField(String baseValue) {
        TextField inputField = new TextField(baseValue);
        inputField.setPrefWidth(UI.Sizes.FIELD_SMALL);

        inputField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // only allow digits
                inputField.setText(oldValue);
            }
        });

        return inputField;
    }
}
