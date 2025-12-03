package GraphReader.ui;

import java.util.ArrayList;

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

    private ListView<String> trucksList;

    private ArrayList<Truck> trucks = new ArrayList<>();

    public TrucksPanel(double width, double height) {
        this.width = width;
        this.height = height;

        createTrucksPanel();
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

        trucksList = new ListView<>();
        trucksList.setPrefWidth(width);

        // Make the ListView expand vertically
        VBox.setVgrow(trucksList, Priority.ALWAYS);

        truckListBox.getChildren().addAll(new Label("Trucks"), trucksList);

        return truckListBox;
    }

    private HBox createTruckInputBox() {
        IntegerProperty truckCount = new SimpleIntegerProperty(0);

        HBox truckInputBox = new HBox(10);
        truckInputBox.setAlignment(Pos.CENTER);

        // Id field
        HBox idGroup = new HBox(5);
        idGroup.setAlignment(Pos.CENTER_LEFT);
        TextField truckIdField = new TextField();
        truckIdField.textProperty().bindBidirectional(truckCount, new NumberStringConverter());
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
        addTruckButton.setOnAction(e -> addTruck(truckCount, deliveryCapacityField.getText(), maxDistanceField.getText()));
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

    private void addTruck(IntegerProperty truckCount, String deliveryCapacityString, String maxDistanceString) {
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
