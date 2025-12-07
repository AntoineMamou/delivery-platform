package GraphReader.ui;

import java.time.LocalTime;
import java.util.ArrayList;

import com.delivery.core.eventbus.DeliveryAddedEvent;
import com.delivery.core.eventbus.EventBus;
import com.delivery.core.model.Delivery;


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


public class DeliveriesPanel extends VBox{
	
	private IntegerProperty selectedNodeId;
	
	private double width, height;
	
	private int maxNodes;
	
	private ListView<String> deliveriesList;
	
	private ArrayList<Delivery> deliveries = new ArrayList<>();
	
	public DeliveriesPanel(double width, double height, IntegerProperty selectedNodeId)
	{
		this.width = width;
		this.height = height;
		this.selectedNodeId = selectedNodeId;
		
		createDeliveriesPanel();
	}
	
	public void setMaxNodes(int maxNodes)
	{
		this.maxNodes = maxNodes;
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

	    deliveriesList = new ListView<>();
	    deliveriesList.setPrefWidth(width);

	    // Make the ListView expand vertically
	    VBox.setVgrow(deliveriesList, Priority.ALWAYS);

	    deliveriesViewBox.getChildren().addAll(new Label("Deliveries"), deliveriesList);

	    return deliveriesViewBox;
	}
	
	private HBox createDeliveryInputBox() {
	    IntegerProperty deliveryCount = new SimpleIntegerProperty(0);

	    HBox deliveryInputBox = new HBox(10);
	    deliveryInputBox.setAlignment(Pos.CENTER);

	    // Id field
	    HBox idGroup = new HBox(5);
	    idGroup.setAlignment(Pos.CENTER_LEFT);
	    TextField deliveryIdField = createIdField(deliveryCount);
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
	    addDeliveryButton.setOnAction(e -> addDelivery(deliveryCount, Integer.parseInt(nodeIdField.getText()), earlyTimeField.getText(), lateTimeField.getText()));
	    buttonGroup.getChildren().add(addDeliveryButton);

	    deliveryInputBox.getChildren().addAll(idGroup, addressGroup, deliveryWindowGroup, buttonGroup);

	    return deliveryInputBox;
	}
	
	private void addDelivery(IntegerProperty deliveryCount, int addressId, String earlyTimeString, String lateTimeString)
	{
		// Data wasn't provided correctly, we dont create a delivery.                //Change that to be modular
        if (earlyTimeString.isEmpty() || lateTimeString.isEmpty() || addressId < 0 || addressId > 30) return;
        
        LocalTime earlyTime = getTimeFromString(earlyTimeString);
        LocalTime lateTime = getTimeFromString(lateTimeString);
        
        try {
        	validateTimeWindow(earlyTime, lateTime);
        	
        	Delivery delivery = new Delivery(deliveryCount.get(), addressId, earlyTime, lateTime);
            
            deliveriesList.getItems().add(delivery.toString());
            
            //Increment delivery count
            deliveryCount.setValue(deliveryCount.get() + 1);
                
            deliveries.add(delivery);
            
            EventBus.getInstance().publish(new DeliveryAddedEvent(addressId));
        } catch (InvalidDeliveryWindowException e) {
            System.err.println("Error: " + e.getMessage());
        }
	}
	
	// Helper to create a validated time field
    private TextField createTimeField(String defaultText)
    {
        TextField textField = new TextField(defaultText);
        textField.setPromptText("HH:mm");
        textField.setPrefWidth(60);
        
        // Input validation: only allow digits and colon while typing.
        // Regex explanation:
        //   ([01]?\\d?|2[0-3]?)  -> allows 0-23 hours, optionally 1 or 2 digits
        //   (:[0-5]?\\d?)?       -> optionally allows a colon followed by 0-59 minutes, optionally 1 or 2 digits
        // On focus lost, we enforce full HH:mm format, where hours are 00-23 and minutes are 00-59.

        textField.textProperty().addListener((obs, oldV, newV) -> {
            if (!newV.matches("([01]?\\d?|2[0-3]?)(:[0-5]?\\d?)?")) textField.setText(oldV);
        });

        textField.focusedProperty().addListener((obs, oldF, isFocused) -> {
            if (!isFocused && !textField.getText().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            	textField.setText("");
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
    
    private LocalTime getTimeFromString(String string)
    {
    	LocalTime time;
    	
    	if (string.length() < 5) return null;
    	
    	int hour = Integer.parseInt(string.substring(0, 2));
        int minute = Integer.parseInt(string.substring(3, 5));
            
        time = LocalTime.of(hour, minute);
        
        return time;
    	
    }
    
    private void validateTimeWindow(LocalTime startTime, LocalTime endTime) throws InvalidDeliveryWindowException
    {
    	if (startTime.isAfter(endTime)) {
            throw new InvalidDeliveryWindowException(
                "Invalid delivery window: start time (" + startTime + ") cannot be after end time (" + endTime + ")"
            );
        }
    }
    
    public ArrayList<Delivery> getDeliveries() {
        return new ArrayList<>(deliveries); // return a copy
    }

}
