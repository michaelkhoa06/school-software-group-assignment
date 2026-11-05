/*
-=[ About ]=-
Statuses for tasks. Each TaskData will have 1 status.

-=[ Usage ]=-
ACTIVE: Tasks that can be selected by the roulette.
SELECTED: The *one* task that is currently selected (rolled).
COMPLETED: Task that is in the history. The user rolled and completed this task.
FAILED: Task that is in the history. The user rolled and failed to complete this task.
*/

public enum TaskStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    SKIPPED
}
