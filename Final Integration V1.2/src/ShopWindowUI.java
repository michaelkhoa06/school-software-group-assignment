import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ShopWindowUI {

    private static final String FRAME_STYLE =
            "-fx-border-color: #cbd5e1;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-color: #ffffff;";

    private final Consumer<Shop.ShopUpgradeIndices> purchaseRequest;

    private List<Boolean> purchases = List.of();

    private final Map<Shop.ShopUpgradeIndices, Button> buyButtons =
            new EnumMap<>(Shop.ShopUpgradeIndices.class);

    private Stage shopStage;

    public ShopWindowUI(
            Consumer<Shop.ShopUpgradeIndices> purchaseRequest
    ) {
        this.purchaseRequest = purchaseRequest;
    }

    public void show(Stage owner) {

        // Reuse the existing Shop window.
        if (shopStage != null) {
            shopStage.show();
            shopStage.toFront();
            return;
        }

        shopStage = new Stage();
        shopStage.initOwner(owner);

        Label shopTitle = new Label("SHOP");
        shopTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1e293b;"
        );

        VBox item1Box = createUpgradeCard(
                "Bonus Points",
                "Earn 15 points instead of 10 for completing a task.",
                Shop.ShopUpgradeIndices.BONUS_POINTS
        );

        VBox item2Box = createUpgradeCard(
                "Cheaper Skips",
                "Reduce the cost of skipping a task from 10 to 5 points.",
                Shop.ShopUpgradeIndices.CHEAPER_SKIPS
        );

        VBox item3Box = createUpgradeCard(
                "More Breaks",
                "Increase your current BREAK probability by 25%.",
                Shop.ShopUpgradeIndices.MORE_BREAKS
        );

        VBox shopLayout = new VBox(
                15,
                shopTitle,
                item1Box,
                item2Box,
                item3Box
        );

        shopLayout.setPadding(new Insets(20));
        shopLayout.setStyle(
                "-fx-background-color: #f1f5f9;"
        );

        Scene shopScene = new Scene(shopLayout, 340, 460);

        // Close the Shop using Escape.
        shopScene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                shopStage.close();
                event.consume();
            }
        });

        shopStage.setTitle("Shop");
        shopStage.setScene(shopScene);
        shopStage.setResizable(false);

        refreshButtons();

        shopStage.show();
    }

    // Creates a reusable Shop upgrade card.
    private VBox createUpgradeCard(
            String name,
            String description,
            Shop.ShopUpgradeIndices upgrade
    ) {

        Label title = new Label(
                name + " - " + upgrade.cost + " PTS"
        );

        title.setStyle("-fx-font-weight: bold;");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle(
                "-fx-text-fill: #64748b;"
        );

        Button buyButton = new Button("Buy");
        UIStyle.success(buyButton);
        buyButton.setMaxWidth(Double.MAX_VALUE);

        buyButtons.put(upgrade, buyButton);

        // Send the purchase request to Core.
        buyButton.setOnAction(event ->
                purchaseRequest.accept(upgrade)
        );

        VBox card = new VBox(
                8,
                title,
                descriptionLabel,
                buyButton
        );

        card.setPadding(new Insets(12));
        card.setStyle(FRAME_STYLE);

        return card;
    }

    // Receives updated purchase data from UIController.
    public void updatePurchases(List<Boolean> purchases) {

        this.purchases = List.copyOf(purchases);

        refreshButtons();
    }

    // Updates all Shop buttons using saved purchase data.
    private void refreshButtons() {

        buyButtons.forEach((upgrade, button) -> {

            int index = upgrade.ordinal();

            boolean purchased =
                    index < purchases.size() &&
                            purchases.get(index);

            button.setText(
                    purchased ? "Purchased" : "Buy"
            );

            button.setDisable(purchased);
        });
    }
}