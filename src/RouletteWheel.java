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
import javafx.scene.shape.Rectangle;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;

public class RouletteWheel {

    private static final String[] TASK_COLORS = {
            "#ef4444",
            "#f59e0b",
            "#eab308",
            "#22c55e",
            "#38bdf8",
            "#a78bfa"
    };

    public static String getTaskColor(int index) {
        return TASK_COLORS[index % TASK_COLORS.length];
    }

    private final Config config;

    private final Group wheelGroup;
    private final Circle wheel;
    private final Button wheelButton;

    private final RotateTransition spin;
    private final VBox centerPanel;

    private final Button completeTaskButton;
    private final Button skipTaskButton;
    private final Button failTaskButton;

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

        wheel = new Circle(210);

        wheelButton = new Button("SPIN");

        wheelButton.setMinSize(120, 120);
        wheelButton.setPrefSize(120, 120);
        wheelButton.setMaxSize(120, 120);

        wheelButton.setStyle(
                "-fx-background-color: #86efac;" +
                        "-fx-background-radius: 60;" +
                        "-fx-border-color: #166534;" +
                        "-fx-border-radius: 60;" +
                        "-fx-border-width: 2;" +
                        "-fx-text-fill: #14532d;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        wheelGroup = new Group(wheel);
        Polygon pointer = new Polygon(
                0, 0,
                40, 0,
                20, 60
        );

        pointer.setFill(Color.web("#dc2626"));
        pointer.setStroke(Color.web("#991b1b"));
        pointer.setStrokeWidth(2);
        pointer.setTranslateY(-210);

        resultLabel = new Label("Selected Task: --");
        resultLabel.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #1e293b;"
        );

        breakWeightLabel = new Label("BREAK CHANCE: --");
        breakWeightLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #475569;"
        );

        completeTaskButton = new Button("Complete");
        skipTaskButton = new Button("Skip");
        failTaskButton = new Button("Fail");
        completeTaskButton.setPrefSize(125, 40);
        skipTaskButton.setPrefSize(125, 40);
        failTaskButton.setPrefSize(125, 40);


        UIStyle.success(completeTaskButton);

        UIStyle.warning(skipTaskButton);

        UIStyle.danger(failTaskButton);

        completeTaskButton.setDisable(true);
        skipTaskButton.setDisable(true);
        failTaskButton.setDisable(true);

        completeTaskButton.setFont(
                javafx.scene.text.Font.font(14)
        );

        skipTaskButton.setFont(
                javafx.scene.text.Font.font(14)
        );

        failTaskButton.setFont(
                javafx.scene.text.Font.font(14)
        );



        HBox resultButtons = new HBox(
                15,
                completeTaskButton,
                skipTaskButton,
                failTaskButton
        );

        resultButtons.setAlignment(Pos.CENTER);

        StackPane wheelPanel = new StackPane(
                wheelGroup,
                pointer,
                wheelButton
        );



        centerPanel = new VBox(
                20,
                wheelPanel,
                breakWeightLabel,
                resultLabel,
                resultButtons
        );

        centerPanel.setAlignment(Pos.TOP_CENTER);

        spin = new RotateTransition();

        spin.setNode(wheelGroup);
        spin.setInterpolator(
                Interpolator.SPLINE(
                        0.10, 0.80,
                        0.20, 1.00
                )
        );


        wheelButton.setOnAction(_ -> {

            if (spin.getStatus() == Animation.Status.RUNNING ||
                    selectedTaskName != null) {
                return;
            }

            int selectedIndex = currentTasks.size(); // Defaults to break
            TaskData selectedTask = core.spinWheelAndSelect();

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
                    failTaskButton.setDisable(true);

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
                failTaskButton.setDisable(false);
            });

            spin.playFromStart();
        });



        wheel.setFill(Color.TRANSPARENT);
        wheel.setStroke(Color.web("#334155"));
        wheel.setStrokeWidth(3);

    }


        private void drawWheel(
                Group wheelGroup,
                Circle wheel,
                List < String > tasks,
                List < Double > weights
    ){

            wheelGroup.getChildren().clear();
            wheelGroup.getChildren().add(wheel);

            double totalWeight = 0;

            for (double weight : weights) {
                totalWeight += weight;
            }

            double startAngle = 0;
            Group wheelLabels = new Group();

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

                if (i == tasks.size() - 1) {
                    slice.setFill(Color.LIGHTGRAY);
                } else {
                    slice.setFill(Color.web(getTaskColor(i)));
                }

                if (tasks.size() > 1) {
                    slice.setStroke(Color.WHITE);
                    slice.setStrokeWidth(2.5);
                }
                wheelGroup.getChildren().add(slice);

                // Hide labels on slices that are too narrow
                if (sliceSize < 12) {
                    startAngle += sliceSize;
                    continue;
                }

                double middleAngle = startAngle + (sliceSize / 2);
                double radians = Math.toRadians(middleAngle);

                String displayName = tasks.get(i);

                if (displayName.length() > 12) {
                    displayName = displayName.substring(0, 12) + "...";
                }

                Text taskText = new Text(displayName);


                double textX = 135 * Math.cos(radians);
                double textY = -135 * Math.sin(radians);

                taskText.setX(
                        textX - taskText.getLayoutBounds().getWidth() / 2
                );

                taskText.setY(textY);

                Bounds bounds = taskText.getBoundsInLocal();

                Rectangle labelBackground = new Rectangle(
                        bounds.getMinX() - 6,
                        bounds.getMinY() - 4,
                        bounds.getWidth() + 12,
                        bounds.getHeight() + 8
                );

                labelBackground.setFill(Color.rgb(255, 255, 255, 0.9));
                labelBackground.setArcWidth(8);
                labelBackground.setArcHeight(8);

                taskText.setFill(Color.web("#1e293b"));

                boolean overlaps = wheelLabels.getChildren()
                        .stream()
                        .anyMatch(node -> node.getBoundsInParent()
                                .intersects(labelBackground.getBoundsInParent()));

                if (!overlaps) {
                    wheelLabels.getChildren().addAll(
                            labelBackground,
                            taskText
                    );
                }


                startAngle += sliceSize;
            }

            wheelGroup.getChildren().add(wheelLabels);
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
                        "BREAK CHANCE: %.1f%%",
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

    public Button getSpinButton() {return wheelButton;}

    public Button getCompleteButton() {
        return completeTaskButton;
    }

    public Button getSkipButton() {
        return skipTaskButton;
    }
    public Button getFailButton() {
        return failTaskButton;
    }

    public void clearSelection() {
        selectedTaskName = null;

        resultLabel.setText("Selected Task: --");

        completeTaskButton.setDisable(true);
        skipTaskButton.setDisable(true);
        failTaskButton.setDisable(true);
    }

    public void setSpinTime(int seconds) {
        spinTimeSeconds = seconds;
        config.edit(Config.Keys.SPIN_TIME, String.valueOf(seconds));
    }



}