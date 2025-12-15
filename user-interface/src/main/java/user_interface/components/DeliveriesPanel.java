package user_interface.components;

import java.util.List;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.managers.DeliveryManager;
import com.delivery.core_model.events.deliveries.OnDeliveryAddRequest;
import com.delivery.core_model.events.deliveries.OnDeliveryDeleteRequest;
import com.delivery.core_model.events.deliveries.OnUpdatedDeliveries;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import user_interface.UI;


public class DeliveriesPanel extends VBox{
	
	private IntegerProperty selectedNodeId;
	
	private ListView<String> deliveriesList;
	
	private EventBus eventBus;
	
	public DeliveriesPanel(EventBus eventBus, IntegerProperty selectedNodeId)
	{
		this.selectedNodeId = selectedNodeId;
		this.eventBus = eventBus;
		
		createDeliveriesPanel();
	}
	
	public void updateDeliveryList(List<String> deliveryStrings)
	{
		deliveriesList.getItems().setAll(deliveryStrings);
	}

	private void createDeliveriesPanel() {
        VBox deliveryListBox = createDeliveryListBox();
        
        HBox deliveryInputBox = createDeliveryInputBox();

        getChildren().addAll(deliveryListBox, deliveryInputBox);
        setAlignment(Pos.TOP_CENTER);

    }
	
	private VBox createDeliveryListBox() {
	    VBox deliveriesViewBox = new VBox(UI.Spacing.SMALL);
	    deliveriesViewBox.setAlignment(Pos.TOP_CENTER);

	    deliveriesList = new ListView<String>();
	    
	    deliveriesList.setCellFactory(listView ->
	    new PanelListCell<String>(index -> 
	        EventBus.getInstance().publish(new OnDeliveryDeleteRequest(index)))
	);

	    // Make the ListView expand vertically
	    VBox.setVgrow(deliveriesList, Priority.ALWAYS);

	    deliveriesViewBox.getChildren().addAll(new Label("Deliveries"), deliveriesList);

	    return deliveriesViewBox;
	}
	
	private HBox createDeliveryInputBox() {
	    HBox deliveryInputBox = new HBox(UI.Spacing.MEDIUM);
	    deliveryInputBox.setAlignment(Pos.CENTER);

	    // Id field
	    HBox idGroup = new HBox(UI.Spacing.SMALL);
	    idGroup.setAlignment(Pos.CENTER_LEFT);
	    
	    TextField deliveryIdField = new TextField(String.valueOf(DeliveryManager.getDeliveryCount()));
	    
	    // Bind truckIdField to update when deliveries are updated
	    eventBus.subscribe(OnUpdatedDeliveries.class, event -> {
            Platform.runLater(() -> deliveryIdField.setText(String.valueOf(event.deliveryStrings().size())));
        });
	    
	    deliveryIdField.setPrefWidth(UI.Sizes.FIELD_SMALL);
	    idGroup.getChildren().addAll(new Label("Id:"), deliveryIdField);

	    // Address field
	    HBox addressGroup = new HBox(UI.Spacing.SMALL);
	    addressGroup.setAlignment(Pos.CENTER_LEFT);
	    
	    TextField nodeIdField = createIdField(selectedNodeId);
	    addressGroup.getChildren().addAll(new Label("Address:"), nodeIdField);

	    // Delivery window
	    HBox deliveryWindowGroup = new HBox(UI.Spacing.SMALL);
	    deliveryWindowGroup.setAlignment(Pos.CENTER_LEFT);
	    
	    TextField earlyTimeField = createTimeField("08:00");
	    TextField lateTimeField = createTimeField("17:00");
	    
	    deliveryWindowGroup.getChildren().addAll(new Label("Delivery window:"), earlyTimeField, new Label("to"), lateTimeField);

	    // Add button
	    HBox buttonGroup = new HBox();
	    buttonGroup.setAlignment(Pos.CENTER);
	    
	    Button addDeliveryButton = new Button("Add Delivery");
	    
	    addDeliveryButton.setOnAction(e -> {
	    	eventBus.publish(
	    			new OnDeliveryAddRequest(Integer.parseInt(nodeIdField.getText()), earlyTimeField.getText(), lateTimeField.getText())
	    			);
	    });
	    
	    buttonGroup.getChildren().add(addDeliveryButton);

	    deliveryInputBox.getChildren().addAll(idGroup, addressGroup, deliveryWindowGroup, buttonGroup);

	    return deliveryInputBox;
	}

	// Helper to create a validated time field
    private TextField createTimeField(String defaultText)
    {
        TextField textField = new TextField(defaultText);
        textField.setPromptText("HH:mm");
        textField.setPrefWidth(UI.Sizes.FIELD_LARGE);
        
        // Input validation while typing:
        // Allows:
        //   1–2 digits
        //   optional ":"
        //   optional 1–2 digits
        //
        // Regex: "\\d{0,2}(:\\d{0,2})?"
        //
        // Explanation:
        //   \\d{0,2}       -> up to 2 digits (hours)
        //   (:\\d{0,2})?   -> optional ":" followed by up to 2 digits (minutes)

        textField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("\\d{0,2}(:\\d{0,2})?")) {
                textField.setText(oldV);
            }
        });
        
        return textField;
    }
    
    private TextField createIdField(IntegerProperty boundNumber)
    {
    	TextField idField = new TextField();
    	
    	boundNumber.addListener((obs, oldV, newV) ->
        idField.setText(newV.toString())
    	);

    	idField.setText(String.valueOf(boundNumber.get()));
    	
    	idField.setPrefWidth(UI.Sizes.FIELD_SMALL);
    	return idField;
    }
}
