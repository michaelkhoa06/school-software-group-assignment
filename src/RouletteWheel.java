
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RouletteWheel {

    // Keeping the break chance at 10% for now, we can change it later.
    private static final double DEFAULT_BREAK_CHANCE = 0.10;
    private static final double FALLBACK_WEIGHT = 100.0;

    private final Random random;
    private double breakChance;

    public RouletteWheel() {
        this(new Random(), DEFAULT_BREAK_CHANCE);
    }

    // This also lets us use a specific Random object when testing.
    public RouletteWheel(Random random, double breakChance) {
        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null");
        }

        this.random = random;
        setBreakProbability(breakChance);
    }

    public void recalculateWeights(HashMap<TaskData, Double> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("Task map cannot be null");
        }

        // No reason to calculate anything if there are no assignments.
        if (tasks.isEmpty()) {
            return;
        }

        // TaskData already calculates the weights, so I'm just updating
        // them using the same time reference before finding probabilities.
        TaskData.updateTimeReference();

        double totalWeight = 0.0;

        // Adding all the assignment weights together so I can use
        // the total to calculate each assignment's probability.
        for (TaskData task : tasks.keySet()) {

            if (task == null || task.getDeadline() == null ||
                    task.getEstimatedTimeMin() <= 0) {
                throw new IllegalArgumentException(
                        "Each assignment needs a deadline and a positive estimated time");
            }

            task.calculateWeight();
            totalWeight += getValidWeight(task);
        }

        // Since 10% is for breaks, the assignments share the other 90%.
        double assignmentChance = 1.0 - breakChance;

        // Dividing each weight by the total to get its probability.
        // The probability goes in the HashMap since TaskData already
        // has its own weight that I don't want to overwrite.
        for (Map.Entry<TaskData, Double> entry : tasks.entrySet()) {

            double probability = assignmentChance *
                    (getValidWeight(entry.getKey()) / totalWeight);

            entry.setValue(probability);
        }
    }

    private double getValidWeight(TaskData task) {
        double weight = task.getWeight();

        // Just making sure the weight isn't negative or infinite.
        // Using 100 as a temporary value until we fix that issue in TaskData.
        if (!Double.isFinite(weight) || weight <= 0.0) {
            return FALLBACK_WEIGHT;
        }

        return weight;
    }

    public TaskData spin(HashMap<TaskData, Double> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalStateException(
                    "Add an assignment before spinning the wheel");
        }

        // Recalculating before each spin so the probabilities are updated.
        recalculateWeights(tasks);

        // Picking a random number between 0 and 1.
        double number = random.nextDouble();
        double currentChance = 0.0;

        TaskData lastTask = null;

        // Adding the probabilities until the random number falls
        // within one of the assignment's ranges.
        for (Map.Entry<TaskData, Double> entry : tasks.entrySet()) {

            lastTask = entry.getKey();
            currentChance += entry.getValue();

            if (number < currentChance) {
                return entry.getKey();
            }
        }

        // Since TaskData doesn't have a break option yet,
        // I'm returning null whenever the wheel lands on a break.
        if (breakChance > 0.0) {
            return null;
        }

        // This is just in case of any small rounding differences.
        return lastTask;
    }

    public void setBreakProbability(double probability) {

        // Making sure the break chance is valid and isn't 100%.
        // Otherwise there wouldn't be a chance to pick an assignment.
        if (!Double.isFinite(probability) ||
                probability < 0.0 || probability >= 1.0) {
            throw new IllegalArgumentException(
                    "Break chance must be from 0 up to (but not including) 1");
        }

        breakChance = probability;
    }

    public double getBreakProbability() {
        return breakChance;
    }

