public class UIController {

    private TaskPanel taskPanel;
    private RouletteWheel rouletteWheel;
    private HistoryPanel historyPanel;

    public UIController(
            TaskPanel taskPanel,
            RouletteWheel rouletteWheel,
            HistoryPanel historyPanel
    ) {

        this.taskPanel = taskPanel;
        this.rouletteWheel = rouletteWheel;
        this.historyPanel = historyPanel;
    }

    public void updateData(DataSnapshot snapshot) {

        taskPanel.refreshTasks();

        rouletteWheel.refreshWheel();

        if (snapshot.selectedTask() == null) {
            rouletteWheel.clearSelection();
        }

        historyPanel.updateHistory(
                snapshot.taskHistory()
        );
    }
}