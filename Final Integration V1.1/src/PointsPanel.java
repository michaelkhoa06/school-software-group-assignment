import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;


public class PointsPanel {

    private static final String FRAME_STYLE =
            "-fx-border-color: #64748b;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;";

    private final HBox pointsPanel;
    private final Label pointsLabel;
    private final ShopWindowUI shopWindow;

    public PointsPanel(
            RoulettePlannerCore core, // TODO: This *should* receive loaded points only, but ShopWindowUI is still using Core too (it should not)
            Stage owner
    ) {

        pointsLabel = new Label(
                core.getPoints() + " PTS"
        );

        Button shopButton = new Button("Shop");

        shopWindow = new ShopWindowUI(
                pointsLabel,
                core
        );

        shopButton.setOnAction(_ -> shopWindow.show(owner));

        pointsPanel = new HBox(
                10,
                pointsLabel,
                shopButton
        );

        pointsPanel.setPadding(new Insets(12));
        pointsPanel.setStyle(FRAME_STYLE);
    }

    public HBox getView() {
        return pointsPanel;
    }

    public void updatePoints(int pointBalance) {
        pointsLabel.setText(
                pointBalance + " PTS"
        );
    }


}