package GraphReader.ui;

import com.delivery.core.eventbus.DeliveryDeleteRequestEvent;
import com.delivery.core.eventbus.EventBus;

public class DeliveryListCell extends PanelListCell{

    public DeliveryListCell() {
        super();

        button.setOnAction(event -> {
            getListView().getItems().remove(getItem());
            
            EventBus.getInstance().publish(new DeliveryDeleteRequestEvent(getIndex()));
        });
    }
}
