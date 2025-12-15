package GraphReader.ui;

import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;

public class PanelListCell extends ListCell<ListItem>{
	protected final HBox hbox = new HBox();
	protected final Label label = new Label();
	protected final Pane pane = new Pane(); // used as a spacer
	protected final Button button = new Button("Delete");

    public PanelListCell() {
        super();
        hbox.getChildren().addAll(label, pane, button);
        HBox.setHgrow(pane, Priority.ALWAYS); // pushes the button to the right
    }

    @Override
    protected void updateItem(ListItem item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            label.setText(item.getName());
            setGraphic(hbox);
        }
    }
}