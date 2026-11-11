import java.util.List;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class RouletteWheel {


    private Group wheelGroup;
    private Circle wheel;
    private Circle wheelButton;

    private RotateTransition spin;
    private VBox centerPanel;

    private Button completeTaskButton;
    private Button skipTaskButton;

    private Label resultLabel;

    private String selectedTaskName;

    private int spinTimeSeconds = 5;

    public RouletteWheel() {

        Label centerLabel = new Label("SPIN");

        wheel = new Circle(210);
        wheelButton = new Circle(60);

        wheelGroup = new Group(wheel);
        Polygon pointer = new Polygon(
                0, 0,
                40, 0,
                20, 60
        );

        pointer.setFill(Color.RED);
        pointer.setStroke(Color.BLACK);
        pointer.setStrokeWidth(2);
        pointer.setTranslateY(-210);

        resultLabel = new Label("Selected Task: --");

        completeTaskButton = new Button("Complete");
        skipTaskButton = new Button("Skip");

        completeTaskButton.setDisable(true);
        skipTaskButton.setDisable(true);

        HBox resultButtons = new HBox(
                10,
                completeTaskButton,
                skipTaskButton
        );

        StackPane wheelPanel = new StackPane(
                wheelGroup,
                pointer,
                wheelButton,
                centerLabel
        );


        centerPanel = new VBox(
                15,
                wheelPanel,
                resultLabel,
                resultButtons
        );

        spin = new RotateTransition();

        spin.setNode(wheelGroup);
        spin.setInterpolator(
                Interpolator.SPLINE(
                        0.10, 0.80,
                        0.20, 1.00
                )
        );


        List<String> testTasks = List.of(
                "Task 1",
                "Task 2",
                "Task 3",
                "Task 4"
        );

        List<Double> testWeights = List.of(
                5.0,
                1.0,
                1.0,
                1.0
        );

        wheelButton.setOnMouseClicked(event -> {

            if (spin.getStatus() == Animation.Status.RUNNING ||
                    selectedTaskName != null) {
                return;
            }

            // Pick a completely random point on the wheel
            double winningAngle = Math.random() * 360;

            // Find which task owns that point
            int selectedIndex =
                    findTaskIndexForAngle(
                            winningAngle,
                            testWeights
                    );

            // Rotate that exact point to the pointer
            double targetRotation =
                    winningAngle - 90;

            double currentRotation =
                    wheelGroup.getRotate() % 360;

            if (currentRotation < 0) {
                currentRotation += 360;
            }

            targetRotation =
                    ((targetRotation % 360) + 360) % 360;

            double extraRotation =
                    targetRotation - currentRotation;

            if (extraRotation < 0) {
                extraRotation += 360;
            }

            int seconds = spinTimeSeconds;


            double spinAngle =
                    (seconds * 720) + extraRotation;

            spin.setDuration(Duration.seconds(seconds));
            spin.setByAngle(spinAngle);

            spin.setOnFinished(e -> {
                selectedTaskName =
                        testTasks.get(selectedIndex);

                resultLabel.setText(
                        "Selected Task: " +
                                selectedTaskName
                );

                completeTaskButton.setDisable(false);
                skipTaskButton.setDisable(false);
            });

            spin.playFromStart();
        });

        centerLabel.setTextFill(Color.WHITE);

        wheel.setFill(Color.TRANSPARENT);
        wheel.setStroke(Color.DARKBLUE);
        wheel.setStrokeWidth(4);

        wheelButton.setFill(Color.LIGHTGREEN);
        wheelButton.setStroke(Color.DARKGREEN);
        wheelButton.setStrokeWidth(2);

        drawWheel(
                wheelGroup,
                wheel,
                testTasks,
                testWeights
        );

    }


        private void drawWheel(
                Group wheelGroup,
                Circle wheel,
                List < String > tasks,
                List < Double > weights
    ){

            wheelGroup.getChildren().clear();
            wheelGroup.getChildren().add(wheel);

            Color[] colors = {
                    Color.RED,
                    Color.ORANGE,
                    Color.YELLOW,
                    Color.LIGHTGREEN,
                    Color.DEEPSKYBLUE,
                    Color.MEDIUMPURPLE
            };

            double totalWeight = 0;

            for (double weight : weights) {
                totalWeight += weight;
            }

            double startAngle = 0;

            for (int i = 0; i < tasks.size(); i++) {

                double sliceSize =
                        360 * (weights.get(i) / totalWeight);

                Arc slice = new Arc(
                        0,
                        0,
                        210,
                        210,
                        startAngle,
                        sliceSize
                );

                slice.setType(ArcType.ROUND);
                slice.setFill(
                        colors[i % colors.length]
                );

                wheelGroup.getChildren().add(slice);

                double middleAngle = startAngle + (sliceSize / 2);
                double radians = Math.toRadians(middleAngle);

                Text taskText = new Text(tasks.get(i));

                double textX = 135 * Math.cos(radians);
                double textY = -135 * Math.sin(radians);

                taskText.setX(
                        textX - taskText.getLayoutBounds().getWidth() / 2
                );

                taskText.setY(textY);

                wheelGroup.getChildren().add(taskText);


                startAngle += sliceSize;
            }

            wheel.toFront();
        }

        private int findTaskIndexForAngle(
            double angle,
            List<Double> weights
        ){

            double totalWeight = 0;

            for (double weight : weights) {
                totalWeight += weight;
            }

            double currentAngle = 0;

            for (int i = 0; i < weights.size(); i++) {

                double sliceSize =
                        360 * (weights.get(i) / totalWeight);

                if (angle >= currentAngle &&
                        angle < currentAngle + sliceSize) {

                    return i;
                }

                currentAngle += sliceSize;
            }

            return weights.size() - 1;
        }

    public VBox getView() {
        return centerPanel;
    }

    public Button getCompleteButton() {
        return completeTaskButton;
    }

    public Button getSkipButton() {
        return skipTaskButton;
    }

    public String getSelectedTaskName() {
        return selectedTaskName;
    }

    public void clearSelection() {
        selectedTaskName = null;

        resultLabel.setText("Selected Task: --");

        completeTaskButton.setDisable(true);
        skipTaskButton.setDisable(true);
    }

    public void setSpinTime(int seconds) {
        spinTimeSeconds = seconds;
    }


}
