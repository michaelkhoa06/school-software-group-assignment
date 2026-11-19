public class UIController {

    private final TaskPanel taskPanel;
    private final RouletteWheel rouletteWheel;
    private final HistoryPanel historyPanel;
    private final PointsPanel pointsPanel;

    public UIController(
            TaskPanel taskPanel,
            RouletteWheel rouletteWheel,
            HistoryPanel historyPanel,
            PointsPanel pointsPanel
    ) {

        this.taskPanel = taskPanel;
        this.rouletteWheel = rouletteWheel;
        this.historyPanel = historyPanel;
        this.pointsPanel = pointsPanel;
    }

    public void updateData(DataSnapshot snapshot) {

        taskPanel.refreshTaskList(snapshot.activeTasks());

        rouletteWheel.refreshWheel(snapshot.selectedTask(), snapshot.activeTasks());

        historyPanel.updateHistory(
                snapshot.taskHistory()
        );

        pointsPanel.updatePoints(snapshot.pointBalance());
        // Shop *should* be updated here
    }
}