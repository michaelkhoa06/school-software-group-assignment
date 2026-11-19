import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private final Config config;

    private final Group wheelGroup;
    private final Circle wheel;
    private final Circle wheelButton;

    private final RotateTransition spin;
    private final VBox centerPanel;

    private final Button completeTaskButton;
    private final Button skipTaskButton;

    private final Label resultLabel;
    private final Label breakWeightLabel;

    private String selectedTaskName;

    private final RoulettePlannerCore core;

    private final List<TaskData> currentTasks = new ArrayList<>();
    private final List<Double> currentProbabilities = new ArrayList<>();

    private int spinTimeSeconds;


    public RouletteWheel(Config config, RoulettePlannerCore core) {

        this.config = config;

        spinTimeSeconds = config.spinTimeSec();

        this.core = core;
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

        breakWeightLabel = new Label("BREAK WEIGHT: 20%"); // TODO: This is dynamic now. Make blank initially?

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
                breakWeightLabel,
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


        wheelButton.setOnMouseClicked(_ -> {

            if (spin.getStatus() == Animation.Status.RUNNING ||
                    selectedTaskName != null) {
                return;
            }

            int selectedIndex = currentTasks.size(); // Defaults to break
            TaskData selectedTask = core.spinWheelAndSelect();
            System.out.println(selectedTask);
            if (selectedTask != null) {
                selectedIndex = 0;
                for (TaskData task : currentTasks) {
                    if (selectedTask.equals(task)) break;
                    selectedIndex++;
                }
            }

            double winningAngle =
                    findMiddleAngleForTaskIndex(
                            selectedIndex,
                            currentProbabilities
                    );


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

            spin.setOnFinished(_ -> {

                if (selectedTask == null) {

                    selectedTaskName = null;

                    resultLabel.setText(
                            "BREAK - Take a break!"
                    );

                    completeTaskButton.setDisable(true);
                    skipTaskButton.setDisable(true);

                    return;
                }

                selectedTaskName =
                        selectedTask.getTitle();

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

                if (tasks.get(i).equals("BREAK")) {
                    slice.setFill(Color.LIGHTGRAY);
                } else {
                    slice.setFill(
                            colors[i % colors.length]
                    );
                }

                slice.setStroke(Color.WHITE); // TODO: Would it be easy to make it so when there are no tasks (only break) the white divider does not draw?
                slice.setStrokeWidth(2.5);

                wheelGroup.getChildren().add(slice);

                double middleAngle = startAngle + (sliceSize / 2);
                double radians = Math.toRadians(middleAngle);

                Text taskText = new Text(tasks.get(i)); // TODO: Text rendering on small slices gets cut off. Maybe clip to slice size, or truncate the string based on slice width?

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

    private double findMiddleAngleForTaskIndex(
            int selectedIndex,
            List<Double> weights
    ) {

        double totalWeight = 0;

        for (double weight : weights) {
            totalWeight += weight;
        }

        double currentAngle = 0;

        for (int i = 0; i < selectedIndex; i++) {

            currentAngle +=
                    360 * (weights.get(i) / totalWeight);
        }

        double selectedSliceSize =
                360 * (
                        weights.get(selectedIndex)
                                / totalWeight
                );

        return currentAngle +
                (selectedSliceSize / 2);
    }

    public void refreshWheel(TaskData selectedTask, Map<TaskData, Double> activeTasks) {

        // Update selected task to reflect Core state
        if (selectedTask == null) clearSelection();
        else selectedTaskName = selectedTask.getTitle();

        // Update wheel to reflect Core state
        currentTasks.clear();
        currentProbabilities.clear();

        List<String> taskNames = new ArrayList<>();

        double breakChance = core.getReservedBreakProbability();

        breakWeightLabel.setText(
                String.format(
                        "BREAK WEIGHT: %.0f%%",
                        breakChance * 100
                )
        );

        double taskProbabilityBudget = 1.0 - breakChance;

        for (Map.Entry<TaskData, Double> entry :
                activeTasks.entrySet()) {

            currentTasks.add(entry.getKey());

            currentProbabilities.add(
                    entry.getValue() * taskProbabilityBudget
            );

            taskNames.add(
                    entry.getKey().getTitle()
            );
        }

        // BREAK is always the final slice
        currentProbabilities.add(breakChance);
        taskNames.add("BREAK");

        drawWheel(
                wheelGroup,
                wheel,
                taskNames,
                currentProbabilities
        );
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

    public void clearSelection() {
        selectedTaskName = null;

        resultLabel.setText("Selected Task: --");

        completeTaskButton.setDisable(true);
        skipTaskButton.setDisable(true);
    }

    public void setSpinTime(int seconds) {
        spinTimeSeconds = seconds;
        config.edit(Config.Keys.SPIN_TIME, String.valueOf(seconds));
    }



}