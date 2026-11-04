import java.util.*;

/*
-=[ About ]=-
Manages a .json file containing the user's active tasks, history, points, and purchases.

-=[ Usage ]=-
The manager component should call all load methods and store/manipulate the values.
JSONStorage is not responsible for providing access to values after loading.
*/

// TODO: Determine how purchases are stored in memory and saved/loaded

public class JsonStorage {
    // Read the JSON file and provide it as an in-memory List<TaskData>
    public List<TaskData> loadActiveTasks() {
        List<TaskData> out = new ArrayList<>();



        return out;
    }

    // Read the JSON file and provide the task history as a HashMap with
    // keys of completed/failed/skipped tasks and values of awarded/deducted points.
    public Map<TaskData, Integer> loadTaskHistory() {
        Map<TaskData, Integer> out = new HashMap<>();



        return out;
    }

    // Read the JSON file and provide the user's point balance
    public int loadPointBalance() {
        return -1;
    }

    // Read the JSON file and provide the user's purchases as a ???
    public List<?> loadPurchases() {
        List<?> out = new ArrayList<>();



        return out;
    }


    public void saveToFile(
            List<TaskData> activeTasks, Map<TaskData, Integer> taskHistory,
            int pointBalance, List<?> purchases
    ) {
        
    }
}

// Sample JSON format
/*


{
  "selectedTask": {
    "title": "Review JSON Structure",
    "deadline": "2026-06-07T16:00:00",
    "estimatedTimeMin": 30,
    "status": "SELECTED"
  },
  "activeTasks": [
    {
      "title": "Stat Homework",
      "deadline": "2026-06-15T17:00:00",
      "estimatedTimeMin": 120,
      "status": "ACTIVE"
    },
    {
      "title": "Group Project",
      "deadline": "2026-06-10T10:30:00",
      "estimatedTimeMin": 45,
      "status": "ACTIVE"
    }
  ],
  "taskHistory": [
    {
      "task": {
        "title": "Write Essay",
        "deadline": "2026-06-15T17:00:00",
        "estimatedTimeMin": 2880,
        "status": "COMPLETED"
      },
      "pointsAwarded": 5
    },
    {
      "task": {
        "title": "Study for Calculus III",
        "deadline": "2026-06-16T17:00:00",
        "estimatedTimeMin": 180,
        "status": "SKIPPED"
      },
      "pointsAwarded": -10
    }
  ],
  "pointBalance": 42,
  "purchases": []
}


*/