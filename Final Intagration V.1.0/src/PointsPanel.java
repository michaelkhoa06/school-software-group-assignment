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

    private HBox pointsPanel;
    private Label pointsLabel;

    private PointsAccount pointsAccount;
    private ShopWindowUI shopWindow;

    public PointsPanel(
            PointsAccount pointsAccount,
            Shop shop,
            RoulettePlannerCore core,
            Stage owner
    ) {

        this.pointsAccount = pointsAccount;

        pointsLabel = new Label(
                pointsAccount.getBalance() + " PTS"
        );

        Button shopButton = new Button("Shop");

        shopWindow = new ShopWindowUI(
                shop,
                pointsAccount,
                pointsLabel,
                core
        );

        shopButton.setOnAction(event -> {
            shopWindow.show(owner);
        });

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

    public void updatePoints() {
        pointsLabel.setText(
                pointsAccount.getBalance() + " PTS"
        );
    }


}
