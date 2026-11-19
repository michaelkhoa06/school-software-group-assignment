import java.util.List;
import java.util.Map;
import java.util.Set;

/*
-=[ About ]=-
Contains a snapshot of the program's data.
Used in loading from and saving to a file.

-=[ Usage ]=-
Loading:
The manager component calls the JSONStorage load() and receives a DataSnapshot that it can read.
[!] Note that selectedTask can be null!

Saving:
The manager component creates a DataSnapshot with copies of its list data (do not pass directly),
then passes it to the JSONStorage via saveToFile().
*/

public record DataSnapshot(
        TaskData selectedTask,
        Map<TaskData, Double> activeTasks,
        Map<TaskData, Integer> taskHistory,
        int pointBalance,
        List<Boolean> purchases
) {}
