import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.List;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        // Core and Storage
        JsonStorage storage = new JsonStorage();
        RoulettePlannerCore core = new RoulettePlannerCore(storage);

        // UI Elements
        BorderPane root = new BorderPane();

        HistoryPanel historyPanel = new HistoryPanel();
        RouletteWheel rouletteWheel = new RouletteWheel(core);
        TaskPanel taskPanel = new TaskPanel(core);
        SettingPanel settingPanel = new SettingPanel();


        // UI Controller
        UIController uiController = new UIController(taskPanel, rouletteWheel, historyPanel);
        core.setUI(uiController);

        // Shop and Points accounting
        PointsAccount pointsAccount = new PointsAccount();

        pointsAccount.setBalance(
                core.getPoints()
        );

        Shop shop = new Shop(pointsAccount);

        List<Boolean> savedPurchases = core.getPurchases();

        shop.loadUpgrades(
                savedPurchases.get(0),
                savedPurchases.get(1),
                savedPurchases.get(2)
        );

        rouletteWheel.setShop(shop);
        taskPanel.setShop(shop);

        // PointsPanel UI
        PointsPanel pointsPanel = new PointsPanel(
                pointsAccount,
                shop,
                core,
                stage
        );


        // Open History
        taskPanel.getHistoryButton().setOnAction(event -> {

            taskPanel.hideTasks();
            historyPanel.show();
        });

        // Open Tasks
        taskPanel.getAddTaskButton().setOnAction(event -> {

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
                        (observable, oldValue, newValue) -> {
                            rouletteWheel.setSpinTime(newValue);
                        }
                );

        // RouletteWheel Listener
        rouletteWheel.getCompleteButton().setOnAction(event -> {

            TaskData selectedTask =
                    core.getSelectedTask();

            if (selectedTask == null) {
                return;
            }

            core.markSelectedTaskAs(
                    TaskStatus.COMPLETED
            );


            shop.completeTask();

            core.setPoints(pointsAccount.getBalance());

            pointsPanel.updatePoints();

            rouletteWheel.clearSelection();
        });


        rouletteWheel.getSkipButton().setOnAction(event -> {

            TaskData selectedTask =
                    core.getSelectedTask();

            if (selectedTask == null) {
                return;
            }

            core.markSelectedTaskAs(
                    TaskStatus.SKIPPED
            );

            shop.skipTask();

            core.setPoints(pointsAccount.getBalance());

            pointsPanel.updatePoints();

            rouletteWheel.clearSelection();
        });


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
        stage.setOnCloseRequest(event -> {
            core.onClose();
        });
        stage.show();

    }





    public static void main(String[] args) {
        launch(args);
    }
}