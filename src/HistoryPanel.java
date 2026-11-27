import javafx.geometry.Insets;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

// Displays completed, failed, and skipped tasks.
public class HistoryPanel {

    private static final String FRAME_STYLE =
            "-fx-border-color: #cbd5e1;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-color: #ffffff;";

    private VBox historyPanel;
    private VBox completedList;
    private VBox skippedList;
    private VBox failedList;

    public void show() {
        historyPanel.setVisible(true);
        historyPanel.setManaged(true);
    }

    public void hide() {
        historyPanel.setVisible(false);
        historyPanel.setManaged(false);
    }

    public HistoryPanel() {

        Label historyTitle = new Label("HISTORY");
        historyTitle.setStyle("-fx-font-weight: bold;");

        Button completedButton = new Button("Completed");
        Button skippedButton = new Button("Skipped");
        Button failedButton = new Button("Failed");

        UIStyle.success(completedButton);
        UIStyle.warning(skippedButton);
        UIStyle.danger(failedButton);

        completedButton.setMaxWidth(Double.MAX_VALUE);
        skippedButton.setMaxWidth(Double.MAX_VALUE);
        failedButton.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(completedButton, Priority.ALWAYS);
        HBox.setHgrow(skippedButton, Priority.ALWAYS);
        HBox.setHgrow(failedButton, Priority.ALWAYS);

        HBox historyButtons = new HBox(
                5,
                completedButton,
                skippedButton,
                failedButton
        );

        // Completed and failed task lists
        completedList = new VBox(8);
        skippedList = new VBox(8);
        failedList = new VBox(8);

        VBox historyContent = new VBox();
        historyContent.getChildren().add(completedList);

        ScrollPane historyScroll = new ScrollPane(historyContent);

        historyScroll.setFitToWidth(true);
        historyScroll.setPrefViewportHeight(400);

        historyScroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: #ffffff;"
        );

        historyContent.setStyle(
                "-fx-background-color: #ffffff;"
        );

        // History Panel
        historyPanel = new VBox(
                10,
                historyTitle,
                historyButtons,
                historyScroll
        );

        historyPanel.setVisible(false);
        historyPanel.setManaged(false);

        // Show completed tasks
        completedButton.setOnAction(event -> {
            historyContent.getChildren().setAll(completedList);
        });

        // Show skipped tasks
        skippedButton.setOnAction(event -> {
            historyContent.getChildren().setAll(skippedList);
        });
        // Show failed tasks
        failedButton.setOnAction(event -> {
            historyContent.getChildren().setAll(failedList);
        });


        historyPanel.setPadding(new Insets(12));
        historyPanel.setStyle(FRAME_STYLE);
    }

    public VBox getView() {
        return historyPanel;
    }

    // Refresh history using the current Core data.
    public void updateHistory(Map<TaskData, Integer> history) {

        completedList.getChildren().clear();
        skippedList.getChildren().clear();
        failedList.getChildren().clear();

        for (TaskData task : history.keySet()) {

            if (task.getStatus() == TaskStatus.COMPLETED) {

                completedList.getChildren().add(
                        createHistoryEntry(
                                task.getTitle(),
                                "#f0fdf4",
                                "#166534"
                        )
                );

            } else if (task.getStatus() == TaskStatus.SKIPPED) {

                skippedList.getChildren().add(
                        createHistoryEntry(
                                task.getTitle(),
                                "#fffbeb",
                                "#92400e"
                        )
                );

            } else if (task.getStatus() == TaskStatus.FAILED) {

                failedList.getChildren().add(
                        createHistoryEntry(
                                task.getTitle(),
                                "#fef2f2",
                                "#b91c1c"
                        )
                );
            }
        }
    }

    // Creates a color-coded entry based on task status.
    private Label createHistoryEntry(
            String name,
            String background,
            String textColor
    ) {

        Label entry = new Label(name);

        entry.setPadding(new Insets(12));
        entry.setMaxWidth(Double.MAX_VALUE);
        entry.setWrapText(true);

        entry.setStyle(
                "-fx-background-color: " + background + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-background-radius: 6;" +
                        "-fx-font-weight: bold;"
        );

        return entry;
    }
}