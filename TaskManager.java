import java.time.LocalDateTime;
import java.util.HashMap;

/*
 * Manages TaskData objects using the shared HashMap from the other teammates.
 *
 * The Double value is reserved for the roulette probability.
 * The teammate working on the roulette system will calculate/update it.
 */
public class TaskManager {

    private final HashMap<TaskData, Double> tasks;
    private int points = 0;

    // Connect to the HashMap created by another teammate.
    public TaskManager(HashMap<TaskData, Double> partnerTasks) {
        if (partnerTasks == null) {
            throw new IllegalArgumentException("Task HashMap cannot be null.");
        }
        tasks = partnerTasks;
    }

    // Can also start the app with an empty HashMap.
    public TaskManager() {
        this(new HashMap<TaskData, Double>());
    }

    // Add a new task using TaskData's existing constructor.
    public TaskData addTask(String title, LocalDateTime deadline, int estimatedTimeMin) {
        TaskData newTask = new TaskData(title, deadline, estimatedTimeMin, TaskStatus.ACTIVE);
        tasks.put(newTask, 0.0); // Roulette teammate will set the probability.
        return newTask;
    }

    // Add a TaskData object that a teammate has already created.
    public boolean addTask(TaskData task) {
        if (task == null || tasks.containsKey(task)) {
            return false;
        }
        tasks.put(task, 0.0); // Placeholder until the roulette probability is calculated.
        return true;
    }

    // Mark an ACTIVE task as completed and award points only once.
    public boolean completeTask(TaskData task) {
        if (task == null || !tasks.containsKey(task)) {
            return false;
        }
        if (task.getStatus() != TaskStatus.ACTIVE) {
            return false; // Already completed, failed, or skipped.
        }

        task.setStatus(TaskStatus.COMPLETED);
        tasks.put(task, 0.0); // A completed task should not be selected again.
        points += 10;
        return true;
    }

    // Remove a task from the HashMap completely.
    public boolean removeTask(TaskData task) {
        if (task == null || !tasks.containsKey(task)) {
            return false;
        }
        tasks.remove(task);
        return true;
    }

    // Return the SAME HashMap used by the other teammates.
    public HashMap<TaskData, Double> getTasks() {
        return tasks;
    }

    // The points shop can read the total earned points.
    public int getPoints() {
        return points;
    }
}
