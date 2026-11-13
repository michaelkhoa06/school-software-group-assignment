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


    private Group wheelGroup;
    private Circle wheel;
    private Circle wheelButton;

    private RotateTransition spin;
    private VBox centerPanel;

    private Button completeTaskButton;
    private Button skipTaskButton;

    private Label resultLabel;
    private Label breakWeightLabel;

    private String selectedTaskName;

    private RoulettePlannerCore core;
    private Shop shop;

    private static final double BREAK_BASE_CHANCE = 0.20;

    private List<TaskData> currentTasks = new ArrayList<>();
    private List<Double> currentProbabilities = new ArrayList<>();

    private int spinTimeSeconds = 5;


    public RouletteWheel(RoulettePlannerCore core) {

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

        breakWeightLabel = new Label("BREAK WEIGHT: 20%");

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


        wheelButton.setOnMouseClicked(event -> {

            if (spin.getStatus() == Animation.Status.RUNNING ||
                    selectedTaskName != null) {
                return;
            }

            // Make sure the wheel uses the newest Core data
            refreshWheel();

            if (currentTasks.isEmpty()) {
                resultLabel.setText("No tasks available.");
                return;
            }

            int selectedIndex = rollOutcomeIndex();

            if (selectedIndex < 0) {
                return;
            }

            boolean breakSelected =
                    selectedIndex == currentTasks.size();

            TaskData rolledTask =
                    breakSelected
                            ? null
                            : currentTasks.get(selectedIndex);

            double winningAngle =
                    findMiddleAngleForTaskIndex(
                            selectedIndex,
                            currentProbabilities
                    );

            TaskData selectedTask;

            if (breakSelected) {
                selectedTask = null;
            } else {
                selectedTask = core.selectTask(rolledTask);

                if (selectedTask == null) {
                    return;
                }
            }


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

                if (breakSelected) {

                    selectedTaskName = null;

                    resultLabel.setText(
                            "BREAK - Take a break!"
                    );

                    completeTaskButton.setDisable(true);
                    skipTaskButton.setDisable(true);

                    return;
                }

                if (core.getSelectedTask() != selectedTask) {
                    clearSelection();
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

        refreshWheel();
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

                slice.setStroke(Color.WHITE);
                slice.setStrokeWidth(2.5);

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

    private int rollOutcomeIndex() {

        double randomRoll = Math.random();
        double total = 0.0;

        for (int i = 0; i < currentProbabilities.size(); i++) {

            total += currentProbabilities.get(i);

            if (randomRoll < total) {
                return i;
            }
        }

        return currentProbabilities.size() - 1;
    }

    public void refreshWheel() {

        Map<TaskData, Double> activeTasks =
                core.getActiveTasksWithProbabilities();

        currentTasks.clear();
        currentProbabilities.clear();

        List<String> taskNames = new ArrayList<>();

        if (activeTasks.isEmpty()) {
            drawWheel(
                    wheelGroup,
                    wheel,
                    taskNames,
                    currentProbabilities
            );
            return;
        }

        double breakChance = BREAK_BASE_CHANCE;

        if (shop != null) {
            breakChance *= shop.getBreakWeightMultiplier();
        }

        breakWeightLabel.setText(
                String.format(
                        "BREAK WEIGHT: %.0f%%",
                        breakChance * 100
                )
        );

        double taskChance = 1.0 - breakChance;

        for (Map.Entry<TaskData, Double> entry :
                activeTasks.entrySet()) {

            currentTasks.add(entry.getKey());

            currentProbabilities.add(
                    entry.getValue() * taskChance
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

    public String getSelectedTaskName() {
        return selectedTaskName;
    }

    public void clearSelection() {
        selectedTaskName = null;

        resultLabel.setText("Selected Task: --");

        completeTaskButton.setDisable(true);
        skipTaskButton.setDisable(true);
    }

    public void setShop(Shop shop) {
        this.shop = shop;
        refreshWheel();
    }

    public void setSpinTime(int seconds) {
        spinTimeSeconds = seconds;
    }



}
