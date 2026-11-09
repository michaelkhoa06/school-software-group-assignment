import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

public class RouletteWheel {

    // I'm starting with a 10% chance of getting a break.
    // We can always change this when we put everyone's code together.
    private static final double DEFAULT_BREAK_CHANCE = 0.10;
    private static final double URGENCY_BOOST = 4.0;
    private static final double TIME_REFERENCE = 60.0;

    private Random random;
    private Clock clock;
    private Task breakTask;
    private double breakChance;

    public RouletteWheel() {
        this(new Random(), Clock.systemDefaultZone(), DEFAULT_BREAK_CHANCE);
    }

    // I added this constructor so we can test the probabilities using a fixed time.
    public RouletteWheel(Random random, Clock clock, double breakChance) {
        if (random == null || clock == null) {
            throw new IllegalArgumentException("Random and clock cannot be null");
        }

        this.random = random;
        this.clock = clock;
        this.breakTask = Task.createBreakTask();
        setBreakProbability(breakChance);
    }

    public void recalculateWeights(ArrayList<Task> tasks) {
        if (tasks == null) {
            throw new IllegalArgumentException("Task list cannot be null");
        }

        // If there aren't any assignments, there's nothing for the wheel to pick.
        if (tasks.isEmpty()) {
            breakTask.setWeight(0.0);
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        double[] scores = new double[tasks.size()];
        double totalScore = 0.0;

        // First, I'm giving every assignment a score based on its deadline
        // and how long it should take to finish. Higher score = better chance.
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);

            if (task == null || task.isBreak()) {
                throw new IllegalArgumentException("The list should only have active assignments");
            }
            if (task.getDeadline() == null || task.getEstimatedMinutes() <= 0) {
                throw new IllegalArgumentException("Every assignment needs a deadline and estimated time");
            }

            scores[i] = calculateScore(task, now);
            totalScore += scores[i];
        }

        // I'm saving 10% of the wheel for a break by default.
        // The assignments split the remaining 90% based on their scores.
        double assignmentChance = 1.0 - breakChance;

        for (int i = 0; i < tasks.size(); i++) {
            double probability = assignmentChance * (scores[i] / totalScore);
            tasks.get(i).setWeight(probability);
        }

        breakTask.setWeight(breakChance);
    }

    private double calculateScore(Task task, LocalDateTime now) {
        // An assignment that's due sooner should have a higher priority.
        // If it's already overdue, I'm treating it like it's due right now.
        double minutesLeft = Duration.between(now, task.getDeadline()).toMinutes();
        minutesLeft = Math.max(0.0, minutesLeft);
        double daysLeft = minutesLeft / 1440.0;

        double deadlineScore = 1.0 + URGENCY_BOOST / (1.0 + daysLeft);

        // I'm also considering the estimated time. If two assignments have
        // the same deadline, the shorter one gets a little extra priority.
        double timeScore = 1.0 + TIME_REFERENCE
                / (TIME_REFERENCE + task.getEstimatedMinutes());

        return deadlineScore * timeScore;
    }

    public Task spin(ArrayList<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalStateException("Add an assignment before spinning the wheel");
        }

        // Recalculate first in case an assignment was added, changed, or removed.
        recalculateWeights(tasks);

        // This gives me a random number between 0 and 1.
        // I keep adding each assignment's probability until the number fits
        // into one of those ranges. That's the assignment the wheel picks.
        double number = random.nextDouble();
        double currentChance = 0.0;

        for (Task task : tasks) {
            currentChance += task.getWeight();

            if (number < currentChance) {
                return task;
            }
        }

        // If it didn't land on an assignment, it landed on the break section.
        if (breakTask.getWeight() > 0.0) {
            return breakTask;
        }

        // If breaks are turned off, this also covers small rounding differences.
        return tasks.get(tasks.size() - 1);
    }

    public void setBreakProbability(double probability) {
        // The break chance can be changed later, but it needs to be valid.
        if (!Double.isFinite(probability) || probability < 0.0 || probability >= 1.0) {
            throw new IllegalArgumentException("Break chance must be between 0 and 1 (not including 1)");
        }

        breakChance = probability;
    }

    public double getBreakProbability() {
        return breakChance;
    }

    public Task getBreakTask() {
        return breakTask;
    }
}