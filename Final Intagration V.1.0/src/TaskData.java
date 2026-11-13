import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/*
-=[ About ]=-
The main data container object.
Most components will manipulate either a HashMap, List, or Set containing this object.

-=[ Usage ]=-
TaskData should be managed through an in-memory HashMap<TaskData, double>
where the key is the task and value is the roulette probability,
which is calculated using task.getWeight() / keys.size().

When the user's day changes, call updateWeight() on all TaskData. TODO: This is incorrect
*/

public class TaskData {
    private static LocalDateTime timeReference;  // Standardize so all weights are calculated from the same reference

    private String title;            // Task title
    private LocalDateTime deadline;  // Task due date (day only)
    private int estimatedTimeMin;    // User's estimated time to complete the task, in minutes
    private double weight;              // Weight value computed on task creation. This is NOT roulette probability!
    private TaskStatus status;       // Is the task in active tasks, in history, or selected?

    public TaskData(String title, LocalDateTime deadline, int estimatedTimeMin, TaskStatus status) {
        this.title = title;
        this.deadline = deadline.withSecond(0).withNano(0);
        this.estimatedTimeMin = estimatedTimeMin;
        this.status = status;
        calculateWeight();
    }

    public static void updateTimeReference() {
        timeReference = LocalDateTime.now();
    }

    public void calculateWeight() {
        if (timeReference == null) timeReference = LocalDateTime.now();

        long minutesRemaining = ChronoUnit.MINUTES.between(timeReference, deadline);
        long slackMinutes = minutesRemaining - estimatedTimeMin;

        // No time remains to work
        if (minutesRemaining <= 0 || slackMinutes <= 0) {
            weight = 100.0;
            return;
        }

        weight = 100.0 / (1.0 + (slackMinutes / 60.0));
    }


    public String getTitle() {
        return title;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public int getEstimatedTimeMin() {
        return estimatedTimeMin;
    }

    public double getWeight() {
        return weight;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
        calculateWeight();
    }

    public void setEstimatedTimeMin(int estimatedTimeMin) {
        this.estimatedTimeMin = estimatedTimeMin;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Title=" + title + " Due=" + deadline + " EstTime=" + estimatedTimeMin + " Weight=" + weight + " Status=" + status;
    }
}