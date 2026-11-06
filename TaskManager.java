import java.util.HashMap;

public class TaskManager {

    // Stores all tasks
    private HashMap<Integer, Task> tasks;

    // Gives every task a unique ID
    private int nextId = 1;

    // Tracks points for completed tasks
    private int points = 0;

    // Connects to the HashMap from a teammate
    public TaskManager(HashMap<Integer, Task> partnerTasks) {
        tasks = partnerTasks;

        // Find the next available task ID
        for (int id : tasks.keySet()) {
            if (id >= nextId) {
                nextId = id + 1;
            }
        }
    }

    // METHOD 1: Add a task
    public void addTask(String title, String dueDate, String severity) {

        Task newTask = new Task(title, dueDate, severity);

        tasks.put(nextId, newTask);

        System.out.println("Task added! ID: " + nextId);

        nextId++;
    }

    // METHOD 2: Complete a task
    public void completeTask(int id) {

        if (tasks.containsKey(id)) {

            Task task = tasks.get(id);

            if (!task.completed) {
                task.markComplete();
                points += 10;
                System.out.println("Task completed! +10 points");
            } else {
                System.out.println("Task is already completed.");
            }

        } else {
            System.out.println("Task not found.");
        }
    }

    // METHOD 3: Remove a task
    public void removeTask(int id) {

        if (tasks.containsKey(id)) {
            tasks.remove(id);
            System.out.println("Task removed!");
        } else {
            System.out.println("Task not found.");
        }
    }

    // Allows teammates to access all tasks
    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    // Allows the points shop to access earned points
    public int getPoints() {
        return points;
    }
}
