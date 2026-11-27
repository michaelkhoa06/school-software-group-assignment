import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import java.util.List;
import java.util.function.Consumer;

public class PointsPanel {

    private static final String FRAME_STYLE =
            "-fx-border-color: #cbd5e1;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-color: #ffffff;";

     private final HBox pointsPanel;
    private final Label pointsLabel;
    private final Button shopButton;
    private final ShopWindowUI shopWindow;

    public PointsPanel(
            Consumer<Shop.ShopUpgradeIndices> purchaseRequest,
            Stage owner
    ) {

        pointsLabel = new Label("0 PTS");


        pointsLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #15803d;"
        );

        shopButton = new Button("Shop");
        UIStyle.success(shopButton);

        shopWindow = new ShopWindowUI(purchaseRequest);

        shopButton.setOnAction(_ -> shopWindow.show(owner));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        pointsPanel = new HBox(
                10,
                pointsLabel,
                spacer,
                shopButton
        );

        pointsPanel.setAlignment(Pos.CENTER_LEFT);


        pointsPanel.setPadding(new Insets(12));
        pointsPanel.setStyle(FRAME_STYLE);

    }

    public Button getShopButton() {
        return shopButton;
    }

    public HBox getView() {
        return pointsPanel;
    }

    public void updatePoints(int pointBalance) {
        pointsLabel.setText(
                pointBalance + " PTS"
        );
    }
    public void updatePurchases(List<Boolean> purchases) {
        shopWindow.updatePurchases(purchases);
    }


}