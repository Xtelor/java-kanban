package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final LinkedList<Task> taskHistory;

    public InMemoryHistoryManager() {
        this.taskHistory = new LinkedList<>();
    }

    // Возвращает историю
    public List<Task> getHistory() {
        return new ArrayList<>(taskHistory);
    }

    // Добавление задачи в историю
    public <T extends Task> void addInHistory(T task) {
        if (task == null) return;

        if (taskHistory.size() >= 10) {
            taskHistory.removeFirst();
        }

        Task taskCopy;
        if (task instanceof Subtask subtask) {
            taskCopy = new Subtask(subtask.getTaskId(), subtask.getTaskName(),
                                   subtask.getTaskDescription(), subtask.getTaskStatus());
            ((Subtask) taskCopy).setEpicIdentifier(subtask.getEpicIdentifier());
        } else if (task instanceof Epic epic) {
            taskCopy = new Epic(epic.getTaskId(), epic.getTaskName(), epic.getTaskDescription());
        } else {
            taskCopy = new Task(task.getTaskId(), task.getTaskName(),
                                task.getTaskDescription(), task.getTaskStatus());
        }

        taskHistory.add(taskCopy);
    }
}
