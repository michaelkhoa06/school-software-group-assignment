import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


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



    public TaskPanel() {

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

            VBox parentList = (VBox) card.getParent();

            if (parentList != null) {
                parentList .getChildren().remove(card);
            }
        });

        card.setStyle(
                FRAME_STYLE +
                        "-fx-background-color: #ffffff;"
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

        taskScroll.setVisible(true);
        taskScroll.setManaged(true);
    }

    public void hideTasks() {
        taskForm.setVisible(false);
        taskForm.setManaged(false);

        taskScroll.setVisible(false);
        taskScroll.setManaged(false);
    }


}
