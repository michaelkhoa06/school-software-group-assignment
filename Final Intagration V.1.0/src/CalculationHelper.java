import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
-=[ About ]=-
Static helper methods for calculating probabilities and rolling tasks.

-=[ Usage ]=-
Used in Core component only.
*/

public class CalculationHelper {
    private CalculationHelper() {}

    public static Map<TaskData, Double> calculateProbabilitiesAll(Set<TaskData> activeTasks) {
        Map<TaskData, Double> probabilities = new HashMap<>();
        if (activeTasks == null || activeTasks.isEmpty()) {
            return probabilities;
        }

        double totalWeight = 0.0;
        for (TaskData task : activeTasks) {
            totalWeight += task.getWeight();
        }

        if (totalWeight <= 0.0) {

            double equalProbability =
                    1.0 / activeTasks.size();

            for (TaskData task : activeTasks) {
                probabilities.put(
                        task,
                        equalProbability
                );
            }

            return probabilities;
        }


        for (TaskData task : activeTasks) {

            double probability =
                    task.getWeight() / totalWeight;

            probabilities.put(
                    task,
                    probability
            );
        }

        return probabilities;
    }

    public static TaskData rollTaskFromProbabilities(Map<TaskData, Double> activeTasks) {
        if (activeTasks == null || activeTasks.isEmpty()) {
            return null;
        }

        double randomRoll = Math.random();
        double probabilitySum = 0.0;
        TaskData lastTask = null;

        for (Map.Entry<TaskData, Double> entry :
                activeTasks.entrySet()) {

            lastTask = entry.getKey();

            probabilitySum += entry.getValue();

            if (randomRoll < probabilitySum) {
                return entry.getKey();
            }
        }

        // Protect against small floating-point rounding differences.
        return lastTask;
    }
}
