import java.util.*;

public class CalculationHelper {
    private CalculationHelper() {}

    public static Map<TaskData, Double> calculateProbabilitiesAll(Set<TaskData> activeTasks) {
        Map<TaskData, Double> out = new HashMap<>();

        double totalWeight = 0.0;
        for (TaskData task : activeTasks) {
            totalWeight += task.getWeight();
        }

        for (TaskData task : activeTasks) {
            double probability = task.getWeight() / totalWeight;
            out.put(task, probability);
        }

        return out;
    }

    public static double calculateProbability(TaskData targetTask, int totalActiveTasks) {
        return targetTask.getWeight() / totalActiveTasks;
    }

    public static TaskData rollTaskFromProbabilities(Map<TaskData, Double> activeTasks) {
        double randomRoll = Math.random();
        double weightSum = activeTasks.values().stream().mapToDouble(Double::doubleValue).sum();
        for (TaskData task : activeTasks.keySet()) {
            weightSum += activeTasks.get(task);
            if (randomRoll < weightSum) {
                return task;
            }
        }

        // There was a problem with the probabilities
        return null;
    }
}