package manager;

import tasks.Task;
import java.util.List;

public interface HistoryManager {
    // Возвращает историю
    List<Task> getHistory();

    // Добавление задачи в историю
    <T extends Task> void addInHistory(T task);
}
