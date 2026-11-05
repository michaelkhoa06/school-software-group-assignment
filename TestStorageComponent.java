import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
This is for testing the storage component. Do not include it in the final project!
*/

public class TestStorageComponent {
    public static void main(String[] args) {
        System.out.println("Start");

        DataSnapshot allFieldsGood = new DataSnapshot(
                new TaskData(
                        "Fix login page CSS overflow",
                        LocalDateTime.now().plusHours(2),
                        45,
                        TaskStatus.SELECTED
                ),
                List.of(
                        new TaskData(
                                "Refactor data storage module",
                                LocalDateTime.now().plusDays(1),
                                120,
                                TaskStatus.ACTIVE
                        ),
                        new TaskData(
                                "Write unit tests for JSON parser",
                                LocalDateTime.now().plusDays(3),
                                90,
                                TaskStatus.ACTIVE
                        ),
                        new TaskData(
                                "Update project README documentation",
                                LocalDateTime.now().plusDays(5),
                                60,
                                TaskStatus.ACTIVE
                        )
                ),
                Map.of(
                        new TaskData(
                                "Setup automated database backup script",
                                LocalDateTime.now().minusDays(1),
                                60,
                                TaskStatus.COMPLETED
                        ), 5,
                        new TaskData(
                                "Design user settings UI mockups",
                                LocalDateTime.now().minusDays(2),
                                150,
                                TaskStatus.COMPLETED
                        ), 5,
                        new TaskData(
                                "Configure GitHub Actions CI/CD pipeline",
                                LocalDateTime.now().minusDays(3),
                                180,
                                TaskStatus.FAILED
                        ), -10
                ),
                70,
                List.of(false, false, true)
        );

        DataSnapshot nullSelected = new DataSnapshot(
                null,
                List.of(
                        new TaskData(
                                "Update project README documentation",
                                LocalDateTime.now().plusDays(5),
                                60,
                                TaskStatus.ACTIVE
                        )
                ),
                Map.of(
                        new TaskData(
                                "Initial Maven project scaffolding",
                                LocalDateTime.now().minusDays(7),
                                120,
                                TaskStatus.FAILED
                        ), -5,
                        new TaskData(
                                "Install and verify local dependencies",
                                LocalDateTime.now().minusDays(8),
                                15,
                                TaskStatus.FAILED
                        ), -5
                ),
                342,
                List.of(true, true, false, false, true, false, false)
        );

        DataSnapshot allFieldsEmpty = new DataSnapshot(
                null,
                List.of(),
                Map.of(),
                Integer.MIN_VALUE,
                List.of()
        );

        DataSnapshot lotsOfData = new DataSnapshot(
                new TaskData(
                        "Finish documentation",
                        LocalDateTime.now().plusDays(2),
                        60,
                        TaskStatus.SELECTED
                ),
                List.of(
                        new TaskData(
                                "Fix login page CSS overflow",
                                LocalDateTime.now().plusHours(2),
                                45,
                                TaskStatus.ACTIVE
                        ),
                        new TaskData(
                                "Refactor data storage module",
                                LocalDateTime.now().plusDays(1),
                                120,
                                TaskStatus.ACTIVE
                        ),
                        new TaskData(
                                "Write unit tests for JSON parser",
                                LocalDateTime.now().plusDays(3),
                                90,
                                TaskStatus.ACTIVE
                        ),
                        new TaskData(
                                "Update project README documentation",
                                LocalDateTime.now().plusDays(5),
                                60,
                                TaskStatus.ACTIVE
                        ),
                        new TaskData(
                                "Review open pull requests",
                                LocalDateTime.now().plusHours(4),
                                30,
                                TaskStatus.ACTIVE
                        )
                ),
                Map.of(
                        new TaskData(
                                "Setup automated database backup script",
                                LocalDateTime.now().minusDays(1),
                                60,
                                TaskStatus.COMPLETED
                        ), 5,
                        new TaskData(
                                "Design user settings UI mockups",
                                LocalDateTime.now().minusDays(2),
                                150,
                                TaskStatus.COMPLETED
                        ), 5,
                        new TaskData(
                                "Configure GitHub Actions CI/CD pipeline",
                                LocalDateTime.now().minusDays(3),
                                180,
                                TaskStatus.COMPLETED
                        ), -10,
                        new TaskData(
                                "Initial Maven project scaffolding",
                                LocalDateTime.now().minusDays(7),
                                120,
                                TaskStatus.FAILED
                        ), 5,
                        new TaskData(
                                "Install and verify local dependencies",
                                LocalDateTime.now().minusDays(8),
                                15,
                                TaskStatus.COMPLETED
                        ), 5
                ),
                Integer.MAX_VALUE,
                List.of(true, false, true, true, false, true, false, false, true, false, true, false, true, true, false, false, true, false, true, true, false, false, true, false, true, false, true, true, false, true)
        );

        DataSnapshot duplicateTasks = new DataSnapshot(
                new TaskData(
                        "Refactor storage module",
                        LocalDateTime.now().plusHours(5),
                        120,
                        TaskStatus.ACTIVE
                ),
                List.of(
                        new TaskData(
                                "Refactor storage module",
                                LocalDateTime.now().plusHours(5),
                                120,
                                TaskStatus.ACTIVE
                        ),
                        new TaskData(
                                "Refactor storage module",
                                LocalDateTime.now().plusHours(5),
                                120,
                                TaskStatus.ACTIVE
                        )
                ),
                Map.of(
                        new TaskData(
                                "Refactor storage module",
                                LocalDateTime.now().plusHours(5),
                                120,
                                TaskStatus.COMPLETED
                        ), 5,
                        new TaskData(
                                "Refactor storage module",
                                LocalDateTime.now().plusHours(5),
                                120,
                                TaskStatus.COMPLETED
                        ), 5,
                        new TaskData(
                                "Refactor storage module",
                                LocalDateTime.now().plusHours(5),
                                120,
                                TaskStatus.FAILED
                        ), -5
                ),
                1111,
                List.of(true, true, true, true, true)
        );

        DataSnapshot malformedTasks = new DataSnapshot(
                new TaskData(
                        "",
                        LocalDateTime.MIN,
                        Integer.MIN_VALUE,
                        TaskStatus.FAILED
                ),
                List.of(
                        new TaskData(
                                "",
                                LocalDateTime.MIN,
                                -50,
                                TaskStatus.COMPLETED
                        )
                ),
                Map.of(
                        new TaskData(
                                "\\",
                                LocalDateTime.MAX,
                                Integer.MAX_VALUE,
                                TaskStatus.ACTIVE
                        ), Integer.MAX_VALUE,
                        new TaskData(
                                "\n",
                                LocalDateTime.now(),
                                Integer.MIN_VALUE,
                                TaskStatus.SELECTED
                        ), Integer.MIN_VALUE
                ),
                Integer.MIN_VALUE,
                List.of(false)
        );

        List<DataSnapshot> snapshots = new ArrayList<>();
        snapshots.add(allFieldsGood);
        snapshots.add(nullSelected);
        snapshots.add(allFieldsEmpty);
        snapshots.add(lotsOfData);
        snapshots.add(duplicateTasks);
        snapshots.add(malformedTasks);

        JsonStorage storage = new JsonStorage();

        int index = (int) (Math.random() * (snapshots.size() - 1));
        System.out.println("Testing DataSnapshot #" + index);

        System.out.println("---------- Writing to File ----------");
        storage.saveToFile(snapshots.get(index));

        System.out.println("\n\n");

        System.out.println("--------- Loading from File ---------");
        DataSnapshot snapshot = storage.load();
        System.out.println("\nSelected Task: " + snapshot.selectedTask());
        System.out.println("\nPoint Balance: " + snapshot.pointBalance());
        System.out.println("\nActive Tasks: ");
        for (TaskData t : snapshot.activeTasks()) {
            System.out.println(t);
        }
        System.out.println("\nTask History: ");
        for (TaskData t : snapshot.taskHistory().keySet()) {
            System.out.println(t + " -> PointsAwarded: " + snapshot.taskHistory().get(t));
        }
        System.out.println("\nPurchases: " + snapshot.purchases());
    }
}
