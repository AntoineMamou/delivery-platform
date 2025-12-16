package user_interface.components;

import com.delivery.core.events.OnOptimizationResult;
import com.delivery.core.model.Delivery;
import com.delivery.core.model.Route;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import user_interface.UI;

public class DeliverySchedulePanel extends VBox {

    private final ListView<String> scheduleList = new ListView<>();

    public DeliverySchedulePanel() {
        setSpacing(UI.Spacing.SMALL);
        setBackground(new Background(
                new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)
            ));
        
        Label titleLabel = new Label("Delivery schedule");
        
        HBox labelContainer = new HBox(titleLabel);
        labelContainer.setAlignment(Pos.CENTER);
        
        getChildren().addAll(labelContainer, scheduleList);
        
        VBox.setVgrow(scheduleList, Priority.ALWAYS);
    }

    public void updateSchedule(OnOptimizationResult result) {
        Platform.runLater(() -> {
            scheduleList.getItems().clear();
            for (Route route : result.routes()) {
                scheduleList.getItems().add("Truck " + route.getVehicle().getId());
                for (Delivery d : route.getDeliveries()) {
                    scheduleList.getItems().add("  -> " + d.getEstimatedArrivalTime() +
                            " : Delivery " + d.getId() + ", Address " + d.getAddressNodeId());
                }
            }
        });
    }

    public ListView<String> getListView() {
        return scheduleList;
    }
}
