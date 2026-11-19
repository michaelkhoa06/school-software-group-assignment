import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ShopWindowUI {

    private static final String FRAME_STYLE =
            "-fx-border-color: #64748b;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;";

    private final Label pointsLabel;
    private final RoulettePlannerCore core;


    public ShopWindowUI(
            Label pointsLabel,
            RoulettePlannerCore core // TODO: Ideally, this would save state from a snapshot and not use getters (and not need a Core reference)
    ) {
        this.pointsLabel = pointsLabel;
        this.core = core;
    }

    public void show(Stage owner) {

        Stage shopStage = new Stage();
        shopStage.initOwner(owner);

        Label shopTitle = new Label("SHOP :");

        Label item1 = new Label("Bonus Points - " + Shop.ShopUpgradeIndices.BONUS_POINTS.cost  + " PTS");
        Button buy1 = new Button("Buy");

        if (core.getUpgradeStatus(Shop.ShopUpgradeIndices.BONUS_POINTS)) {
            buy1.setText("Purchased");
            buy1.setDisable(true);
        }

        VBox item1Box = new VBox(
                8,
                item1,
                buy1
        );

        buy1.setOnAction(_ -> {

            if (core.makePurchase(Shop.ShopUpgradeIndices.BONUS_POINTS)) {

                pointsLabel.setText(
                        core.getPoints() + " PTS"
                );

                buy1.setText("Purchased");
                buy1.setDisable(true);
            }
        });
        item1Box.setPadding(new Insets(12));
        item1Box.setStyle(FRAME_STYLE);

        Label item2 = new Label("Cheaper Skips - " + Shop.ShopUpgradeIndices.CHEAPER_SKIPS.cost  + " PTS");
        Button buy2 = new Button("Buy");

        if (core.getUpgradeStatus(Shop.ShopUpgradeIndices.CHEAPER_SKIPS)) {
            buy2.setText("Purchased");
            buy2.setDisable(true);
        }

        VBox item2Box = new VBox(
                8,
                item2,
                buy2
        );

        buy2.setOnAction(_ -> {

            if (core.makePurchase(Shop.ShopUpgradeIndices.CHEAPER_SKIPS)) {

                pointsLabel.setText(
                        core.getPoints() + " PTS"
                );

                buy2.setText("Purchased");
                buy2.setDisable(true);
            }
        });

        item2Box.setPadding(new Insets(12));
        item2Box.setStyle(FRAME_STYLE);

        Label item3 = new Label("More Breaks - " + Shop.ShopUpgradeIndices.MORE_BREAKS.cost  + " PTS");
        Button buy3 = new Button("Buy");

        if (core.getUpgradeStatus(Shop.ShopUpgradeIndices.MORE_BREAKS)) {
            buy3.setText("Purchased");
            buy3.setDisable(true);
        }

        VBox item3Box = new VBox(
                8,
                item3,
                buy3
        );

        buy3.setOnAction(_ -> {

            if (core.makePurchase(Shop.ShopUpgradeIndices.MORE_BREAKS)) {

                pointsLabel.setText(
                        core.getPoints() + " PTS"
                );

                buy3.setText("Purchased");
                buy3.setDisable(true);
            }
        });

        item3Box.setPadding(new Insets(12));
        item3Box.setStyle(FRAME_STYLE);

        VBox shopLayout = new VBox(
                15,
                shopTitle,
                item1Box,
                item2Box,
                item3Box
        );



        shopLayout.setPadding(new Insets(20));

        Scene shopScene = new Scene(
                shopLayout,
                300,
                400
        );

        shopStage.setTitle("Shop");
        shopStage.setScene(shopScene);
        shopStage.setResizable(false);
        shopStage.show();

    }





}