import com.google.gson.*;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

/*
-=[ About ]=-
Manages a .json file containing the user's active tasks, history, points, and purchases.
To avoid corruption, it writes to a temporary file before replacing the old data file.
On Windows, files are saved in /Appdata/Roaming/RoulettePlanner

-=[ Usage ]=-
The manager component should call load() and store the values contained in the DataSnapshot.
JSONStorage is not responsible for providing access to values after loading.
The manager is responsible for telling JSONStorage to write the saved data
by creating a DataSnapshot object and passing it through saveToFile().
*/

public class JsonStorage {
    private static final String STORAGE_NAME = "data.json";
    private static final String TEMP_NAME = STORAGE_NAME + ".temp";
    private final Path dataFolder;
    private final Path dataFile;
    private final Path tempFile;

    // Constructor
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

    // Read the JSON file and provide it as a DataSnapshot record
    public DataSnapshot load() {
        if (ensureFileExists(dataFile, "{}")) {
            if (dataFile != null) {
                try {
                    String json = Files.readString(dataFile);
                    JsonObject root = JsonParser.parseString(json).getAsJsonObject();

                    // Read selected task or null it
                    TaskData selectedTask = null;
                    if (root.has(Keys.SELECTED_TASK) && !root.get(Keys.SELECTED_TASK).isJsonNull()) {
                        selectedTask = parseTaskObject(root.getAsJsonObject(Keys.SELECTED_TASK));
                    }

                    // Read point balance
                    int pointBalance = root.has(Keys.POINT_BAL) ? root.get(Keys.POINT_BAL).getAsInt() : 0;

                    // Read active tasks and build a list
                    List<TaskData> activeTasks = new ArrayList<>();
                    if (root.has(Keys.ACTIVE_TASKS) && !root.get(Keys.ACTIVE_TASKS).isJsonNull()) {
                        for (JsonElement element : root.get(Keys.ACTIVE_TASKS).getAsJsonArray()) {
                            JsonObject obj = element.getAsJsonObject();
                            activeTasks.add(parseTaskObject(obj));
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
        return new DataSnapshot(null, new ArrayList<>(), new HashMap<>(), 0, new ArrayList<>());
    }

    // Write a DataSnapshot record to the JSON file. The manager component should create this record
    public void saveToFile(DataSnapshot snapshot) {
        if (ensureFileExists(tempFile, "")) {
            try (Writer fileWriter = Files.newBufferedWriter(tempFile);
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

                for (TaskData activeTask : snapshot.activeTasks()) {
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
            System.out.println("Failed to initialize storage file: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

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

// Sample JSON format
/*


{
  "selectedTask": null,
  "pointBalance": 250,
  "activeTasks": [
    {
      "title": "Finish documentation",
      "deadline": "2026-11-07T14:01",
      "estimatedTimeMin": 60,
      "status": "ACTIVE"
    },
    {
      "title": "Refactor storage module",
      "deadline": "2026-11-05T19:01",
      "estimatedTimeMin": 120,
      "status": "ACTIVE"
    }
  ],
  "taskHistory": [
    {
      "title": "Design database schema",
      "deadline": "2026-11-02T14:01",
      "estimatedTimeMin": 90,
      "status": "COMPLETED",
      "pointsAwarded": 100
    },
    {
      "title": "Setup project repository",
      "deadline": "2026-10-31T14:01",
      "estimatedTimeMin": 30,
      "status": "COMPLETED",
      "pointsAwarded": 50
    }
  ],
  "purchases": [
    true,
    false,
    true
  ]
}


*/