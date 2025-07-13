package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    private TaskGenerator taskGenerator;

    @BeforeEach
    void beforeEach(){
        taskGenerator = new TaskGenerator();
        task = taskGenerator.createTask();
        epic = taskGenerator.createEpic();
        subtask = taskGenerator.createSubtask();
        historyManager = new InMemoryHistoryManager();
    }

    @Test // Проверка пустоты списка до просмотра задач и заполнения
    void testHistoryList() {
        assertTrue(historyManager.getHistory().isEmpty(), "Изначально список истории должен быть пуст");
    }

    @Test // Проверка добавления задач в историю
    void testAddInHistory() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        List<Task> history = historyManager.getHistory();

        assertFalse(history.isEmpty(), "Список истории не должен быть пуст");
        assertEquals(3,history.size(), "В истории должно быть 3 задания");

        assertTrue(isContainsTask(history, task), "История должна содержать задачу");
        assertTrue(isContainsTask(history, epic), "История должна содержать эпик");
        assertTrue(isContainsTask(history, subtask), "История должна содержать подзадачу");
    }

    @Test // Проверка добавления задач в историю в правильном порядке
    void shouldReturnTasksInValidOrder() {
        historyManager.addInHistory(subtask);
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);

        List<Task> history = historyManager.getHistory();
        assertEquals(subtask, history.getFirst(), "Первой должна быть подзадача");
        assertEquals(task, history.get(1), "Второй должна быть задача");
        assertEquals(epic, history.getLast(), "Последним должен быть эпик");
    }

    @Test // Не должен добавлять null
    void shouldNotAddNullInHistory() {
        historyManager.addInHistory(null);
        assertTrue(historyManager.getHistory().isEmpty(), "Список должен быть пуст");
    }

    @Test // Задачи с отрицательными ID не должны добавляться
    void shouldHandleNegativeIds() {
        Task negativeTask = taskGenerator.createTask(-1);
        Task negativeEpic = taskGenerator.createTask(-2);
        Task negativeSubtask = taskGenerator.createTask(-3);

        historyManager.addInHistory(negativeTask);
        historyManager.addInHistory(negativeEpic);
        historyManager.addInHistory(negativeSubtask);

        assertTrue(historyManager.getHistory().isEmpty(), "Задачи с отрицательным ID добавились");
    }

    @Test // Задачи с ID = 0 не должны добавляться
    void shouldHandleZeroIds() {
        Task zeroTask = taskGenerator.createTask(0);
        Task zeroEpic = taskGenerator.createTask(0);
        Task zeroSubtask = taskGenerator.createTask(0);

        historyManager.addInHistory(zeroTask);
        historyManager.addInHistory(zeroEpic);
        historyManager.addInHistory(zeroSubtask);

        assertTrue(historyManager.getHistory().isEmpty(), "Задачи с отрицательным ID добавились");
    }

    @Test // Проверка на множественное добавление одной и той же задачи без изменений
    void shouldHandleRepeatedTasks() {
        for (int i = 0; i < 10; i++) {
            historyManager.addInHistory(task);
        }

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test // Проверка автоматического удаления дубликатов из истории
    void testHistoryQueue() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.addInHistory(task);

        assertEquals(3, historyManager.getHistory().size(), "В истории должно быть 3 задания");
        assertEquals(epic ,historyManager.getHistory().getFirst(), "Первым в истории должен быть эпик");
        assertEquals(task, historyManager.getHistory().getLast(), "Последней в истории должна быть задача");
        assertTrue(isContainsTask(historyManager.getHistory(), subtask), "В списке нет подзадачи");
    }

    @Test // Проверка перемещения повторно просмотренной задачи в конец списка
    void shouldMoveTaskToEndIfAlreadyExists() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(subtask);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(subtask, history.getFirst(), "Первой должна быть подзадача");
        assertEquals(task, history.getLast(), "Последней должна быть задача");
        assertTrue(isContainsTask(historyManager.getHistory(), epic), "В списке нет эпика");
    }

    @Test // Проверка корректности заполнения при добавлении 1 задачи
    void shouldContainOneTask() {
        historyManager.addInHistory(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size(), "В истории должна быть одна задача");
        assertTrue(isContainsTask(history, task), "Задача не добавлена");
    }

    @Test // Проверка корректности заполнения истории при добавлении большого количества заданий
    void shouldHandleLargeTasksAmount() {
        int size = 0;

        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        for (int i = 0; i < 2000; i++) {
            historyManager.addInHistory(taskGenerator.createTask());
            historyManager.addInHistory(taskGenerator.createEpic());
            historyManager.addInHistory(taskGenerator.createSubtask());
            size += 3;
        }

        historyManager.addInHistory(task);

        assertEquals(size + 3, historyManager.getHistory().size(), "Не все задачи добавлены");
        assertEquals(epic,historyManager.getHistory().getFirst(), "Первым в истории должен быть эпик");
        assertEquals(task, historyManager.getHistory().getLast(), "Последней в истории должна быть задача");
    }

    @Test // Проверка перезаписи задач с одинаковым ID (новых версий уже просмотренных задач)
    void shouldOverrideTasksWithSameIds() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        Task task2 = taskGenerator.createTask(1);
        Epic epic2 = taskGenerator.createEpic(2);
        Subtask subtask2 = taskGenerator.createSubtask(3);

        historyManager.addInHistory(task2);
        historyManager.addInHistory(epic2);
        historyManager.addInHistory(subtask2);

        assertEquals(3, historyManager.getHistory().size(), "В истории должно быть 3 задания");
        assertEquals(task2 ,historyManager.getHistory().getFirst(), "Первой в истории должна быть задача №2");
        assertEquals(subtask2, historyManager.getHistory().getLast(),
                "Последней в истории должна быть подзадача №2");
    }

    @Test // Проверка всех полей задачи при её замене новой версией задачи
    void shouldFullReplaceTaskWithSameId() {
        historyManager.addInHistory(task);
        Task newTask = taskGenerator.createTask(task.getTaskId());

        historyManager.addInHistory(newTask);
        Task currentTask = historyManager.getHistory().getFirst();

        assertEquals("Магазин2", currentTask.getTaskName(), "Имя не изменилось");
        assertEquals("Купить продукты2", currentTask.getTaskDescription(),"Описание не изменилось");
        assertEquals(TaskStatus.DONE, currentTask.getTaskStatus(), "Статус не изменился");
    }

    @Test // Проверка всех полей подзадачи при её замене новой версией подзадачи
    void shouldFullReplaceSubtaskWithSameId() {
        historyManager.addInHistory(subtask);

        Subtask newSubtask = taskGenerator.createSubtask(subtask.getTaskId());
        historyManager.addInHistory(newSubtask);

        Task currentSubtask = historyManager.getHistory().getFirst();

        assertEquals("Вещи2", currentSubtask.getTaskName(), "Имя не изменилось");
        assertEquals("Упаковать вещи2", currentSubtask.getTaskDescription(), "Описание не изменилось");
        assertEquals(TaskStatus.DONE, currentSubtask.getTaskStatus(), "Статус не изменился");
    }

    @Test // Проверка всех полей эпика (без подзадач) при его замене новой версией эпика (без подзадач)
    void shouldReplaceEpicWithSameIdWithoutSubtasks() {
        historyManager.addInHistory(epic);

        Epic newEpic = taskGenerator.createEpic(epic.getTaskId());
        historyManager.addInHistory(newEpic);

        Task currentEpic = historyManager.getHistory().getFirst();

        assertEquals("Переезд2", currentEpic.getTaskName(), "Имя не изменилось");
        assertEquals("Подготовиться к переезду2", currentEpic.getTaskDescription(),
                "Описание не изменилось");
        assertEquals(TaskStatus.NEW, currentEpic.getTaskStatus(), "Статус изменился");
    }

    @Test // Проверка удаления всех задач из истории
    void shouldRemoveTasksFromHistory() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.removeFromHistory(task.getTaskId());
        historyManager.removeFromHistory(epic.getTaskId());
        historyManager.removeFromHistory(subtask.getTaskId());

        assertTrue(historyManager.getHistory().isEmpty(), "Список истории должен быть пуст");
    }

    @Test // Проверка удаления задачи из начала списка
    void shouldRemoveTaskFromBeginning () {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.removeFromHistory(task.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 элемента");
        assertEquals(epic, history.getFirst(), "Первым должен быть эпик");
        assertEquals(subtask, history.getLast(), "Последней должна быть подзадача");
    }

    @Test // Проверка удаления задачи из середины списка
    void shouldRemoveTaskFromMiddle () {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.removeFromHistory(epic.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 элемента");
        assertEquals(task, history.getFirst(), "Первой должна быть задача");
        assertEquals(subtask, history.getLast(), "Последней должна быть подзадача");
    }

    @Test // Проверка удаления из пустого списка
    void shouldRemoveFromEmptyHistory() {
        historyManager.removeFromHistory(1);
        assertTrue(historyManager.getHistory().isEmpty(), "Список должен быть пустым");
    }

    @Test // Проверка на добавление задачи после её удаления
    void shouldAddSameTaskAfterRemoval() {
        historyManager.addInHistory(task);
        historyManager.removeFromHistory(task.getTaskId());
        historyManager.addInHistory(task);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.getFirst());
    }

    @Test // Проверка удаления задачи с конца списка
    void shouldRemoveTaskFromEnd () {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.removeFromHistory(subtask.getTaskId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size(), "Должно остаться 2 элемента");
        assertEquals(task, history.getFirst(), "Первой должна быть задача");
        assertEquals(epic, history.getLast(), "Последним должен быть эпик");
    }

    @Test // Проверка игнорирования запроса на удаление по несуществующему ID
    void shouldNotRemoveNonExistentTasks() {
        historyManager.addInHistory(task);
        historyManager.removeFromHistory(404);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "В истории должна быть 1 задача");
        assertTrue(isContainsTask(history, task), "Задача не сохранилась в истории");
    }

    @Test // Проверка работоспособности при операциях удаления и добавления
    void shouldStayCorrectAfterChanges() {
        historyManager.addInHistory(task);
        historyManager.addInHistory(epic);
        historyManager.addInHistory(subtask);

        historyManager.removeFromHistory(epic.getTaskId());
        historyManager.addInHistory(task);

        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size(), "Должно остаться 2 задачи");
        assertEquals(subtask, history.getFirst(), "Первой должна быть подзадача");
        assertEquals(task, history.getLast(), "Последней должна быть задача");
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

    // Вспомогательный класс для генерации задач
    private static final class TaskGenerator {
        private int idCounter = 1;

        Task createTask() {
            Task task = new Task("Магазин", "Купить продукты", TaskStatus.NEW);
            task.setTaskId(idCounter++);
            return task;
        }

        Task createTask(int identifier) {
            return new Task(identifier,"Магазин2", "Купить продукты2", TaskStatus.DONE);
        }

        Epic createEpic() {
            Epic epic = new Epic("Переезд", "Подготовиться к переезду");
            epic.setTaskId(idCounter++);
            return epic;
        }

        Epic createEpic(int identifier) {
            return new Epic(identifier,"Переезд2", "Подготовиться к переезду2");
        }

        Subtask createSubtask() {
            Subtask subtask = new Subtask("Вещи", "Упаковать вещи", TaskStatus.NEW);
            subtask.setTaskId(idCounter++);
            return subtask;
        }

        Subtask createSubtask(int identifier) {
            return new Subtask(identifier,"Вещи2", "Упаковать вещи2", TaskStatus.DONE);
        }
    }
}
