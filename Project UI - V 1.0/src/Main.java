import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;


import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;

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

        // LEFT SIDE
        Label leftLabel = new Label("TASKS:");
        Button addTaskButton = new Button("Add Task");
        Button historyButton = new Button("History");

        Label historyTitle = new Label("HISTORY");

        Label completedTitle = new Label("Completed");
        Label completedTask = new Label("Math Homework +");

        Label failedTitle = new Label("Failed");
        Label failedTask = new Label("Programming Lab -");

        VBox historyPanel = new VBox(
                10,
                historyTitle,
                completedTitle,
                completedTask,
                failedTitle,
                failedTask
        );

        historyPanel.setVisible(false);
        historyPanel.setManaged(false);

        historyButton.setOnAction(event -> {
            boolean show = !historyPanel.isVisible();

            historyPanel.setVisible(show);
            historyPanel.setManaged(show);
        });

        addTaskButton.setOnAction(event -> {
            historyPanel.setVisible(false);
            historyPanel.setManaged(false);
        });

        VBox leftPanel = new VBox(10,
                leftLabel,
                addTaskButton,
                historyButton,
                historyPanel
        );



        Label centerLabel = new Label("SPIN");
        Label rightLabel = new Label("SETTINGS:");

        // RIGHT SIDE Points and shop controls
        Label pointsLabel = new Label("120 PTS");
        Button shopButton = new Button("Shop");
        HBox pointsPanel = new HBox(10, pointsLabel, shopButton);

        Label spinTimeLabel = new Label("SPIN TIME:");
        Spinner<Integer> spinTimeSpinner = new Spinner<>(1, 15, 5);

        VBox rightPanel = new VBox(15,
                pointsPanel,
                rightLabel,
                spinTimeLabel,
                spinTimeSpinner
        );

        // CENTER Wheel
        Circle wheel = new Circle(230);
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

        StackPane centerPanel = new StackPane(
                wheelGroup,
                pointer,
                wheelButton,
                centerLabel
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

            slice.setRadiusX(230);
            slice.setRadiusY(230);

            slice.setStartAngle(i * 60);
            slice.setLength(60);

            slice.setType(ArcType.ROUND);
            slice.setFill(sliceColors[i]);

            wheelGroup.getChildren().add(slice);
        }
        wheel.toFront();


        centerLabel.setTextFill(Color.WHITE);
        pointer.setTranslateY(-230);
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
        leftPanel.setPrefWidth(200);
        leftPanel.setMinWidth(200);
        leftPanel.setMaxWidth(200);
        leftPanel.setPadding(new Insets(10));

        root.setCenter(centerPanel);
        centerLabel.setPadding(new Insets(20));


        root.setRight(rightPanel);
        rightPanel.setPrefWidth(200);
        rightPanel.setMinWidth(200);
        rightPanel.setMaxWidth(200);
        rightPanel.setPadding(new Insets(10));


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