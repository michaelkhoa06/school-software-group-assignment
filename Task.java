
public class Task {

    String title;
    String dueDate;
    String severity;
    boolean completed;

    // Creates a new task
    public Task(String title, String dueDate, String severity) {
        this.title = title;
        this.dueDate = dueDate;
        this.severity = severity;
        this.completed = false;
    }

    // Marks a task as completed
    public void markComplete() {
        completed = true;
    }

    // Displays the task information
    public String toString() {
        return title + " | Due: " + dueDate
                + " | Severity: " + severity
                + " | Completed: " + completed;
    }
}
