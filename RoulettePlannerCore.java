import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
-=[ About ]=-
Coordinates program data between components by making it accessible, keeping it up-to-date, and saving it.
It is the single source of the program state.

-=[ Usage ]=-
Communication IN:
 - UI should notify this when a task is added, edited, deleted, or completed/failed/skipped using:
   addTask(), editActiveTask(), removeTaskFromActive(), removeTaskFromHistory(), markSelectedTaskAs().
 - UI also notifies when the user closes the program, and spins the wheel.
 - Reads JsonStorage on creation only.

Communication OUT:
 - CalculationHelper (static) to determine probabilities and roll tasks.
 - JsonStorage is given a DataSnapshot whenever a data change is made.
 - Shop TBD
 - UI is given a DataSnapshot whenever a data change is made.
*/

public class RoulettePlannerCore {
    private final JsonStorage storage;
    // TODO: shop?

    private TaskData selectedTask = null;
    private Map<TaskData, Double> activeTasks = new HashMap<>();
    private final Map<TaskData, Integer> taskHistory = new HashMap<>();
    private int pointBalance = 0;
    private final List<Boolean> purchases = new ArrayList<>(3);

    // Recalculates weights every 4 hours in case the user leaves it open for a long time
    private final ScheduledExecutorService weightRecalculator = Executors.newSingleThreadScheduledExecutor();

    public RoulettePlannerCore(JsonStorage storage) {
        this.storage = storage;
        loadFromStorage();

        weightRecalculator.scheduleAtFixedRate(
                () -> {
                    TaskData.updateTimeReference();

                    for (TaskData task : activeTasks.keySet()) {
                        task.calculateWeight();
                    }

                    CalculationHelper.calculateProbabilitiesAll(activeTasks.keySet());
                }, 4, 4, TimeUnit.HOURS
        );
    }


    // =================================================================
    // UI Listeners
    // =================================================================

    public void addTask(String title, LocalDateTime deadline, int estimatedTimeMin) {
        TaskData newTask = new TaskData(title, deadline, estimatedTimeMin, TaskStatus.ACTIVE);

        Set<TaskData> toCalculateProbability = activeTasks.keySet();
        toCalculateProbability.add(newTask);

        activeTasks = CalculationHelper.calculateProbabilitiesAll(toCalculateProbability);
        notifyStorage();
    }

    // Can use title="", deadline=null, estimatedTimeMin=-1 for no changes to each field, respectively
    public void editActiveTask(TaskData toEdit, String title, LocalDateTime deadline, int estimatedTimeMin) {
        if (toEdit == null || !activeTasks.containsKey(toEdit)) return;

        boolean doRecalculation = false;

        if (!title.isEmpty()) toEdit.setTitle(title);
        if (deadline != null) {
            if (deadline != toEdit.getDeadline()) {
                toEdit.setDeadline(deadline);
                doRecalculation = true;
            }
        }
        if (estimatedTimeMin >= 0) {
            if (estimatedTimeMin != toEdit.getEstimatedTimeMin()) {
                toEdit.setEstimatedTimeMin(estimatedTimeMin);
                doRecalculation = true;
            }
        }

        if (doRecalculation) {
            toEdit.calculateWeight();
            activeTasks = CalculationHelper.calculateProbabilitiesAll(activeTasks.keySet());
        }
    }

    public void removeTaskFromActive(TaskData task) {
        if (task == null) return;

        if (activeTasks.remove(task) != null) {
            if (selectedTask == task) selectedTask = null;
            activeTasks = CalculationHelper.calculateProbabilitiesAll(activeTasks.keySet());
            notifyStorage();
        }
    }

    public void removeTaskFromHistory(TaskData task) {
        if (task == null) return;
        if (taskHistory.remove(task) != null) notifyStorage();
    }

    public void markSelectedTaskAs(TaskStatus newStatus) {
        if (newStatus == TaskStatus.ACTIVE || selectedTask == null) return;

        if (!activeTasks.containsKey(selectedTask)) {
            selectedTask = null;
            return;
        }

        int pointReward = 0;
        switch (newStatus) {
            case COMPLETED -> pointReward = 5;
            case FAILED -> pointReward = -3;    // TODO: Are hardcoded point values okay?
            case SKIPPED -> pointReward = -7;
        }

        activeTasks.remove(selectedTask);
        selectedTask.setStatus(TaskStatus.COMPLETED);
        taskHistory.put(selectedTask, pointReward);

        pointBalance += pointReward;

        notifyStorage();
    }

    public void onClose() {
        weightRecalculator.shutdown();
        DataSnapshot snapshot = new DataSnapshot(null, activeTasks.keySet(), taskHistory, pointBalance, purchases);
        storage.saveNow(snapshot);
    }

    public TaskData onSpin() {
        if (selectedTask != null) return selectedTask;

        selectedTask = CalculationHelper.rollTaskFromProbabilities(activeTasks);

        notifyStorage();
        return selectedTask;
    }


    // =================================================================
    // Getters (may not be necessary)
    // =================================================================

    public TaskData getSelectedTask() {
        return selectedTask;
    }

    public Map<TaskData, Double> getActiveTasksWithProbabilities() {
        return Collections.unmodifiableMap(activeTasks);
    }

    public Map<TaskData, Integer> getTaskHistory() {
        return Collections.unmodifiableMap(taskHistory);
    }

    public int getPoints() {
        return pointBalance;
    }


    // =================================================================
    // Private Methods
    // =================================================================

    private void notifyUI() {
        DataSnapshot snapshot = new DataSnapshot(selectedTask, activeTasks, taskHistory, pointBalance, purchases);
        ui.updateData(snapshot);
    }

    private void notifyStorage() {
        DataSnapshot snapshot = new DataSnapshot(selectedTask, activeTasks, taskHistory, pointBalance, purchases);
        storage.updateData(snapshot);
    }

    private void loadFromStorage() {
        DataSnapshot snapshot = storage.load();

        activeTasks.clear();
        Set<TaskData> loadedActiveTasks = snapshot.activeTasks().keySet();
        for (TaskData task : loadedActiveTasks) {
            activeTasks.put(task, CalculationHelper.calculateProbability(task, activeTasks.size()));
        }

        taskHistory.clear();
        taskHistory.putAll(snapshot.taskHistory());

        pointBalance = snapshot.pointBalance();

        List<Boolean> loadedPurchases = snapshot.purchases();
        for (int i = 0; i < purchases.size(); i++) {
            boolean hasPurchased = i < loadedPurchases.size() ? loadedPurchases.get(i) : false;
            purchases.set(i, hasPurchased);
        }
    }
}
