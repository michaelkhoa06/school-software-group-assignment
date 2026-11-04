public class Task {

    private String name;
    private String deadline;
    private int estimatedTime;

    public Task(String name, String deadline, int estimatedTime) {
        this.name = name;
        this.deadline = deadline;
        this.estimatedTime = estimatedTime;
    }

    public String getName() {
        return name;
    }

    public String getDeadline() {
        return deadline;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }
}