import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HistoryPanel {

    private static final String FRAME_STYLE =
            "-fx-border-color: #64748b;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;";

    private VBox historyPanel;
    private VBox completedList;
    private VBox failedList;


    public void show() {
        historyPanel.setVisible(true);
        historyPanel.setManaged(true);
    }

    public void hide() {
        historyPanel.setVisible(false);
        historyPanel.setManaged(false);
    }

    public HistoryPanel(){

        Label historyTitle = new Label("HISTORY");

        Button completedButton = new Button("Completed");
        Button failedButton = new Button("Failed");

        HBox historyButtons = new HBox(
                10,
                completedButton,
                failedButton
        );

        // Completed Tab
        completedList = new VBox(8);

        // Failed Tab
        failedList = new VBox(8);


        VBox historyContent = new VBox();
        historyContent.getChildren().add(completedList);

        ScrollPane historyScroll = new ScrollPane(historyContent);

        historyScroll.setFitToWidth(true);
        historyScroll.setPrefViewportHeight(220);

        // History Panel
        historyPanel = new VBox(
                10,
                historyTitle,
                historyButtons,
                historyScroll
        );

        historyPanel.setVisible(false);
        historyPanel.setManaged(false);

        // SHOW Completed Tasks
        completedButton.setOnAction(event -> {
            historyContent.getChildren().setAll(completedList);

            completedButton.setStyle("-fx-font-weight: bold;");
            failedButton.setStyle("");
        });

        // SHOW Failed Tasks
        failedButton.setOnAction(event -> {
            historyContent.getChildren().setAll(failedList);

            failedButton.setStyle("-fx-font-weight: bold;");
            completedButton.setStyle("");
        });

        completedButton.setStyle("-fx-font-weight: bold;");

        historyPanel.setPadding(new Insets(12));
        historyPanel.setStyle(FRAME_STYLE);
    }

    public VBox getView() {
        return historyPanel;
    }

    public void addCompleted(String name) {
        completedList.getChildren().add(
                createHistoryEntry(name)
        );
    }

    public void addFailed(String name) {
        failedList.getChildren().add(
                createHistoryEntry(name)
        );
    }


    private Label createHistoryEntry(String name) {

        Label entry = new Label(name);
        entry.setPadding(new Insets(10));

        return entry;
    }


}
