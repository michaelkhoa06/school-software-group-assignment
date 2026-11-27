import java.time.LocalDateTime;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.geometry.Pos;

public class TaskPanel {


    private static final String FRAME_STYLE =
            "-fx-border-color: #cbd5e1;" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 10;" +
                    "-fx-background-radius: 10;" +
                    "-fx-background-color: #ffffff;";

    private final VBox taskPanel;
    private final VBox taskForm;
    private final VBox taskList;
    private final ScrollPane taskScroll;
    private final VBox activeTasksPanel;
    private final Label activeTaskCount;

    private final Button addTaskButton;
    private final Button historyButton;

    private final RoulettePlannerCore core;

    public TaskPanel(RoulettePlannerCore core) {

        this.core = core;

        Label leftLabel = new Label("TASKS:");
        leftLabel.setStyle("-fx-font-weight: bold;");

        addTaskButton = new Button("Add Task");
        historyButton = new Button("History");
        UIStyle.neutral(addTaskButton);
        UIStyle.neutral(historyButton);


        addTaskButton.setMaxWidth(Double.MAX_VALUE);
        historyButton.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(addTaskButton, Priority.ALWAYS);
        HBox.setHgrow(historyButton, Priority.ALWAYS);

        HBox navigationButtons = new HBox(
                10,
                addTaskButton,
                historyButton
        );

        VBox taskControls = new VBox(
                10,
                leftLabel,
                navigationButtons
        );

        Label newTaskLabel = new Label("NEW TASK ENTRY");
        newTaskLabel.setStyle("-fx-font-weight: bold;");

        Label taskNameLabel = new Label("Task Name");
        TextField taskNameField = new TextField();
        taskNameField.setPromptText("e.g., Math Homework");

        Label deadlineLabel = new Label("Deadline");
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setMaxWidth(Double.MAX_VALUE);

        Label estimatedTimeLabel = new Label("Estimated Completion Time (minutes)");
        Spinner<Integer> estimatedTimeSpinner = new Spinner<>(5, 480, 30, 5);
        estimatedTimeSpinner.setMaxWidth(Double.MAX_VALUE);



        Button addToWheelButton = new Button("Add to Wheel");
        UIStyle.primary(addToWheelButton);
        addToWheelButton.setMaxWidth(Double.MAX_VALUE);


        Label errorLabel = new Label("");
        errorLabel.setTextFill(Color.RED);

        taskForm = new VBox(
                6,
                newTaskLabel,
                taskNameLabel,
                taskNameField,
                deadlineLabel,
                deadlinePicker,
                estimatedTimeLabel,
                estimatedTimeSpinner,
                addToWheelButton,
                errorLabel
        );

        taskList = new VBox(10);

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
        taskScroll.setPrefViewportHeight(290);
        taskScroll.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(taskScroll, Priority.ALWAYS);

        taskScroll.setPadding(Insets.EMPTY);

        taskScroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: #ffffff;"
        );

        taskList.setStyle("-fx-background-color: #ffffff;");

        taskList.setPadding(new Insets(5));


        taskControls.setPadding(new Insets(12));

        taskControls.setStyle(FRAME_STYLE);

        taskForm.setPadding(new Insets(10));

        taskForm.setStyle(FRAME_STYLE);


        // Active Tasks header
        Label activeTasksTitle = new Label("ACTIVE TASKS");
        activeTasksTitle.setStyle("-fx-font-weight: bold;");

        activeTaskCount = new Label("0 tasks");
        activeTaskCount.setStyle("-fx-text-fill: #64748b;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        HBox activeTasksHeader = new HBox(
                8,
                activeTasksTitle,
                headerSpacer,
                activeTaskCount
        );

        activeTasksHeader.setAlignment(Pos.CENTER_LEFT);

        // Active Tasks panel
        activeTasksPanel = new VBox(
                10,
                activeTasksHeader,
                taskScroll
        );

        activeTasksPanel.setPadding(new Insets(10));
        activeTasksPanel.setStyle(FRAME_STYLE);

        VBox.setVgrow(activeTasksPanel, Priority.ALWAYS);


        taskPanel = new VBox(
                12,
                taskControls,
                taskForm,
                activeTasksPanel
        );
    }

    public void refreshTaskList(Map<TaskData, Double> activeTasks) {

        taskList.getChildren().clear();
        activeTaskCount.setText(
                activeTasks.size() +
                        (activeTasks.size() == 1 ? " task" : " tasks")
        );

        int colorIndex = 0;

        for (Map.Entry<TaskData, Double> entry : activeTasks.entrySet()) {

            TaskData task = entry.getKey();
            double breakChance = core.getReservedBreakProbability();

            double taskProbabilityBudget = 1.0 - breakChance;

            double actualProbability =
                    entry.getValue() * taskProbabilityBudget;

            String odds =
                    String.format(
                            "%.1f%%",
                            actualProbability * 100
                    );


            taskList.getChildren().add(
                    createTaskCard(
                            task,
                            odds,
                            RouletteWheel.getTaskColor(colorIndex++)
                    )
            );
        }
    }

    private VBox createTaskCard(
            TaskData task,
            String odds,
            String taskColor
    ) {

        String name = task.getTitle();
        int estimatedTime = task.getEstimatedTimeMin();

        String dueDate =
                task.getDeadline()
                        .toLocalDate()
                        .toString();


        Label title = new Label(name);
        title.setStyle("-fx-font-weight: bold;");
        title.setMaxWidth(Double.MAX_VALUE);

        Label oddsLabel = new Label(odds);
        oddsLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #475569;"
        );
        oddsLabel.setMinWidth(Region.USE_PREF_SIZE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(8, title, spacer, oddsLabel);
        top.setAlignment(Pos.CENTER_LEFT);


        Label info = new Label(
                "Est: " + estimatedTime +
                        " min   Due: " + dueDate
        );


        Button editButton = new Button("Edit");
        Button deleteButton = new Button("Delete");
        UIStyle.neutral(editButton);
        UIStyle.danger(deleteButton);


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

        card.setPadding(new Insets(10));

        deleteButton.setOnAction(event -> {

            core.removeTaskFromActive(task);

        });

        card.setStyle(
                FRAME_STYLE +
                        "-fx-border-color: #64748b #64748b #64748b " + taskColor + ";" +
                        "-fx-border-width: 1 1 1 6;"
        );

        return card;
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

        activeTasksPanel.setVisible(true);
        activeTasksPanel.setManaged(true);
    }

    public void hideTasks() {
        taskForm.setVisible(false);
        taskForm.setManaged(false);

        activeTasksPanel.setVisible(false);
        activeTasksPanel.setManaged(false);
    }


}