package GraphReader.ui;

import com.delivery.core.eventbus.EventBus;
import com.delivery.core.eventbus.TruckDeleteRequestEvent;

public class TruckListCell extends PanelListCell{
	
	public TruckListCell()
	{
		button.setOnAction(event -> {
            getListView().getItems().remove(getItem());
            
            EventBus.getInstance().publish(new TruckDeleteRequestEvent(getIndex()));
            
            System.out.println("Publishing truck delete request event");
        });
	}

}
