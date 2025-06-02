package manager;


import tasks.Task;
import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> taskHistory;

    public InMemoryHistoryManager() {
        this.taskHistory = new ArrayList<>();
    }

    // Возвращает историю
    public List<Task> getHistory() {
        return new ArrayList<>(taskHistory);
    }

    // Добавление задачи в историю
    public void addInHistory(Task task) {
        if (task == null) return;

        if (taskHistory.size() >= 10) {
            taskHistory.removeFirst();
        }

        Task taskCopy = task.cloneTask();
        taskHistory.add(taskCopy);
    }
}
