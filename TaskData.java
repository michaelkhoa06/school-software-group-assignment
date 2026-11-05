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

When the user's day changes, call updateWeight() on all TaskData.
*/

public class TaskData {
    private String title;            // Task title
    private LocalDateTime deadline;  // Task due date (day only)
    private int estimatedTimeMin;    // User's estimated time to complete the task, in minutes
    private double weight;           // Weight value computed on task creation. This is NOT roulette probability!
    private TaskStatus status;       // Is the task in active tasks, in history, or selected?

    public TaskData(String title, LocalDateTime deadline, int estimatedTimeMin, TaskStatus status) {
        this.title = title;
        this.deadline = deadline.withSecond(0).withNano(0);
        this.estimatedTimeMin = estimatedTimeMin;
        this.status = status;
        updateWeight();
    }

    // Get current day, compare to deadline and estimated time to complete, calculate new weight
    public void updateWeight() {
        LocalDateTime now = LocalDateTime.now().plusMinutes(estimatedTimeMin);
        long minutesBetween = ChronoUnit.MINUTES.between(now, deadline);
        long minutesBetweenMinusCompletionTime = minutesBetween - estimatedTimeMin;

        // Overdue with no time to work
        if (minutesBetween <= 0) {
            weight = 100; // placeholder
        }

        // Less time available than estimated time to complete
        else if (minutesBetweenMinusCompletionTime < 0) {
            weight = 100; // placeholder
        }

        // TODO: Loop over 4 hour intervals until minutesBetweenMinusCompletionTime is less.
        //  The greater the number of iterations, the less the weight. Stop after a certain number to stop massive loops.
        int minutesInterval = 240;
        int count = 0;
        while (minutesBetweenMinusCompletionTime > minutesInterval && count < 42) {
            minutesInterval += 240; // Add 4 hours (in minutes)
            count++;
        }

        // TODO: Calculate this somehow using minutesInterval % 240 = count of 4-hour intervals
        weight = minutesInterval % 240; // placeholder
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
    }

    public void setEstimatedTimeMin(int estimatedTimeMin) {
        this.estimatedTimeMin = estimatedTimeMin;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
