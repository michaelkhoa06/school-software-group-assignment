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

    private Shop shop;
    private PointsAccount pointsAccount;
    private Label pointsLabel;

    public ShopWindowUI(
            Shop shop,
            PointsAccount pointsAccount,
            Label pointsLabel
    ) {
        this.shop = shop;
        this.pointsAccount = pointsAccount;
        this.pointsLabel = pointsLabel;
    }

    public void show(Stage owner) {

        Stage shopStage = new Stage();
        shopStage.initOwner(owner);

        Label shopTitle = new Label("SHOP :");

        Label item1 = new Label("Bonus Points - 50 PTS");
        Button buy1 = new Button("Buy");

        VBox item1Box = new VBox(
                8,
                item1,
                buy1
        );

        buy1.setOnAction(e -> {

            if (shop.buyBonusPoints()) {

                pointsLabel.setText(
                        pointsAccount.getBalance() + " PTS"
                );

                buy1.setText("Purchased");
                buy1.setDisable(true);
            }
        });
        item1Box.setPadding(new Insets(12));
        item1Box.setStyle(FRAME_STYLE);

        Label item2 = new Label("Cheaper Skips - 40 PTS");
        Button buy2 = new Button("Buy");

        VBox item2Box = new VBox(
                8,
                item2,
                buy2
        );

        buy2.setOnAction(e -> {

            if (shop.buyCheaperSkips()) {

                pointsLabel.setText(
                        pointsAccount.getBalance() + " PTS"
                );

                buy2.setText("Purchased");
                buy2.setDisable(true);
            }
        });

        item2Box.setPadding(new Insets(12));
        item2Box.setStyle(FRAME_STYLE);

        Label item3 = new Label("More Breaks - 60 PTS");
        Button buy3 = new Button("Buy");

        VBox item3Box = new VBox(
                8,
                item3,
                buy3
        );

        buy3.setOnAction(e -> {

            if (shop.buyMoreBreaks()) {

                pointsLabel.setText(
                        pointsAccount.getBalance() + " PTS"
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
