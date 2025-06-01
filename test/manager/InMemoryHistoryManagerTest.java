package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    public void beforeEach(){
        task = new Task("Магазин", "Купить продукты", TaskStatus.NEW);
        epic = new Epic("Переезд", "Подготовиться к переезду");
        subtask = new Subtask("Вещи", "Упаковать вещи", TaskStatus.NEW);
        historyManager = new InMemoryHistoryManager();
    }

    @Test // Проверка добавления задач в историю
    public void testAddInHistory() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        List<Task> history = historyManager.getHistory();

        assertEquals(3,history.size(),"В истории должно быть 3 задания");

        assertTrue(isContainsTask(history, task), "История должна содержать задачу");
        assertTrue(isContainsTask(history, epic), "История должна содержать эпик");
        assertTrue(isContainsTask(history, subtask), "История должна содержать подзадачу");

        assertEquals(task ,history.getFirst(), "Первой в истории должна быть задача");
        assertEquals(subtask, history.getLast(), "Последней в истории должна быть подзадача");
    }

    @Test // Проверка работы очереди в менеджере истории
    public void testHistoryQueue() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);

        assertEquals(2, historyManager.getHistory().size(), "В истории должно быть 2 задания");
        assertEquals(task ,historyManager.getHistory().getFirst(), "Первой в истории должна быть задача");
        assertEquals(epic, historyManager.getHistory().getLast(), "Последней в истории должен быть эпик");

        historyManager.addInHistory(subtask);
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);
        historyManager.addInHistory(task);

        assertEquals(10, historyManager.getHistory().size(), "В истории должно быть 10 заданий");
        assertEquals(task ,historyManager.getHistory().getFirst(), "Первой в истории должна быть задача");
        assertEquals(task, historyManager.getHistory().getLast(), "Последней в истории должна быть задача");

        historyManager.addInHistory(epic);

        assertEquals(10, historyManager.getHistory().size(), "В истории должно быть 10 заданий");
        assertEquals(epic ,historyManager.getHistory().getFirst(), "Первым в истории должен быть эпик");
        assertEquals(epic, historyManager.getHistory().getLast(), "Последним в истории должен быть эпик");

    }

    @Test // Проверка пустоты списка до просмотра задач и заполнения
    public void testHistoryList() {
        assertTrue(historyManager.getHistory().isEmpty(), "Список истории должен быть пуст");

        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        List<Task> history = historyManager.getHistory();
        assertFalse(history.isEmpty(), "Список истории не должен быть пуст");
        assertEquals(3, history.size(), "В списке истории должно быть 3 записи");
    }

    // Проверка наличия задачи/эпика/подзадачи в истории
    private boolean isContainsTask(List<Task> history, Task newTask) {
        for (Task task : history) {
            if (task.getTaskId() == newTask.getTaskId() &&
                    task.getTaskName().equals(newTask.getTaskName()) &&
                    task.getTaskDescription().equals(newTask.getTaskDescription()) &&
                    task.getTaskStatus() == newTask.getTaskStatus()) {
                return true;
            }
        }
        return false;
    }

}
