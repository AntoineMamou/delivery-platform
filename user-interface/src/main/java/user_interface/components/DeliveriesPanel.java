package GraphReader.ui;

import com.delivery.core.eventbus.DeliveryAddRequestEvent;
import com.delivery.core.eventbus.DeliveryAddedEvent;
import com.delivery.core.eventbus.DeliveryDeletedEvent;
import com.delivery.core.eventbus.EventBus;
import com.delivery.core.model.Delivery;

import javafx.beans.property.IntegerProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class DeliveriesPanel extends VBox{
	
	private IntegerProperty selectedNodeId;
	
	private double width, height;
	
	private ListView<ListItem> deliveriesList;
	
	public DeliveriesPanel(double width, double height, IntegerProperty selectedNodeId)
	{
		this.width = width;
		this.height = height;
		this.selectedNodeId = selectedNodeId;
		
		createDeliveriesPanel();
		
		EventBus.getInstance().subscribe(DeliveryAddedEvent.class, event -> {
		    addDeliveryListItem(event.delivery());
		});
		
		EventBus.getInstance().subscribe(DeliveryDeletedEvent.class, event -> {
		    updateDeliveryList();
		});
	}
	
	private void updateDeliveryList()
	{
		deliveriesList.getItems().clear();
		
		for (Delivery delivery : DeliveryManager.getDeliveries())
		{
			addDeliveryListItem(delivery);
		}
	}
	
	private void addDeliveryListItem(Delivery delivery) {
		ListItem deliveryItem = new ListItem(delivery.toString());
		
		deliveriesList.getItems().add(deliveryItem);
	}

	private void createDeliveriesPanel() {
        setPrefSize(width, height);
        
        VBox deliveryListBox = createDeliveryListBox();
        
        HBox deliveryInputBox = createDeliveryInputBox();

        getChildren().addAll(deliveryListBox, deliveryInputBox);
        setAlignment(Pos.TOP_CENTER);

    }
	
	private VBox createDeliveryListBox() {
	    VBox deliveriesViewBox = new VBox(5);
	    deliveriesViewBox.setAlignment(Pos.TOP_CENTER);

	    deliveriesList = new ListView<ListItem>();
	    
	    deliveriesList.setCellFactory(param -> new DeliveryListCell()); 
	    
	    deliveriesList.setPrefWidth(width);

	    // Make the ListView expand vertically
	    VBox.setVgrow(deliveriesList, Priority.ALWAYS);

	    deliveriesViewBox.getChildren().addAll(new Label("Deliveries"), deliveriesList);

	    return deliveriesViewBox;
	}
	
	private HBox createDeliveryInputBox() {
	    HBox deliveryInputBox = new HBox(10);
	    deliveryInputBox.setAlignment(Pos.CENTER);

	    // Id field
	    HBox idGroup = new HBox(5);
	    idGroup.setAlignment(Pos.CENTER_LEFT);
	    TextField deliveryIdField = createIdField(DeliveryManager.getDeliveryCount());
	    idGroup.getChildren().addAll(new Label("Id:"), deliveryIdField);

	    // Address field
	    HBox addressGroup = new HBox(5);
	    addressGroup.setAlignment(Pos.CENTER_LEFT);
	    TextField nodeIdField = createIdField(selectedNodeId);
	    addressGroup.getChildren().addAll(new Label("Address:"), nodeIdField);

	    // Delivery window
	    HBox deliveryWindowGroup = new HBox(5);
	    deliveryWindowGroup.setAlignment(Pos.CENTER_LEFT);
	    TextField earlyTimeField = createTimeField("08:00");
	    TextField lateTimeField = createTimeField("17:00");
	    deliveryWindowGroup.getChildren().addAll(new Label("Delivery window:"), earlyTimeField, new Label("to"), lateTimeField);

	    // Add button
	    HBox buttonGroup = new HBox();
	    buttonGroup.setAlignment(Pos.CENTER);
	    Button addDeliveryButton = new Button("Add Delivery");
	    addDeliveryButton.setOnAction(e -> {
	    	EventBus.getInstance().publish(
	    			new DeliveryAddRequestEvent(Integer.parseInt(nodeIdField.getText()), earlyTimeField.getText(), lateTimeField.getText())
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
        textField.setPrefWidth(60);
        
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
    	
    	idField.setPrefWidth(35);
    	return idField;
    }
}
