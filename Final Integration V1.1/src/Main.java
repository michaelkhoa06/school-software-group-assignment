import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Core components
        Config config = new Config();
        JsonStorage storage = new JsonStorage();
        RoulettePlannerCore core = new RoulettePlannerCore(storage);

        // UI Elements
        BorderPane root = new BorderPane();

        HistoryPanel historyPanel = new HistoryPanel();
        RouletteWheel rouletteWheel = new RouletteWheel(config, core);
        TaskPanel taskPanel = new TaskPanel(core);
        SettingPanel settingPanel = new SettingPanel(config.spinTimeSec());
        PointsPanel pointsPanel = new PointsPanel(core, stage);


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
                settingPanel.getView()
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
        Scene scene = new Scene(root, 1000, 600);

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