import java.time.LocalDateTime;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;

public class TaskPanel {


    private static final String FRAME_STYLE =
            "-fx-border-color: #64748b;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;";


    private VBox taskPanel;
    private VBox taskForm;
    private VBox taskList;
    private ScrollPane taskScroll;

    private Button addTaskButton;
    private Button historyButton;

    private RoulettePlannerCore core;

    private Shop shop;

    private static final double BREAK_BASE_CHANCE = 0.20;

    public TaskPanel(RoulettePlannerCore core) {

        this.core = core;

        Label leftLabel = new Label("TASKS:");

        addTaskButton = new Button("Add Task");
        historyButton = new Button("History");

        VBox taskControls = new VBox(
                10,
                leftLabel,
                addTaskButton,
                historyButton
        );

        Label newTaskLabel = new Label("NEW TASK ENTRY");

        Label taskNameLabel = new Label("Task Name");
        TextField taskNameField = new TextField();
        taskNameField.setPromptText("e.g., Math Homework");

        Label deadlineLabel = new Label("Deadline");
        DatePicker deadlinePicker = new DatePicker();

        Label estimatedTimeLabel = new Label("Estimated Completion Time (minutes)");
        Spinner<Integer> estimatedTimeSpinner = new Spinner<>(5, 480, 30, 5);



        Label weightLabel = new Label("Calculated Weight: --");

        Runnable updateWeightPreview = () -> {

            if (deadlinePicker.getValue() == null) {
                weightLabel.setText("Calculated Weight: --");
                return;
            }

            LocalDateTime deadline =
                    deadlinePicker.getValue().atTime(23, 59);

            int estimatedTime =
                    estimatedTimeSpinner.getValue();

            TaskData previewTask =
                    new TaskData(
                            "Preview",
                            deadline,
                            estimatedTime,
                            TaskStatus.ACTIVE
                    );

            weightLabel.setText(
                    String.format(
                            "Calculated Weight: %.2f",
                            previewTask.getWeight()
                    )
            );
        };

        deadlinePicker.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateWeightPreview.run()
        );

        estimatedTimeSpinner.valueProperty().addListener(
                (observable, oldValue, newValue) ->
                        updateWeightPreview.run()
        );


        Button addToWheelButton = new Button("Add to Wheel");

        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);

        taskForm = new VBox(
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

        taskList = new VBox(10);
        refreshTaskList();

        addToWheelButton.setOnAction(event -> {

            String name = taskNameField.getText().trim();

            if (name.isEmpty() || deadlinePicker.getValue() == null) {
                errorLabel.setText("Enter task name and deadline.");
                return;
            }

            int estimatedTime = estimatedTimeSpinner.getValue();

            LocalDateTime deadline =
                    deadlinePicker.getValue().atTime(23, 59);

            core.addTask(
                    name,
                    deadline,
                    estimatedTime
            );

            // Clear form
            taskNameField.clear();
            deadlinePicker.setValue(null);
            estimatedTimeSpinner.getValueFactory().setValue(30);

            errorLabel.setText("");
        });

        taskScroll = new ScrollPane(taskList);
        taskScroll.setFitToWidth(true);
        taskScroll.setPrefViewportHeight(150);

        taskScroll.setPadding(new Insets(12));

        taskScroll.setStyle(
                FRAME_STYLE +
                        "-fx-background-color: transparent;"
        );
        taskList.setPadding(new Insets(5));


        taskControls.setPadding(new Insets(12));

        taskControls.setStyle(FRAME_STYLE);

        taskForm.setPadding(new Insets(12));

        taskForm.setStyle(FRAME_STYLE);


        taskPanel = new VBox(
                12,
                taskControls,
                taskForm,
                taskScroll
        );
    }

    private void refreshTaskList() {

        taskList.getChildren().clear();

        for (Map.Entry<TaskData, Double> entry :
                core.getActiveTasksWithProbabilities().entrySet()) {

            TaskData task = entry.getKey();
            double breakChance = BREAK_BASE_CHANCE;

            if (shop != null) {
                breakChance *= shop.getBreakWeightMultiplier();
            }

            double taskChance = 1.0 - breakChance;

            double actualProbability =
                    entry.getValue() * taskChance;

            String odds =
                    String.format(
                            "%.1f%%",
                            actualProbability * 100
                    );


            taskList.getChildren().add(
                    createTaskCard(
                            task,
                            odds
                    )
            );
        }
    }

    private VBox createTaskCard(
            TaskData task,
            String odds
    ) {

        String name = task.getTitle();
        int estimatedTime = task.getEstimatedTimeMin();

        String dueDate =
                task.getDeadline()
                        .toLocalDate()
                        .toString();


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

            Dialog<ButtonType> dialog = new Dialog<>();

            dialog.setTitle("Edit Task");
            dialog.setHeaderText("Edit task information");

            TextField nameField =
                    new TextField(task.getTitle());

            DatePicker deadlineField =
                    new DatePicker(
                            task.getDeadline().toLocalDate()
                    );

            Spinner<Integer> timeField =
                    new Spinner<>(
                            5,
                            480,
                            task.getEstimatedTimeMin(),
                            5
                    );

            Label editWeightLabel = new Label();

            Runnable updateEditWeight = () -> {

                if (deadlineField.getValue() == null) {
                    editWeightLabel.setText("Calculated Weight: --");
                    return;
                }

                LocalDateTime previewDeadline =
                        deadlineField.getValue().atTime(23, 59);

                int previewTime =
                        timeField.getValue();

                TaskData previewTask =
                        new TaskData(
                                "Preview",
                                previewDeadline,
                                previewTime,
                                TaskStatus.ACTIVE
                        );

                editWeightLabel.setText(
                        String.format(
                                "Calculated Weight: %.2f",
                                previewTask.getWeight()
                        )
                );
            };

            deadlineField.valueProperty().addListener(
                    (observable, oldValue, newValue) ->
                            updateEditWeight.run()
            );

            timeField.valueProperty().addListener(
                    (observable, oldValue, newValue) ->
                            updateEditWeight.run()
            );

            updateEditWeight.run();

            GridPane grid = new GridPane();

            grid.setHgap(10);
            grid.setVgap(10);

            grid.add(
                    new Label("Task Name:"),
                    0,
                    0
            );

            grid.add(
                    nameField,
                    1,
                    0
            );

            grid.add(
                    new Label("Deadline:"),
                    0,
                    1
            );

            grid.add(
                    deadlineField,
                    1,
                    1
            );

            grid.add(
                    new Label("Estimated Time:"),
                    0,
                    2
            );

            grid.add(
                    timeField,
                    1,
                    2
            );

            grid.add(
                    editWeightLabel,
                    1,
                    3
            );

            dialog.getDialogPane().setContent(grid);

            dialog.getDialogPane()
                    .getButtonTypes()
                    .addAll(
                            ButtonType.OK,
                            ButtonType.CANCEL
                    );

            dialog.showAndWait().ifPresent(result -> {

                if (result != ButtonType.OK) {
                    return;
                }

                String newName =
                        nameField.getText().trim();

                if (newName.isEmpty() ||
                        deadlineField.getValue() == null) {
                    return;
                }

                LocalDateTime newDeadline =
                        deadlineField
                                .getValue()
                                .atTime(23, 59);

                int newEstimatedTime =
                        timeField.getValue();

                core.editActiveTask(
                        task,
                        newName,
                        newDeadline,
                        newEstimatedTime
                );
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

            core.removeTaskFromActive(task);

        });

        card.setStyle(
                FRAME_STYLE +
                        "-fx-background-color: #ffffff;"
        );

        return card;
    }

    public void refreshTasks() {
        refreshTaskList();
    }

    public VBox getView() {
        return taskPanel;
    }

    public Button getAddTaskButton() {
        return addTaskButton;
    }

    public Button getHistoryButton() {
        return historyButton;
    }

    public void showTasks() {
        taskForm.setVisible(true);
        taskForm.setManaged(true);

        taskScroll.setVisible(true);
        taskScroll.setManaged(true);
    }

    public void hideTasks() {
        taskForm.setVisible(false);
        taskForm.setManaged(false);

        taskScroll.setVisible(false);
        taskScroll.setManaged(false);
    }

    public void setShop(Shop shop) {
        this.shop = shop;
        refreshTaskList();
    }


}
