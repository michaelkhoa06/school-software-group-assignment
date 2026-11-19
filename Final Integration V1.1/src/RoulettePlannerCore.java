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
 - Shop is told when to buy an upgrade.
 - Shop is given point count and purchase list during loading.
 - UI is given a DataSnapshot whenever a data change is made.
*/

public class RoulettePlannerCore {
    private final JsonStorage storage;
    private UIController ui;
    private final Shop shop;

    private TaskData selectedTask = null;
    private Map<TaskData, Double> activeTasks = new HashMap<>();
    private final Map<TaskData, Integer> taskHistory = new HashMap<>();

    // Recalculates weights every 4 hours in case the user leaves it open for a long time
    private final ScheduledExecutorService weightRecalculator = Executors.newSingleThreadScheduledExecutor();

    public RoulettePlannerCore(JsonStorage storage) {
        this.storage = storage;
        this.shop = new Shop();

        loadFromStorage();

        weightRecalculator.scheduleAtFixedRate(
                () -> {
                    TaskData.updateTimeReference();

                    for (TaskData task : activeTasks.keySet()) {
                        task.calculateWeight();
                    }

                    activeTasks = CalculationHelper.calculateProbabilitiesAll(activeTasks.keySet());
                }, 4, 4, TimeUnit.HOURS
        );
        System.out.println("Updated active tasks' time reference.");
    }


    // =================================================================
    // Communication Setter (run ASAP)
    // =================================================================

    public void setUI(UIController ui) {
        this.ui = ui;
        notifyUI();
    }


    // =================================================================
    // UI Listeners
    // =================================================================

    public void addTask(String title, LocalDateTime deadline, int estimatedTimeMin) {
        TaskData newTask = new TaskData(title, deadline, estimatedTimeMin, TaskStatus.ACTIVE);

        Set<TaskData> toCalculateProbability = new HashSet<>(activeTasks.keySet());
        toCalculateProbability.add(newTask);

        activeTasks = CalculationHelper.calculateProbabilitiesAll(toCalculateProbability);
        notifyStorage();
        notifyUI();
    }

    // Can use title="", deadline=null, estimatedTimeMin=-1 for no changes to each field, respectively
    public void editActiveTask(TaskData toEdit, String title, LocalDateTime deadline, int estimatedTimeMin) {
        if (toEdit == null || !activeTasks.containsKey(toEdit)) return;

        boolean doRecalculation = false;

        if (!title.isEmpty()) toEdit.setTitle(title);

        if (deadline != null) {
            if (!deadline.equals(toEdit.getDeadline())) {
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

        notifyStorage();
        notifyUI();
    }

    public void removeTaskFromActive(TaskData task) {
        if (task == null) return;

        if (activeTasks.remove(task) != null) {
            if (selectedTask == task) selectedTask = null;
            activeTasks = CalculationHelper.calculateProbabilitiesAll(activeTasks.keySet());
            notifyStorage();
            notifyUI();
        }
    }

    // TODO: Should this take an index instead?
    public void removeTaskFromHistory(TaskData task) {
        if (task == null) return;
        if (taskHistory.remove(task) != null) notifyStorage(); notifyUI();
    }

    public void markSelectedTaskAs(TaskStatus newStatus) {
        if (newStatus == TaskStatus.ACTIVE || selectedTask == null) return;

        if (!activeTasks.containsKey(selectedTask)) {
            selectedTask = null;
            return;
        }

        switch (newStatus) {
            case COMPLETED -> {
                activeTasks.remove(selectedTask);
                selectedTask.setStatus(newStatus);
                taskHistory.put(selectedTask, shop.completeTask());
                selectedTask = null;
            }

            case FAILED -> {
                activeTasks.remove(selectedTask);
                selectedTask.setStatus(newStatus);
                taskHistory.put(selectedTask, shop.failTask());
                selectedTask = null;
            }

            case SKIPPED -> {
                taskHistory.put(new TaskData(
                        selectedTask.getTitle(),
                        selectedTask.getDeadline(),
                        selectedTask.getEstimatedTimeMin(),
                        TaskStatus.SKIPPED
                ),shop.skipTask());
                selectedTask = null;
            }
        }

        activeTasks = CalculationHelper.calculateProbabilitiesAll(activeTasks.keySet());

        notifyStorage();
        notifyUI();
    }

    // Returns null if a break is selected
    public TaskData spinWheelAndSelect() {
        // Skipped guards against re-spinning with a task already selected. UI is responsible for blocking button click
        selectedTask = CalculationHelper.rollTaskFromProbabilities(activeTasks, getReservedBreakProbability());
        notifyStorage();
        return selectedTask;
    }

    public boolean makePurchase(Shop.ShopUpgradeIndices upgrade) {
        if (shop.buyUpgrade(upgrade)) {
            notifyUI();
            notifyStorage();
            return true;
        }
        return false;
    }

    public void onClose() {
        weightRecalculator.shutdown();
        DataSnapshot snapshot = createSnapshot();

        storage.saveNow(snapshot);
    }


    // =================================================================
    // Getters (avoid using)
    // =================================================================

    public int getPoints() {
        return shop.getBalance();
    }

    public boolean getUpgradeStatus(Shop.ShopUpgradeIndices upgrade) {
        return shop.isUpgradeBought(upgrade);
    }

    // Break probability is in the range [0.05, 0.40] (without upgrade), [0.0625, 0.5] (with upgrade), or [1.0, 1.0] (no tasks)
    public double getReservedBreakProbability() {
        if (activeTasks.isEmpty()) return 1.0;
        double totalWeight = activeTasks.keySet().stream().mapToDouble(TaskData::getWeight).sum();
        double averageWeight = totalWeight / activeTasks.size();

        return Math.clamp(0.4 - averageWeight / 100.0 * 0.2, 0.05, 0.4) * shop.getBreakWeightMultiplier();
    }


    // =================================================================
    // Private Methods
    // =================================================================

    private void notifyUI() {
        if (ui != null) ui.updateData(createSnapshot());
    }

    private void notifyStorage() {
        storage.updateData(createSnapshot());
    }

    private void loadFromStorage() {
        DataSnapshot snapshot = storage.load();

        activeTasks = CalculationHelper.calculateProbabilitiesAll(snapshot.activeTasks().keySet());

        taskHistory.clear();
        taskHistory.putAll(snapshot.taskHistory());

        shop.loadData(snapshot.pointBalance(), snapshot.purchases());
    }

    private DataSnapshot createSnapshot() {
        return new DataSnapshot(
                selectedTask,
                new HashMap<>(activeTasks),
                new HashMap<>(taskHistory),
                shop.getBalance(),
                shop.getPurchaseList()
        );
    }
}