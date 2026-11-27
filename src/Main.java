import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.stage.WindowEvent;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Core components
        Config config = new Config();
        JsonStorage storage = new JsonStorage();
        RoulettePlannerCore core = new RoulettePlannerCore(storage);

        // UI Elements
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f1f5f9;");

        HistoryPanel historyPanel = new HistoryPanel();
        RouletteWheel rouletteWheel = new RouletteWheel(config, core);
        TaskPanel taskPanel = new TaskPanel(core);
        SettingPanel settingPanel = new SettingPanel(config.spinTimeSec());

        PointsPanel pointsPanel = new PointsPanel(core::makePurchase, stage);

        // UI Controller
        UIController uiController = new UIController(taskPanel, rouletteWheel, historyPanel, pointsPanel);
        core.setUI(uiController);


        // Open History
        taskPanel.getHistoryButton().setOnAction(_ -> {

            taskPanel.hideTasks();
            historyPanel.show();
        });

        // Open Tasks
        taskPanel.getAddTaskButton().setOnAction(_ -> {

            historyPanel.hide();
            taskPanel.showTasks();
        });


        // Left panel group
        VBox leftPanel = new VBox(
                12,
                taskPanel.getView(),
                historyPanel.getView()
        );

        // Right panel group
        VBox rightPanel = new VBox(
                12,
                pointsPanel.getView(),
                settingPanel.getView(),
                settingPanel.getHotkeysPanel(),
                settingPanel.getInstructionsPanel()
        );
        rightPanel.setPadding(new Insets(10));


        // SettingPanel Listener
        settingPanel.getSpinTimeSpinner()
                .valueProperty()
                .addListener(
                        (_, _, newValue) ->
                                rouletteWheel.setSpinTime(newValue)
                );

        // RouletteWheel Listener
        rouletteWheel.getCompleteButton().setOnAction(_ ->
                core.markSelectedTaskAs(TaskStatus.COMPLETED)
        );


        rouletteWheel.getSkipButton().setOnAction(_ ->
                core.markSelectedTaskAs(TaskStatus.SKIPPED)
        );

        rouletteWheel.getFailButton().setOnAction(_ ->
                core.markSelectedTaskAs(TaskStatus.FAILED)
        );

        // MAIN //
        // Left side UI
        root.setLeft(leftPanel);
        leftPanel.setPrefWidth(280);
        leftPanel.setMinWidth(280);
        leftPanel.setMaxWidth(280);
        leftPanel.setPadding(new Insets(10));

        // Centered Wheel
        root.setCenter(rouletteWheel.getView());
        rouletteWheel.getView().setPadding(new Insets(20));

        // Right side UI
        root.setRight(rightPanel);
        rightPanel.setPrefWidth(200);
        rightPanel.setMinWidth(200);
        rightPanel.setMaxWidth(200);

        // Main Window
        Scene scene = new Scene(root, 1000, 720);

        // Keyboard shortcuts
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {

            if (event.isControlDown() || event.isAltDown() ||
                    event.isMetaDown()) {
                return;
            }

            // Allow an open date picker to handle Escape itself
            if (scene.getFocusOwner() instanceof DatePicker picker
                    && picker.isShowing()) {
                return;
            }

            // Exit through the normal shutdown process
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.fireEvent(new WindowEvent(
                        stage,
                        WindowEvent.WINDOW_CLOSE_REQUEST
                ));
                event.consume();
                return;
            }

            // Prevent shortcuts from activating while entering task information
            if (scene.getFocusOwner() instanceof TextInputControl ||
                    scene.getFocusOwner() instanceof DatePicker ||
                    scene.getFocusOwner() instanceof Spinner<?>) {
                return;
            }

            switch (event.getCode()) {
                case SPACE -> rouletteWheel.getSpinButton().fire();
                case S -> pointsPanel.getShopButton().fire();
                case A -> taskPanel.getAddTaskButton().fire();
                case H -> taskPanel.getHistoryButton().fire();
                case C -> rouletteWheel.getCompleteButton().fire();
                case K -> rouletteWheel.getSkipButton().fire();
                case F -> rouletteWheel.getFailButton().fire();
                default -> {
                    return;
                }
            }

            event.consume();
        });


        stage.setTitle("Roulette Planner");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(_ -> {
            core.onClose();
            config.saveNow();
        });
        stage.show();

    }





    public static void main(String[] args) {
        launch(args);
    }
}