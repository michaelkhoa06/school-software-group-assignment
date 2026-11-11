import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class Main extends Application {


    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        PointsAccount pointsAccount = new PointsAccount();

        Shop shop = new Shop(pointsAccount);

        HistoryPanel history = new HistoryPanel();

        RouletteWheel rouletteWheel = new RouletteWheel();

        TaskPanel taskPanel = new TaskPanel();

        SettingPanel settingPanel = new SettingPanel();


        // Open History
        taskPanel.getHistoryButton().setOnAction(event -> {

            taskPanel.hideTasks();

            history.show();
        });

        // Open Tasks
        taskPanel.getAddTaskButton().setOnAction(event -> {

            history.hide();

            taskPanel.showTasks();
        });


        VBox leftPanel = new VBox(
                12,
                taskPanel.getView(),
                history.getView()
        );

        PointsPanel pointsPanel =
                new PointsPanel(
                        pointsAccount,
                        shop,
                        stage
                );


        // SettingPanel Listener
        settingPanel.getSpinTimeSpinner()
                .valueProperty()
                .addListener(
                        (observable, oldValue, newValue) -> {
                            rouletteWheel.setSpinTime(newValue);
                        }
                );


        VBox rightPanel = new VBox(
                12,
                pointsPanel.getView(),
                settingPanel.getView()
        );
        rightPanel.setPadding(new Insets(10));

        rouletteWheel.getCompleteButton().setOnAction(event -> {

            String selectedTask =
                    rouletteWheel.getSelectedTaskName();

            if (selectedTask == null) {
                return;
            }

            history.addCompleted(selectedTask);

            shop.completeTask();

            pointsPanel.updatePoints();

            rouletteWheel.clearSelection();
        });

        rouletteWheel.getSkipButton().setOnAction(event -> {

            String selectedTask =
                    rouletteWheel.getSelectedTaskName();

            if (selectedTask == null) {
                return;
            }

            history.addFailed(selectedTask);

            shop.skipTask();

            pointsPanel.updatePoints();

            rouletteWheel.clearSelection();
        });



        // MAIN
        root.setLeft(leftPanel);
        leftPanel.setPrefWidth(280);
        leftPanel.setMinWidth(280);
        leftPanel.setMaxWidth(280);
        leftPanel.setPadding(new Insets(10));

        root.setCenter(rouletteWheel.getView());
        rouletteWheel.getView().setPadding(new Insets(20));

        root.setRight(rightPanel);
        rightPanel.setPrefWidth(200);
        rightPanel.setMinWidth(200);
        rightPanel.setMaxWidth(200);

        Scene scene = new Scene(root, 1000, 600);

        stage.setTitle("Roulette Planner");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }



    public static void main(String[] args) {
        launch(args);
    }



}