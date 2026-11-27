import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.geometry.Pos;


public class SettingPanel {

    private static final String FRAME_STYLE =
            "-fx-border-color: #cbd5e1;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-color: #ffffff;";

    private final VBox settingsPanel;
    private final VBox hotkeysPanel;
    private final VBox instructionsPanel;
    private final Spinner<Integer> spinTimeSpinner;

    public SettingPanel(int startingNumber) {

        Label rightLabel = new Label("SETTINGS:");
        rightLabel.setStyle("-fx-font-weight: bold;");

        Label spinTimeLabel = new Label("SPIN TIME:");

        spinTimeSpinner = new Spinner<>(
                1,
                15,
                startingNumber
        );

        settingsPanel = new VBox(
                15,
                rightLabel,
                spinTimeLabel,
                spinTimeSpinner
        );

        settingsPanel.setPadding(new Insets(12));
        settingsPanel.setStyle(FRAME_STYLE);

        // Hotkeys Panel
        Label hotkeysTitle = new Label("HOTKEYS:");
        hotkeysTitle.setStyle("-fx-font-weight: bold;");

        GridPane hotkeysGrid = new GridPane();
        hotkeysGrid.setHgap(15);
        hotkeysGrid.setVgap(6);

        String[][] hotkeys = {
                {"Space", "Spin"},
                {"S", "Shop"},
                {"A", "Add Task"},
                {"H", "History"},
                {"C", "Complete"},
                {"K", "Skip"},
                {"F", "Fail"},
                {"Esc", "Exit / Close"}
        };

        for (int i = 0; i < hotkeys.length; i++) {
            Label key = new Label(hotkeys[i][0]);
            Label action = new Label(hotkeys[i][1]);

            key.setMinWidth(45);
            key.setAlignment(Pos.CENTER);

            key.setStyle(
                    "-fx-background-color: #e2e8f0;" +
                            "-fx-background-radius: 4;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 3 6 3 6;"
            );

            hotkeysGrid.add(key, 0, i);
            hotkeysGrid.add(action, 1, i);
        }

        hotkeysPanel = new VBox(
                12,
                hotkeysTitle,
                hotkeysGrid
        );

        hotkeysPanel.setPadding(new Insets(12));
        hotkeysPanel.setStyle(FRAME_STYLE);

        // Instructions Panel
        Label instructionsTitle = new Label("INSTRUCTIONS:");
        instructionsTitle.setStyle("-fx-font-weight: bold;");

        Label instructions = new Label(
                "1. Add your tasks.\n" +
                        "2. Spin the wheel.\n" +
                        "3. Complete, skip, or fail tasks.\n" +
                        "4. Spend points on upgrades."
        );

        instructions.setWrapText(true);

        instructionsPanel = new VBox(
                10,
                instructionsTitle,
                instructions
        );

        instructionsPanel.setPadding(new Insets(12));
        instructionsPanel.setStyle(FRAME_STYLE);

    }

    public VBox getHotkeysPanel() {
        return hotkeysPanel;
    }

    public VBox getInstructionsPanel() {
        return instructionsPanel;
    }

    public VBox getView() {
        return settingsPanel;
    }

    public Spinner<Integer> getSpinTimeSpinner() {
        return spinTimeSpinner;
    }

}