package manager;

import tasks.Task;
import java.util.List;

public interface HistoryManager {
    // Возвращает историю
    List<Task> getHistory();

    // Добавление задачи в историю
    void addInHistory(Task task);
}
