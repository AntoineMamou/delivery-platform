package user_interface.components;

import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import java.util.function.IntConsumer;

public class PanelListCell<T> extends ListCell<T> {
    protected final HBox hbox = new HBox();
    protected final Label label = new Label();
    protected final Pane pane = new Pane(); // spacer
    protected final Button button = new Button("Delete");

    public PanelListCell(IntConsumer onDelete) {
        super();

        hbox.getChildren().addAll(label, pane, button);
        HBox.setHgrow(pane, Priority.ALWAYS);

        button.setOnAction(event -> {
            getListView().getItems().remove(getItem());
            if (onDelete != null) {
                onDelete.accept(getIndex());
            }
        });
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            label.setText(item.toString());
            setGraphic(hbox);
        }
    }
}