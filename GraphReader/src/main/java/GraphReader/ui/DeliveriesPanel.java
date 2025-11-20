package GraphReader.ui;

import java.time.LocalTime;
import java.util.ArrayList;

import GraphReader.model.Delivery;
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


public class DeliveriesPanel extends VBox{
	
	private IntegerProperty selectedNodeId;
	
	private double width, height;
	
	private ListView<String> deliveriesList;
	
	private ArrayList<Delivery> deliveries = new ArrayList<>();
	
	public DeliveriesPanel(double width, double height, IntegerProperty selectedNodeId)
	{
		this.width = width;
		this.height = height;
		this.selectedNodeId = selectedNodeId;
		
		createDeliveriesPanel();
	}
	
	private void createDeliveriesPanel() {
        setPrefSize(width, height);
        
        VBox deliveryListBox = createDeliveryListBox();
        
        HBox deliveryInputBox = createDeliveryInputBox();

        getChildren().addAll(deliveryListBox, deliveryInputBox);
        setAlignment(Pos.TOP_CENTER);

    }
	
	private VBox createDeliveryListBox()
	{
		VBox deliveriesViewBox = new VBox();
		
        deliveriesList = new ListView<>();
        deliveriesList.setPrefWidth(width);
        
        ScrollPane deliveriesScroll = new ScrollPane(deliveriesList);
        deliveriesScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        deliveriesScroll.setPrefWidth(width);
        
        deliveriesViewBox.getChildren().addAll(new Label("Deliveries"), deliveriesScroll);
        
        return deliveriesViewBox;
	}
	
	private HBox createDeliveryInputBox()
	{
		IntegerProperty deliveryCount = new SimpleIntegerProperty(0);
		
		HBox deliveryInputBox = new HBox(5);
		deliveryInputBox.setAlignment(Pos.CENTER);

        TextField deliveryIdField = createIdField(deliveryCount);
        TextField nodeIdField = createIdField(selectedNodeId);

        TextField earlyTimeField = createTimeField("08:00");
        TextField lateTimeField = createTimeField("17:00");

        Button addDeliveryButton = new Button("Add Delivery");
        
        addDeliveryButton.setOnAction(e -> addDelivery(deliveryCount, earlyTimeField.getText(), lateTimeField.getText()));

        deliveryInputBox.getChildren().addAll(
                new Label("Id:"), deliveryIdField,
                new Label("Address:"), nodeIdField,
                new Label("Delivery window:"), earlyTimeField,
                new Label("to"), lateTimeField,
                addDeliveryButton
        );
    
        return deliveryInputBox;
	}
	
	private void addDelivery(IntegerProperty deliveryCount, String earlyTimeString, String lateTimeString)
	{
		// Data wasn't provided correctly, we dont create a delivery.
        if (earlyTimeString.isEmpty() || lateTimeString.isEmpty() || selectedNodeId.get() == -1) return;
        
        LocalTime earlyTime = getTimeFromString(earlyTimeString);
        LocalTime lateTime = getTimeFromString(lateTimeString);

        Delivery delivery = new Delivery(deliveryCount.get(), selectedNodeId.get(), earlyTime, lateTime);
        
        deliveriesList.getItems().add(delivery.toString());
        
        deliveryCount.setValue(deliveryCount.get() + 1);
            
        deliveries.add(delivery);
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
    	idField.textProperty().bindBidirectional(boundNumber, new NumberStringConverter());
    	idField.setPrefWidth(35);
    	idField.setEditable(false);
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
}
