import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;


public class SettingPanel {

    private static final String FRAME_STYLE =
            "-fx-border-color: #64748b;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;";

    private VBox settingsPanel;
    private Spinner<Integer> spinTimeSpinner;

    public SettingPanel() {

        Label rightLabel = new Label("SETTINGS:");

        Label spinTimeLabel = new Label("SPIN TIME:");

        spinTimeSpinner = new Spinner<>(
                1,
                15,
                5
        );

        settingsPanel = new VBox(
                15,
                rightLabel,
                spinTimeLabel,
                spinTimeSpinner
        );

        settingsPanel.setPadding(new Insets(12));
        settingsPanel.setStyle(FRAME_STYLE);


    }

    public VBox getView() {
        return settingsPanel;
    }

    public Spinner<Integer> getSpinTimeSpinner() {
        return spinTimeSpinner;
    }

}
