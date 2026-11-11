import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
-=[ About ]=-
Manages a .json file containing the user's active tasks, history, points, and purchases.
To avoid corruption, it writes to a temporary file before replacing the old data file.
On Windows, files are saved in /Appdata/Roaming/RoulettePlanner

-=[ Usage ]=-
Loading:
The manager component should call load() and store the values contained in the DataSnapshot.
JSONStorage is not responsible for providing access to values after loading.

Saving:
The manager is responsible for telling JSONStorage to write the saved data
by creating a DataSnapshot object and passing it through updateData().
Manually trigger a save with saveNow(). Only call this ONCE when closing the program!
*/

public class JsonStorage {
    private static final String STORAGE_NAME = "data.json";
    private static final String TEMP_NAME = STORAGE_NAME + ".temp";

    private final Path dataFolder;
    private final Path dataFile;
    private final Path tempFile;

    private final ScheduledExecutorService autoSave = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pendingSave;
    private static final int autoSaveDelaySec = 10;

    public JsonStorage() {
        dataFolder = OSUtil.getDataFolder();

        if (dataFolder != null) {
            dataFile = dataFolder.resolve(STORAGE_NAME).toFile().toPath();
            tempFile = dataFolder.resolve(TEMP_NAME).toFile().toPath();
        }
        else {
            dataFile = null;
            tempFile = null;
        }
    }


    // =================================================================
    // Public Methods
    // =================================================================

    // Read the JSON file and provide it as a DataSnapshot record
    public DataSnapshot load() {
        if (ensureFileExists(dataFile, "{}")) {
            if (dataFile != null) {
                try {
                    String json = Files.readString(dataFile, StandardCharsets.UTF_8);
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                    // Read selected task or null it
                    TaskData selectedTask = null;
                    if (root.has(Keys.SELECTED_TASK) && !root.get(Keys.SELECTED_TASK).isJsonNull()) {
                        selectedTask = parseTaskObject(root.getAsJsonObject(Keys.SELECTED_TASK));
                    }

                    // Read point balance
                    int pointBalance = root.has(Keys.POINT_BAL) ? root.get(Keys.POINT_BAL).getAsInt() : 0;

                    // Read active tasks and build a list
                    Map<TaskData> activeTasks = new HashMap<>();
                    if (root.has(Keys.ACTIVE_TASKS) && !root.get(Keys.ACTIVE_TASKS).isJsonNull()) {
                        for (JsonElement element : root.get(Keys.ACTIVE_TASKS).getAsJsonArray()) {
                            JsonObject obj = element.getAsJsonObject();
                            activeTasks.put(parseTaskObject(obj), 0.0);  // Fill with placeholder probability of 0.0
                        }
                    }

                    // Read task history and build a map
                    Map<TaskData, Integer> taskHistory = new HashMap<>();
                    if (root.has(Keys.TASK_HISTORY) && !root.get(Keys.TASK_HISTORY).isJsonNull()) {
                        for (JsonElement element : root.get(Keys.TASK_HISTORY).getAsJsonArray()) {
                            JsonObject obj = element.getAsJsonObject();
                            taskHistory.put(parseTaskObject(obj), obj.has(Keys.HIST_POINTS_AWARDED) ? obj.get(Keys.HIST_POINTS_AWARDED).getAsInt() : 0);
                        }
                    }

                    // Read purchases and build a list
                    List<Boolean> purchases = new ArrayList<>();
                    if (root.has(Keys.PURCHASES) && !root.get(Keys.PURCHASES).isJsonNull()) {
                        for (JsonElement element : root.get(Keys.PURCHASES).getAsJsonArray()) {
                            purchases.add(element.getAsBoolean());
                        }
                    }

                    System.out.println("Successfully loaded data from file!");
                    return new DataSnapshot(selectedTask, activeTasks, taskHistory, pointBalance, purchases);
                } catch (IOException e) {
                    System.out.println("Failed to read JSON file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // Reading failed
        return new DataSnapshot(null, new HashMap<>(), new HashMap<>(), 0, new ArrayList<>());
    }

    // Pass an updated snapshot of the program data. Changes save to file after 10 seconds of inactivity.
    public void updateData(DataSnapshot dataSnapshot) {
        cancelPendingSave();
        pendingSave = autoSave.schedule(() -> saveToFile(dataSnapshot), JsonStorage.autoSaveDelaySec, TimeUnit.SECONDS);
    }

    // Treat this as private! Only call it during program shutdown.
    public void saveNow(DataSnapshot dataSnapshot) {
        cancelPendingSave();
        autoSave.execute(() -> saveToFile(dataSnapshot));
    }


    // =================================================================
    // Private Methods (all sections below are also private)
    // =================================================================

    // Write a DataSnapshot record to the JSON file. The manager component should create this record
    private void saveToFile(DataSnapshot snapshot) {
        if (ensureFileExists(tempFile, "")) {
            try (Writer fileWriter = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8);
                 JsonWriter writer = new JsonWriter(fileWriter)) {
                writer.setIndent("  ");

                // Open root
                writer.beginObject();

                // Write selectedTask (or null)
                TaskData selectedTask = snapshot.selectedTask();
                writer.name(Keys.SELECTED_TASK);
                if (selectedTask != null) {
                    writer.beginObject();

                    writer.name(Keys.TASK_TITLE).value(selectedTask.getTitle());
                    writer.name(Keys.TASK_DEADLINE).value(selectedTask.getDeadline().toString());
                    writer.name(Keys.TASK_EST_TIME).value(selectedTask.getEstimatedTimeMin());
                    writer.name(Keys.TASK_STATUS).value(selectedTask.getStatus().toString());

                    writer.endObject();
                }
                else {
                    writer.nullValue();
                }

                // Write pointBalance
                writer.name(Keys.POINT_BAL).value(snapshot.pointBalance());

                // Write activeTasks array
                writer.name(Keys.ACTIVE_TASKS);
                writer.beginArray();

                for (TaskData activeTask : snapshot.activeTasks().keySet()) {
                    writer.beginObject();

                    writer.name(Keys.TASK_TITLE).value(activeTask.getTitle());
                    writer.name(Keys.TASK_DEADLINE).value(activeTask.getDeadline().toString());
                    writer.name(Keys.TASK_EST_TIME).value(activeTask.getEstimatedTimeMin());
                    writer.name(Keys.TASK_STATUS).value(activeTask.getStatus().toString());

                    writer.endObject();
                }

                writer.endArray();

                // Write taskHistory array
                writer.name(Keys.TASK_HISTORY);
                writer.beginArray();

                for (TaskData historyTask : snapshot.taskHistory().keySet()) {
                    writer.beginObject();

                    writer.name(Keys.TASK_TITLE).value(historyTask.getTitle());
                    writer.name(Keys.TASK_DEADLINE).value(historyTask.getDeadline().toString());
                    writer.name(Keys.TASK_EST_TIME).value(historyTask.getEstimatedTimeMin());
                    writer.name(Keys.TASK_STATUS).value(historyTask.getStatus().toString());
                    writer.name(Keys.HIST_POINTS_AWARDED).value(snapshot.taskHistory().get(historyTask));

                    writer.endObject();
                }

                writer.endArray();

                // Write purchases array
                writer.name(Keys.PURCHASES);
                writer.beginArray();

                for (Boolean purchase : snapshot.purchases()) {
                    writer.value(purchase);
                }

                writer.endArray();

                // Close root
                writer.endObject();

                // Replace the old data file with the temp (avoids corruption)
                Files.move(tempFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Data successfully saved!");
            } catch (Exception e) {
                System.err.println("Failed to write JSON file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean ensureFileExists(Path file, String contents) {
        try {
            if (dataFolder != null && !Files.exists(dataFolder)) {
                Files.createDirectories(dataFolder);
            }

            if (!Files.exists(file)) {
                Files.createFile(file);
                if (!contents.isEmpty()) {
                    Files.writeString(file, contents);
                }
                System.out.println("Successfully created blank file at " + file);
            }
            return true;
        } catch (IOException e) {
            System.out.println("Failed to initialize storage file '" + file.getFileName() + "': " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void cancelPendingSave() {
        if (pendingSave != null) {
            pendingSave.cancel(false);
            pendingSave = null;
        }
    }


    // =================================================================
    // Parsing Helpers (private)
    // =================================================================

    private TaskData parseTaskObject(JsonObject obj) {
        String title = obj.has(Keys.TASK_TITLE) ? obj.get(Keys.TASK_TITLE).getAsString() : "";

        LocalDateTime deadline = LocalDateTime.now();
        if (obj.has(Keys.TASK_DEADLINE) && !obj.get(Keys.TASK_DEADLINE).isJsonNull()) {
            deadline = LocalDateTime.parse(obj.get(Keys.TASK_DEADLINE).getAsString());
        }

        int estimatedTimeMin = obj.has(Keys.TASK_EST_TIME) ? obj.get(Keys.TASK_EST_TIME).getAsInt() : 0;

        TaskStatus status = obj.has(Keys.TASK_STATUS) ? TaskStatus.valueOf(obj.get(Keys.TASK_STATUS).getAsString()) : TaskStatus.ACTIVE;

        return new TaskData(title, deadline, estimatedTimeMin, status);
    }


    // =================================================================
    // JSON String Keys
    // =================================================================

    private static class Keys {
        private static final String SELECTED_TASK = "selectedTask";
        private static final String ACTIVE_TASKS = "activeTasks";
        private static final String TASK_HISTORY = "taskHistory";
        private static final String POINT_BAL = "pointBalance";
        private static final String PURCHASES = "purchases";

        private static final String TASK_TITLE = "title";
        private static final String TASK_DEADLINE = "deadline";
        private static final String TASK_EST_TIME = "estimatedTimeMin";
        private static final String TASK_STATUS = "status";

        private static final String HIST_POINTS_AWARDED = "pointsAwarded";
    }
}

// =================================================================
// Sample JSON Structure
// =================================================================
/*


{
  "selectedTask": {
    "title": "Fix login page CSS overflow",
    "deadline": "2026-11-06T11:43",
    "estimatedTimeMin": 45,
    "status": "SELECTED"
  },
  "pointBalance": 70,
  "activeTasks": [
    {
      "title": "Refactor data storage module",
      "deadline": "2026-11-07T09:43",
      "estimatedTimeMin": 120,
      "status": "ACTIVE"
    },
    {
      "title": "Write unit tests for JSON parser",
      "deadline": "2026-11-09T09:43",
      "estimatedTimeMin": 90,
      "status": "ACTIVE"
    },
    {
      "title": "Update project README documentation",
      "deadline": "2026-11-11T09:43",
      "estimatedTimeMin": 60,
      "status": "ACTIVE"
    }
  ],
  "taskHistory": [
    {
      "title": "Setup automated database backup script",
      "deadline": "2026-11-05T09:43",
      "estimatedTimeMin": 60,
      "status": "COMPLETED",
      "pointsAwarded": 5
    },
    {
      "title": "Design user settings UI mockups",
      "deadline": "2026-11-04T09:43",
      "estimatedTimeMin": 150,
      "status": "COMPLETED",
      "pointsAwarded": 5
    },
    {
      "title": "Configure GitHub Actions CI/CD pipeline",
      "deadline": "2026-11-03T09:43",
      "estimatedTimeMin": 180,
      "status": "FAILED",
      "pointsAwarded": -10
    }
  ],
  "purchases": [
    false,
    false,
    true
  ]
}


*/
