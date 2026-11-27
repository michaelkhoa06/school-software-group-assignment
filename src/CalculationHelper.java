import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
-=[ About ]=-
Static helper methods for calculating probabilities and rolling tasks.

-=[ Usage ]=-
Used in Core component only.
Has a method for debugging probability logic.
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

    public static TaskData rollTaskFromProbabilities(Map<TaskData, Double> activeTasks, double reservedBreakProbability) {
        if (activeTasks == null || activeTasks.isEmpty()) {
            return null;
        }

        double randomRoll = Math.random();

        if (randomRoll < reservedBreakProbability) return null;

        double probabilitySum = reservedBreakProbability;
        double taskProbabilityBudget = 1.0 - reservedBreakProbability;
        TaskData lastTask = null;

        for (Map.Entry<TaskData, Double> entry :
                activeTasks.entrySet()) {

            lastTask = entry.getKey();

            probabilitySum += entry.getValue() * taskProbabilityBudget;

            if (randomRoll < probabilitySum) {
                return entry.getKey();
            }
        }

        // Protect against small floating-point rounding differences.
        return lastTask;
    }

    public static void testProbabilities(
            Map<TaskData, Double> activeTasks,
            double reservedBreakProbability) {

        int iterations = 100000;

        int breaks = 0;
        Map<TaskData, Integer> occurrences = new HashMap<>();

        for (TaskData task : activeTasks.keySet()) {
            occurrences.put(task, 0);
        }

        for (int i = 0; i < iterations; i++) {
            TaskData result = rollTaskFromProbabilities(
                    activeTasks,
                    reservedBreakProbability
            );

            if (result == null) {
                breaks++;
            } else {
                occurrences.merge(result, 1, Integer::sum);
            }
        }

        System.out.println("Results after " + iterations + " rolls:");
        System.out.println("Breaks: " + breaks +
                " (" + (breaks * 100.0 / iterations) + "%)");

        occurrences.forEach((task, count) ->
                System.out.println(
                        task.getTitle() + ": " + count +
                                " (" + (count * 100.0 / iterations) + "%)"
                )
        );
    }
}