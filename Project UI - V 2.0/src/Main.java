import com.google.gson.Gson;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;

import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ScrollPane;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;
import javafx.util.Duration;


public class Main extends Application {

    @Override
    public void start(Stage stage) {

        BorderPane root = new BorderPane();

        // =====================================================
        // LEFT SIDE
        // =====================================================

        // Task Management
        Label leftLabel = new Label("TASKS:");

        Button addTaskButton = new Button("Add Task");
        Button historyButton = new Button("History");

        VBox taskControls = new VBox(
                10,
                leftLabel,
                addTaskButton,
                historyButton
        );

        //
        Label newTaskLabel = new Label("NEW TASK ENTRY");

        Label taskNameLabel = new Label("Task Name");
        TextField taskNameField = new TextField();
        taskNameField.setPromptText("e.g., Math Homework");

        Label deadlineLabel = new Label("Deadline");
        DatePicker deadlinePicker = new DatePicker();

        Label estimatedTimeLabel = new Label("Estimated Completion Time (minutes)");
        Spinner<Integer> estimatedTimeSpinner = new Spinner<>(5, 480, 30, 5);

        Label weightLabel = new Label("Calculated Weight: --");

        Button addToWheelButton = new Button("Add to Wheel");

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);

        VBox taskForm = new VBox(
                8,
                newTaskLabel,
                taskNameLabel,
                taskNameField,
                deadlineLabel,
                deadlinePicker,
                estimatedTimeLabel,
                estimatedTimeSpinner,
                weightLabel,
                addToWheelButton,
                errorLabel
        );

        // Active Task List
        VBox taskList = new VBox(10);

        addToWheelButton.setOnAction(event -> {

            String name = taskNameField.getText().trim();

            if (name.isEmpty() || deadlinePicker.getValue() == null) {
                errorLabel.setText("Enter task name and deadline.");
                return;
            }

            int estimatedTime = estimatedTimeSpinner.getValue();
            String deadline = deadlinePicker.getValue().toString();

            taskList.getChildren().add(
                    createTaskCard(
                            name,
                            "-- odds",
                            estimatedTime,
                            deadline
                    )
            );

            // Clear form
            taskNameField.clear();
            deadlinePicker.setValue(null);
            estimatedTimeSpinner.getValueFactory().setValue(30);

            errorLabel.setText("");
        });

        taskList.getChildren().add(
                createTaskCard(
                        "Math Homework",
                        "33.3% odds",
                        30,
                        "Nov 5"
                )
        );

        ScrollPane taskScroll = new ScrollPane(taskList);
        taskScroll.setFitToWidth(true);
        taskScroll.setPrefViewportHeight(150);

        taskScroll.setPadding(new Insets(12));

        taskScroll.setStyle(
                "-fx-border-color: #64748b;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-background-color: transparent;"
        );
        taskList.setPadding(new Insets(5));

        // =====================================================
        // HISTORY
        // =====================================================

        Label historyTitle = new Label("HISTORY");

        // Buttons History sections
        Button completedButton = new Button("Completed");

        Button failedButton = new Button("Failed");

        HBox historyButtons = new HBox(
                10,
                completedButton,
                failedButton
        );

        // Completed Tab
        VBox completedList = new VBox(8);

        completedList.getChildren().add(
                createHistoryEntry("Math Homework")
        );

        // Failed Tab
        VBox failedList = new VBox(8);

        failedList.getChildren().add(
                createHistoryEntry("Programming Lab")
        );

        // // //
        VBox historyContent = new VBox();

        historyContent.getChildren().add(completedList);

        // History Scroll
        ScrollPane historyScroll = new ScrollPane(historyContent);

        historyScroll.setFitToWidth(true);
        historyScroll.setPrefViewportHeight(220);


        // History Panel
        VBox historyPanel = new VBox(
                10,
                historyTitle,
                historyButtons,
                historyScroll
        );

        historyPanel.setVisible(false);
        historyPanel.setManaged(false);

        // =====================================================
        // HISTORY BUTTON BEHAVIOR
        // =====================================================

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


        // Open History
        historyButton.setOnAction(event -> {

            taskForm.setVisible(false);
            taskForm.setManaged(false);

            taskScroll.setVisible(false);
            taskScroll.setManaged(false);

            historyPanel.setVisible(true);
            historyPanel.setManaged(true);
        });

        // Open Task
        addTaskButton.setOnAction(event -> {

            historyPanel.setVisible(false);
            historyPanel.setManaged(false);

            taskForm.setVisible(true);
            taskForm.setManaged(true);

            taskScroll.setVisible(true);
            taskScroll.setManaged(true);
        });

        // =====================================================
        // LEFT SIDE FRAMES
        // =====================================================

        taskControls.setPadding(new Insets(12));

        taskControls.setStyle(
                "-fx-border-color: #64748b;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        taskForm.setPadding(new Insets(12));

        taskForm.setStyle(
                "-fx-border-color: #64748b;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        historyPanel.setPadding(new Insets(12));

        historyPanel.setStyle(
                "-fx-border-color: #64748b;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        // THE Left Panel
        VBox leftPanel = new VBox(
                12,
                taskControls,
                taskForm,
                taskScroll,
                historyPanel
        );

        Label centerLabel = new Label("SPIN");


        // RIGHT SIDE Points/Shop
        Label pointsLabel = new Label("120 PTS");
        Button shopButton = new Button("Shop");

        shopButton.setOnAction(event -> {

            Stage shopStage = new Stage();
            shopStage.initOwner(stage);

            Label shopTitle = new Label("SHOP :");

            Label item1 = new Label("Skip Task - 50 PTS");
            Button buy1 = new Button("Buy");

            VBox item1Box = new VBox(
                    8,
                    item1,
                    buy1
            );

            item1Box.setPadding(new Insets(12));
            item1Box.setStyle(
                    "-fx-border-color: #64748b;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;"
            );


            Label item2 = new Label("Extra Spin - 100 PTS");
            Button buy2 = new Button("Buy");

            VBox item2Box = new VBox(
                    8,
                    item2,
                    buy2
            );

            item2Box.setPadding(new Insets(12));
            item2Box.setStyle(
                    "-fx-border-color: #64748b;" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 10;" +
                            "-fx-background-radius: 10;"
            );


            VBox shopLayout = new VBox(
                    15,
                    shopTitle,
                    item1Box,
                    item2Box
            );

            shopLayout.setPadding(new Insets(20));

            Scene shopScene = new Scene(
                    shopLayout,
                    300,
                    250
            );

            shopStage.setTitle("Shop");
            shopStage.setScene(shopScene);
            shopStage.setResizable(false);
            shopStage.show();
        });

        // //
        HBox pointsPanel = new HBox(
                10,
                pointsLabel,
                shopButton
        );

        pointsPanel.setPadding(new Insets(12));

        pointsPanel.setStyle(
                "-fx-border-color: #64748b;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        // SETTINGS
        Label rightLabel = new Label("SETTINGS:");

        Label spinTimeLabel = new Label("SPIN TIME:");
        Spinner<Integer> spinTimeSpinner = new Spinner<>(1, 15, 5);

        VBox settingsPanel = new VBox(
                15,
                rightLabel,
                spinTimeLabel,
                spinTimeSpinner
        );

        settingsPanel.setPadding(new Insets(12));

        settingsPanel.setStyle(
                "-fx-border-color: #64748b;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        VBox rightPanel = new VBox(
                12,
                pointsPanel,
                settingsPanel
        );

        rightPanel.setPadding(new Insets(10));


        // CENTER Wheel
        Circle wheel = new Circle(210);
        Circle wheelButton = new Circle(60);

        Group wheelGroup = new Group(wheel);

        // Pointer arrow
        Polygon pointer = new Polygon(
                0, 0,
                40, 0, // width , 0
                20, 60 // width/2, height
        );

        pointer.setFill(Color.RED);
        pointer.setStroke(Color.BLACK);
        pointer.setStrokeWidth(2);

        Label resultLabel = new Label("Selected Task: --");

        StackPane wheelPanel = new StackPane(
                wheelGroup,
                pointer,
                wheelButton,
                centerLabel
        );

        VBox centerPanel = new VBox(
                15,
                wheelPanel,
                resultLabel
        );


        BorderPane.setMargin(leftPanel, new Insets(10));
        BorderPane.setMargin(centerPanel, new Insets(10));
        BorderPane.setMargin(rightPanel, new Insets(10));

        centerLabel.setMouseTransparent(true);

        // Spinning Animation
        RotateTransition spin = new RotateTransition();
        spin.setNode(wheelGroup);
        spin.setInterpolator(Interpolator.EASE_OUT);


        // Press center to Spin
        wheelButton.setOnMouseClicked(event -> {
            if (spin.getStatus() == Animation.Status.RUNNING) {
                return;
            }

            int seconds = spinTimeSpinner.getValue();

            double spinAngle = seconds * 720;

            spin.setDuration(Duration.seconds(seconds));
            spin.setByAngle(spinAngle);

            spin.playFromStart();
        });


        //Slices
        Color[] sliceColors = {
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.LIGHTGREEN,
                Color.DEEPSKYBLUE,
                Color.MEDIUMPURPLE
        };

        for (int i = 0; i < 6; i++) {
            Arc slice = new Arc();

            slice.setCenterX(0);
            slice.setCenterY(0);

            slice.setRadiusX(210);
            slice.setRadiusY(210);

            slice.setStartAngle(i * 60);
            slice.setLength(60);

            slice.setType(ArcType.ROUND);
            slice.setFill(sliceColors[i]);

            wheelGroup.getChildren().add(slice);
        }
        wheel.toFront();


        centerLabel.setTextFill(Color.WHITE);
        pointer.setTranslateY(-210);
        // Outer Wheel
        wheel.setFill(Color.TRANSPARENT);
        wheel.setStroke(Color.DARKBLUE);
        wheel.setStrokeWidth(4);


        // Inside Wheel
        wheelButton.setFill(Color.LIGHTGREEN);
        wheelButton.setStroke(Color.DARKGREEN);
        wheelButton.setStrokeWidth(2);

        // MAIN
        root.setLeft(leftPanel);
        leftPanel.setPrefWidth(280);
        leftPanel.setMinWidth(280);
        leftPanel.setMaxWidth(280);
        leftPanel.setPadding(new Insets(10));

        root.setCenter(centerPanel);
        centerPanel.setPadding(new Insets(20));


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
    // Task Card
    private VBox createTaskCard(
            String name,
            String odds,
            int estimatedTime,
            String dueDate
    ) {

        Label title = new Label(name);
        Label oddsLabel = new Label(odds);

        HBox top = new HBox(15, title, oddsLabel);

        Label info = new Label(
                "Est: " + estimatedTime +
                        " min   Due: " + dueDate
        );

        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");

        editButton.setOnAction(event -> {

            TextInputDialog dialog = new TextInputDialog(title.getText());
            dialog.setTitle("Edit Task");
            dialog.setHeaderText("Edit task name");
            dialog.setContentText("Task Name:");

            dialog.showAndWait().ifPresent(newName -> {
                if (!newName.trim().isEmpty()) {
                    title.setText(newName.trim());
                }
            });
        });

        HBox buttons = new HBox(
                10,
                editButton,
                deleteButton
        );

        VBox card = new VBox(
                8,
                top,
                info,
                buttons
        );

        card.setPadding(new Insets(12));

        deleteButton.setOnAction(event -> {

            VBox taskList = (VBox) card.getParent();

            if (taskList != null) {
                taskList.getChildren().remove(card);
            }
        });

        card.setStyle(
                "-fx-border-color: #64748b;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-background-color: #ffffff;"
        );

        return card;
    }
    private Label createHistoryEntry(String name) {

        Label entry = new Label(name);
        entry.setPadding(new Insets(10));

        return entry;
    }

    public static void main(String[] args) {
        launch(args);
    }
}