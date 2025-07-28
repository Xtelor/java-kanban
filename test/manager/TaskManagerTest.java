package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    protected T taskManager;
    protected Task task;
    protected Epic epic;
    protected Subtask subtask;

    protected final long duration = 99;
    protected final LocalDateTime date = LocalDateTime.of(2025, 5, 12, 14, 54);

    @BeforeEach
    void beforeEach() {
        taskManager = createManager();

        task = new Task("Задача #1", "Проверка", TaskStatus.NEW,
                duration, date);
        epic = new Epic("Эпик #1", "Проверка");
        subtask = new Subtask("Подзадача #1", "Проверка", TaskStatus.IN_PROGRESS,
                duration, date.plusHours(4));
    }

    abstract T createManager();

    @Test
        // Проверка создания задачи
    void shouldCreateTask() {
        taskManager.createNewTask(task);
        assertTrue(taskManager.getTasks().contains(task), "Задача не добавлена");
    }

    @Test
        // Проверка создания эпика
    void shouldCreateEpic() {
        taskManager.createNewEpic(epic);
        assertTrue(taskManager.getEpics().contains(epic), "Эпик не добавлен");
    }

    @Test
        // Проверка создания подзадачи
    void shouldCreateSubtask() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        assertTrue(taskManager.getSubtasks().contains(subtask), "Подзадача не добавлена");
    }

    @Test
        // Проверка поиска задачи по ID
    void shouldFindTaskById() {
        assertNull(taskManager.getTaskById(task.getTaskId()), "Изначально не должно быть задач");

        taskManager.createNewTask(task);
        Task foundTask = taskManager.getTaskById(task.getTaskId());

        assertNotNull(foundTask, "Задача не найдена");
        assertEquals(task, foundTask, "Найденная задача не совпадает с действительной");
    }

    @Test
        // Проверка нахождения задачи в истории
    void shouldFindTaskInHistory() {
        taskManager.createNewTask(task);
        taskManager.getTaskById(task.getTaskId());

        List<Task> history = taskManager.getHistory();
        assertTrue(history.contains(task), "Задачи нет в истории просмотров");
        assertEquals(1, history.size(), "В истории должна быть одна задача");
    }

    @Test
        // Проверка поиска эпика по ID
    void shouldFindEpicById() {
        assertNull(taskManager.getEpicById(epic.getTaskId()), "Изначально не должно быть эпиков");

        taskManager.createNewEpic(epic);
        Epic foundEpic = taskManager.getEpicById(epic.getTaskId());

        assertNotNull(foundEpic, "Эпик не найден");
        assertEquals(epic, foundEpic, "Найденный эпик не совпадает с действительным");
    }

    @Test
        // Проверка нахождения эпика в истории
    void shouldFindEpicInHistory() {
        taskManager.createNewEpic(epic);
        taskManager.getEpicById(epic.getTaskId());

        List<Task> history = taskManager.getHistory();

        boolean isEpicFound = history.stream()
                .map(Task::getTaskId)
                .anyMatch(id -> id.equals(epic.getTaskId()));

        assertTrue(isEpicFound, "Эпика нет в истории просмотров");
        assertEquals(1, history.size(), "В истории должен быть один эпик");
    }

    @Test
        // Проверка поиска подзадачи по ID
    void shouldFindSubtaskById() {
        assertNull(taskManager.getSubtaskById(subtask.getTaskId()), "Изначально не должно быть подзадач");

        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        Subtask foundSubtask = taskManager.getSubtaskById(subtask.getTaskId());

        assertNotNull(foundSubtask, "Подзадача не найдена");
        assertEquals(subtask, foundSubtask, "Найденная подзадача не совпадает с действительной");
    }

    @Test
        // Проверка нахождения подзадачи в истории
    void shouldFindSubtaskInHistory() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.getSubtaskById(subtask.getTaskId());

        List<Task> history = taskManager.getHistory();
        boolean isSubtaskFound = history.stream()
                .map(Task::getTaskId)
                .anyMatch(id -> id.equals(subtask.getTaskId()));

        assertTrue(isSubtaskFound, "Подзадачи нет в истории просмотров");
        assertEquals(1, history.size(), "В истории должна быть одна подзадача");
    }

    @Test
        // Проверка списка задач
    void shouldGetTasksList() {
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пуст");

        taskManager.createNewTask(task);
        List<Task> tasks = taskManager.getTasks();

        assertFalse(tasks.isEmpty(), "Список задач не должен быть пуст");
        assertEquals(1, tasks.size(), "В списке должна быть одна задача");
        assertTrue(tasks.contains(task), "Добавленной задачи нет в списке");
    }

    @Test
        // Проверка списка эпиков
    void shouldGetEpicsList() {
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пуст");

        taskManager.createNewEpic(epic);
        List<Epic> epics = taskManager.getEpics();

        assertFalse(epics.isEmpty(), "Список эпиков не должен быть пуст");
        assertEquals(1, epics.size(), "В списке должен быть один эпик");
        assertTrue(epics.contains(epic), "Добавленного эпика нет в списке");
    }

    @Test
        // Проверка списка подзадач
    void shouldGetSubtasksList() {
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пуст");

        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertFalse(subtasks.isEmpty(), "Список подзадач не должен быть пуст");
        assertEquals(1, subtasks.size(), "В списке должна быть одна подзадача");
        assertTrue(subtasks.contains(subtask), "Добавленной подзадачи нет в списке");
    }

    @Test
        // Проверка удаления задачи по ID
    void shouldDeleteTaskById() {
        taskManager.createNewTask(task);

        int taskId = task.getTaskId();
        taskManager.deleteTaskById(taskId);

        assertNull(taskManager.getTaskById(taskId), "Задача должна быть удалена");
        assertFalse(taskManager.getTasks().contains(task), "Задача должна быть удалена из списка");
    }

    @Test
        // Проверка удаления эпика по ID
    void shouldDeleteEpicById() {
        taskManager.createNewEpic(epic);

        int epicId = epic.getTaskId();
        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getEpicById(epicId), "Эпик должен быть удален");
        assertFalse(taskManager.getEpics().contains(epic), "Эпик должен быть удален из списка");
    }

    @Test
        // Проверка удаления подзадачи по ID
    void shouldDeleteSubtaskById() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        int subtaskId = subtask.getTaskId();
        taskManager.deleteSubtaskById(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача должна быть удалена");
        assertFalse(taskManager.getSubtasks().contains(subtask), "Подзадача должна быть удалена из списка");
    }

    @Test
        // Проверка удаления всех задач
    void shouldDeleteAllTasks() {
        taskManager.createNewTask(task);
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getTasks().isEmpty(), "Не все задачи удалены");
    }

    @Test
        // Проверка удаления всех эпиков
    void shouldDeleteAllEpics() {
        taskManager.createNewEpic(epic);
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getEpics().isEmpty(), "Не все эпики удалены");
    }

    @Test
        // Проверка удаления всех подзадач при удалении эпика
    void subtasksShouldBeDeletedWithTheirEpic() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадачи удаленного эпика не удалились");
    }

    @Test
        // Проверка удаления всех подзадач
    void shouldDeleteAllSubtasks() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Не все подзадачи удалились");
    }

    @Test
        // Проверка обновления задачи
    void tasksShouldBeUpdated() {
        taskManager.createNewTask(task);
        int taskId = task.getTaskId();

        Task newTask = new Task(taskId, "Задача №1", "Решено", TaskStatus.DONE, 23, LocalDateTime.now());
        Task result = taskManager.updateTask(newTask);

        assertNotNull(result, "Метод updateTask должен вернуть новую версию задачи");

        Task retrievedTask = taskManager.getTaskById(taskId);
        assertNotNull(retrievedTask, "Задача должна существовать после обновления");

        assertEquals(newTask.getTaskName(), retrievedTask.getTaskName(), "Имя задачи не обновилось");
        assertEquals(newTask.getTaskDescription(), retrievedTask.getTaskDescription(),
                "Описание не обновилось");
        assertEquals(newTask.getTaskStatus(), retrievedTask.getTaskStatus(), "Статус не обновился");
        assertEquals(taskId, retrievedTask.getTaskId(), "ID задачи не должен меняться");
    }

    @Test
        // Проверка обновления эпика
    void epicsShouldBeUpdated() {
        taskManager.createNewEpic(epic);
        int epicId = epic.getTaskId();
        Epic newEpic = new Epic(epicId, "Эпик №1", "Проверка");

        Epic result = taskManager.updateEpic(newEpic);
        assertNotNull(result, "Метод updateEpic должен вернуть новую версию эпика");

        Epic retrievedEpic = taskManager.getEpicById(epicId);
        assertNotNull(retrievedEpic, "Эпик должен существовать после обновления");

        assertEquals(epicId, retrievedEpic.getTaskId(), "ID эпика не должен меняться");
        assertEquals(newEpic.getTaskName(), retrievedEpic.getTaskName(), "Имя эпика не обновилось");
        assertEquals(newEpic.getTaskDescription(), retrievedEpic.getTaskDescription(),
                "Описание не обновилось");
        assertEquals(newEpic.getTaskStatus(), retrievedEpic.getTaskStatus(), "Статус не обновился");
    }

    @Test
        // Проверка обновления подзадачи
    void subtasksShouldBeUpdated() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        int subtaskId = subtask.getTaskId();

        Subtask newSubtask = new Subtask(subtaskId, "Подзадача 1",
                "Проверка", TaskStatus.IN_PROGRESS, 44, LocalDateTime.now());

        Subtask result = taskManager.updateSubtask(newSubtask);
        assertNotNull(result, "Метод updateSubtask должен вернуть новую версию эпика");

        Subtask retrievedSubtask = taskManager.getSubtaskById(subtaskId);
        assertEquals(subtaskId, retrievedSubtask.getTaskId(), "ID подзадачи не должен меняться");
        assertEquals(newSubtask.getTaskName(), retrievedSubtask.getTaskName(), "Имя подзадачи не обновилось");
        assertEquals(newSubtask.getTaskDescription(), retrievedSubtask.getTaskDescription(),
                "Описание не обновилось");
        assertEquals(newSubtask.getTaskStatus(), retrievedSubtask.getTaskStatus(), "Статус не обновился");
    }

    @Test
        // Проверка поиска по несуществующему ID
    void shouldReturnNullIfTaskNotFound() {
        assertNull(taskManager.getTaskById(88));
    }

    @Test
        // Проверка автоматического обновления статуса эпика
    void epicStatusShouldBeAutoUpdated() {
        taskManager.createNewEpic(epic);
        assertEquals(TaskStatus.NEW, epic.getTaskStatus(), "Статус нового эпика не NEW");

        taskManager.createNewSubtask(epic, subtask);
        assertEquals(subtask.getTaskStatus(), epic.getTaskStatus(),
                "Статус эпика не изменился при добавлении подзадачи с другим статусом");
    }

    @Test
        // Проверка автоматического обновления статуса эпика: все подзадачи NEW
    void epicStatusShouldBeCorrectWithNewSubtasks() {
        taskManager.createNewEpic(epic);

        subtask.setTaskStatus(TaskStatus.NEW);
        Subtask newSubtask = new Subtask("Подзадача #2", "Проверка", TaskStatus.NEW,
                duration + 1, date.plusHours(2));

        taskManager.createNewSubtask(epic, subtask);
        taskManager.createNewSubtask(epic, newSubtask);

        assertEquals(TaskStatus.NEW, epic.getTaskStatus(), "Статус эпика должен быть NEW");
    }

    @Test
        // Проверка автоматического обновления статуса эпика: все подзадачи DONE
    void epicStatusShouldBeCorrectWithDoneSubtasks() {
        taskManager.createNewEpic(epic);

        subtask.setTaskStatus(TaskStatus.DONE);
        Subtask newSubtask = new Subtask("Подзадача #2", "Проверка", TaskStatus.DONE,
                duration + 1, date.plusHours(2));

        taskManager.createNewSubtask(epic, subtask);
        taskManager.createNewSubtask(epic, newSubtask);

        assertEquals(TaskStatus.DONE, epic.getTaskStatus(), "Статус эпика должен быть DONE");
    }

    @Test
        // Проверка автоматического обновления статуса эпика: все подзадачи IN_PROGRESS
    void epicStatusShouldBeCorrectWithSubtasksInProgress() {
        taskManager.createNewEpic(epic);

        Subtask newSubtask = new Subtask("Подзадача #2", "Проверка", TaskStatus.IN_PROGRESS,
                duration + 1, date.plusHours(2));

        taskManager.createNewSubtask(epic, subtask);
        taskManager.createNewSubtask(epic, newSubtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getTaskStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
        // Проверка автоматического обновления статуса эпика: подзадачи со статусами NEW и DONE
    void epicStatusShouldBeCorrectWithDifferentStatuses() {
        taskManager.createNewEpic(epic);

        subtask.setTaskStatus(TaskStatus.NEW);
        Subtask newSubtask = new Subtask("Подзадача #2", "Проверка", TaskStatus.DONE,
                duration + 1, date.plusHours(2));

        taskManager.createNewSubtask(epic, subtask);
        taskManager.createNewSubtask(epic, newSubtask);

        assertEquals(TaskStatus.IN_PROGRESS, epic.getTaskStatus(), "Статус эпика должен быть IN_PROGRESS");
    }

    @Test
        // Проверка получения пустого приоритетного списка
    void shouldReturnEmptyPrioritizedTasks() {
        List<Task> result = taskManager.getPrioritizedTasks();

        assertNotNull(result, "Список не создан");
        assertTrue(result.isEmpty(), "Список не пуст");
    }

    @Test
        // Проверка получения списка задач в порядке приоритета
    void shouldReturnPrioritizedTasks() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        List<Task> result = taskManager.getPrioritizedTasks();

        assertTrue(result.contains(task), "Список должен содержать задачу");
        assertTrue(result.contains(subtask), "Список должен содержать подзадачу");

        assertEquals(result.getFirst(), task, "Первой должна быть задача");
        assertEquals(result.getLast(), subtask, "Последней должна быть подзадача");
    }

    @Test
        // Проверка получения списка задач в порядке приоритета после удаления задачи
    void shouldReturnPrioritizedTasksAfterTaskRemoval() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteTaskById(task.getTaskId());

        List<Task> result = taskManager.getPrioritizedTasks();

        assertFalse(result.contains(task), "Список не должен содержать задачу");
        assertTrue(result.contains(subtask), "Список должен содержать подзадачу");

        assertEquals(result.getFirst(), subtask, "Первой должна быть подзадача");
        assertEquals(result.getLast(), subtask, "Последней должна быть подзадача");
    }

    @Test
        // Проверка получения списка задач в порядке приоритета после удаления всех задач
    void shouldReturnPrioritizedTasksAfterAllTasksRemoval() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteAllTasks();

        List<Task> result = taskManager.getPrioritizedTasks();

        assertFalse(result.contains(task), "Список не должен содержать задачу");
        assertTrue(result.contains(subtask), "Список должен содержать подзадачу");

        assertEquals(result.getFirst(), subtask, "Первой должна быть подзадача");
        assertEquals(result.getLast(), subtask, "Последней должна быть подзадача");
    }

    @Test
        // Проверка получения списка задач в порядке приоритета после удаления подзадачи
    void shouldReturnPrioritizedTasksAfterSubtaskRemoval() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteSubtaskById(subtask.getTaskId());

        List<Task> result = taskManager.getPrioritizedTasks();

        assertTrue(result.contains(task), "Список должен содержать задачу");
        assertFalse(result.contains(subtask), "Список не должен содержать подзадачу");

        assertEquals(result.getFirst(), task, "Первой должна быть задача");
        assertEquals(result.getLast(), task, "Последней должна быть задача");
    }

    @Test
        // Проверка получения списка задач в порядке приоритета после удаления всех подзадач
    void shouldReturnPrioritizedTasksAfterAllSubtasksRemoval() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteAllSubtasks();

        List<Task> result = taskManager.getPrioritizedTasks();

        assertTrue(result.contains(task), "Список должен содержать задачу");
        assertFalse(result.contains(subtask), "Список не должен содержать подзадачу");

        assertEquals(result.getFirst(), task, "Первой должна быть задача");
        assertEquals(result.getLast(), task, "Последней должна быть задача");
    }

    @Test
        // Проверка получения списка задач в порядке приоритета после удаления эпика
    void shouldReturnPrioritizedTasksAfterEpicRemoval() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteEpicById(epic.getTaskId());

        List<Task> result = taskManager.getPrioritizedTasks();

        assertTrue(result.contains(task), "Список должен содержать задачу");
        assertFalse(result.contains(subtask), "Список не должен содержать подзадачу");

        assertEquals(result.getFirst(), task, "Первой должна быть задача");
        assertEquals(result.getLast(), task, "Последней должна быть задача");
    }

    @Test
        // Проверка получения списка задач в порядке приоритета после удаления всех эпиков
    void shouldReturnPrioritizedTasksAfterAllEpicsRemoval() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        taskManager.deleteAllEpics();

        List<Task> result = taskManager.getPrioritizedTasks();

        assertTrue(result.contains(task), "Список должен содержать задачу");
        assertFalse(result.contains(subtask), "Список не должен содержать подзадачу");

        assertEquals(result.getFirst(), task, "Первой должна быть задача");
        assertEquals(result.getLast(), task, "Последней должна быть задача");
    }


    @Test
        // Проверка списка задач на игнорирование задач без даты начала
    void shouldNotAddTaskWithUnchangedStartTimeInList() {
        Task newTask = new Task("Задача #1", "Проверка", TaskStatus.NEW,
                duration, LocalDateTime.MIN);

        taskManager.createNewTask(newTask);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        List<Task> result = taskManager.getPrioritizedTasks();

        assertFalse(result.contains(newTask), "Список не должен содержать задачу");
        assertTrue(result.contains(subtask), "Список должен содержать подзадачу");

        assertEquals(result.getFirst(), subtask, "Первой должна быть подзадача");
        assertEquals(result.getLast(), subtask, "Последней должна быть подзадача");
    }

    @Test
        // Проверка списка задач на игнорирование подзадач без даты начала
    void shouldNotAddSubtaskWithUnchangedStartTimeInList() {
        Subtask newSubtask = new Subtask("Подзадача #1", "Проверка", TaskStatus.NEW,
                duration, LocalDateTime.MIN);

        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, newSubtask);

        List<Task> result = taskManager.getPrioritizedTasks();

        assertFalse(result.contains(newSubtask), "Список не должен содержать подзадачу");
        assertTrue(result.contains(task), "Список должен содержать задачу");

        assertEquals(result.getFirst(), task, "Первой должна быть задача");
        assertEquals(result.getLast(), task, "Последней должна быть задача");
    }

    @Test
        // Проверка выбрасывания исключения при пересечении задач по времени
    void shouldHandleConflictingTasks() {
        String expected = "Задача пересекается по времени с уже существующей задачей.";
        taskManager.createNewTask(task);
        Task newTask = new Task("Задача #1", "Проверка", TaskStatus.NEW,
                duration, date.plusHours(1));
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskManager.createNewTask(newTask),
                "Метод должен выбрасывать IllegalStateException при пересечении задач по времени"

        );

        assertEquals(expected, exception.getMessage());
    }

    @Test
        // Проверка выбрасывания исключения при пересечении подзадач по времени
    void shouldHandleConflictingSubtasks() {
        String expected = "Подзадача пересекается по времени с уже существующей подзадачей.";
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        Subtask newSubtask = new Subtask("Подзадача #1", "Проверка", TaskStatus.IN_PROGRESS,
                duration, date.plusHours(5));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskManager.createNewSubtask(epic, newSubtask),
                "Метод должен выбрасывать IllegalStateException при пересечении подзадач по времени"

        );

        assertEquals(expected, exception.getMessage());
    }

    @Test
        // Проверка выбрасывания исключения при пересечении обновленной задачи по времени
    void shouldHandleConflictsDuringTaskUpdate() {
        Task task = new Task("Задача1", "Описание1", TaskStatus.NEW,
                60, LocalDateTime.of(2023, 1, 1, 10, 0));
        taskManager.createNewTask(task);

        Task newTask = new Task(task.getTaskId(), "Задача1", "Описание2", TaskStatus.DONE,
                60, LocalDateTime.of(2023, 1, 1, 10, 30));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskManager.updateTask(newTask),
                "Метод должен выбрасывать IllegalStateException в случае пересечения " +
                        "обновленной задачи с уже существующей"
        );

        assertEquals("Новое время для задачи пересекается с существующей.", exception.getMessage());
    }

    @Test
        // Проверка выбрасывания исключения при пересечении обновленной подзадачи по времени
    void shouldHandleConflictsDuringSubtaskUpdate() {
        taskManager.createNewEpic(epic);
        Subtask subtask = new Subtask("Задача1", "Описание1", TaskStatus.NEW,
                60, LocalDateTime.of(2023, 1, 1, 10, 0));
        taskManager.createNewSubtask(epic, subtask);

        Subtask newSubtask = new Subtask(subtask.getTaskId(), "Задача1", "Описание2",
                TaskStatus.DONE, 60, LocalDateTime.of(2023, 1, 1, 10, 30));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskManager.updateSubtask(newSubtask),
                "Метод должен выбрасывать IllegalStateException в случае пересечения " +
                        "обновленной подзадачи с уже существующей"
        );

        assertEquals("Новое время для подзадачи пересекается с существующей.", exception.getMessage());
    }

    @Test
        // Проверка нахождения в эпиках только актуальных ID подзадач
    void shouldBeActualSubtaskIdsInEpic() {
        taskManager.createNewEpic(epic);
        Subtask newSubtask = new Subtask("Подзадача 2", "Проверка", TaskStatus.DONE, 19, LocalDateTime.now());
        taskManager.createNewSubtask(epic, subtask);
        taskManager.createNewSubtask(epic, newSubtask);

        List<Integer> subtasksIds;

        subtasksIds = epic.getSubtasksIds();
        assertTrue(subtasksIds.contains(subtask.getTaskId()), "В эпике нет ID подзадачи 1");
        assertTrue(subtasksIds.contains(newSubtask.getTaskId()), "В эпике нет ID подзадачи 2");

        taskManager.deleteSubtaskById(subtask.getTaskId());

        subtasksIds = epic.getSubtasksIds();
        assertFalse(subtasksIds.contains(subtask.getTaskId()), "В эпике осталось ID подзадачи 1");
        assertTrue(subtasksIds.contains(newSubtask.getTaskId()), "В эпике нет ID подзадачи 2");
    }

    @Test
        // Проверка на возврат NULL при пустом списке подзадач эпика
    void shouldReturnNullWhenSubtasksListIsEmpty() {
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertTrue(subtasks.isEmpty());
    }

    @Test
        // Проверка на возврат списка подзадач эпика
    void shouldReturnSubtasksList() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(1, subtasks.size(), "Должна быть одна подзадача");
        assertEquals(subtask, subtasks.getFirst(), "Подзадача должна совпадать с добавленной");

        Subtask newSubtask = new Subtask("Подзадача 1", "Проверка", TaskStatus.DONE, 14, LocalDateTime.now());
        taskManager.createNewSubtask(epic, newSubtask);

        subtasks = taskManager.getSubtasks();

        assertEquals(2, subtasks.size(), "Должно быть две подзадачи");
        assertTrue(subtasks.contains(subtask) && subtasks.contains(newSubtask),
                "Обе подзадачи должны быть в списке");

    }

}
