package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;
    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void beforeEach() {
        taskManager = Managers.getDefault();

        task = new Task("Задача #1","Проверка", TaskStatus.NEW);
        epic = new Epic("Эпик #1","Проверка");
        subtask = new Subtask("Подзадача #1", "Проверка", TaskStatus.IN_PROGRESS);
    }

    @Test // Проверка создания задачи
    void shouldCreateTask() {
        taskManager.createNewTask(task);
        assertTrue(taskManager.getTasks().contains(task), "Задача не добавлена");
    }

    @Test // Проверка создания эпика
    void shouldCreateEpic() {
        taskManager.createNewEpic(epic);
        assertTrue(taskManager.getEpics().contains(epic),"Эпик не добавлен");
    }

    @Test // Проверка создания подзадачи
    void shouldCreateSubtask() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic,subtask);
        assertTrue(taskManager.getSubtasks().contains(subtask), "Подзадача не добавлена");
    }

    @Test // Проверка поиска задачи по ID
    void shouldFindTaskById() {
        assertNull(taskManager.getTaskById(task.getTaskId()), "Изначально не должно быть задач");

        taskManager.createNewTask(task);
        Task foundTask = taskManager.getTaskById(task.getTaskId());

        assertNotNull(foundTask, "Задача не найдена");
        assertEquals(task, foundTask, "Найденная задача не совпадает с действительной");
    }

    @Test // Проверка нахождения задачи в истории
    void shouldFindTaskInHistory() {
        taskManager.createNewTask(task);
        taskManager.getTaskById(task.getTaskId());

        List<Task> history = taskManager.getHistory();
        assertTrue(history.contains(task), "Задачи нет в истории просмотров");
        assertEquals(1, history.size(), "В истории должна быть одна задача");
    }

    @Test // Проверка поиска эпика по ID
    void shouldFindEpicById() {
        assertNull(taskManager.getEpicById(epic.getTaskId()), "Изначально не должно быть эпиков");

        taskManager.createNewEpic(epic);
        Epic foundEpic = taskManager.getEpicById(epic.getTaskId());

        assertNotNull(foundEpic, "Эпик не найден");
        assertEquals(epic, foundEpic, "Найденный эпик не совпадает с действительным");
    }

    @Test // Проверка нахождения эпика в истории
    void shouldFindEpicInHistory() {
        taskManager.createNewEpic(epic);
        taskManager.getEpicById(epic.getTaskId());

        List<Task> history = taskManager.getHistory();
        boolean isEpicFound = false;

        for (Task task : history) {
            if (task.getTaskId() == epic.getTaskId()) {
                isEpicFound = true;
                break;
            }
        }

        assertTrue(isEpicFound, "Эпика нет в истории просмотров");
        assertEquals(1, history.size(), "В истории должен быть один эпик");
    }

    @Test // Проверка поиска подзадачи по ID
    void shouldFindSubtaskById() {
        assertNull(taskManager.getSubtaskById(subtask.getTaskId()), "Изначально не должно быть подзадач");

        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        Subtask foundSubtask = taskManager.getSubtaskById(subtask.getTaskId());

        assertNotNull(foundSubtask, "Подзадача не найдена");
        assertEquals(subtask, foundSubtask, "Найденная подзадача не совпадает с действительной");
    }

    @Test // Проверка нахождения подзадачи в истории
    void shouldFindSubtaskInHistory() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.getSubtaskById(subtask.getTaskId());

        List<Task> history = taskManager.getHistory();
        boolean isSubtaskFound = false;

        for (Task task : history) {
            if (task.getTaskId() == subtask.getTaskId()) {
                isSubtaskFound = true;
                break;
            }
        }

        assertTrue(isSubtaskFound, "Подзадачи нет в истории просмотров");
        assertEquals(1, history.size(), "В истории должна быть одна подзадача");
    }

    @Test // Проверка поиска по несуществующему ID
    void shouldReturnNullIfTaskNotFound() {
        assertNull(taskManager.getTaskById(88));
    }

    @Test // Проверка списка задач
    void shouldGetTasksList() {
        assertTrue(taskManager.getTasks().isEmpty(), "Список задач должен быть пуст");

        taskManager.createNewTask(task);
        List<Task> tasks = taskManager.getTasks();

        assertFalse(tasks.isEmpty(), "Список задач не должен быть пуст");
        assertEquals(1, tasks.size(), "В списке должна быть одна задача");
        assertTrue(tasks.contains(task), "Добавленной задачи нет в списке");
    }

    @Test // Проверка списка эпиков
    void shouldGetEpicsList() {
        assertTrue(taskManager.getEpics().isEmpty(), "Список эпиков должен быть пуст");

        taskManager.createNewEpic(epic);
        List<Epic> epics = taskManager.getEpics();

        assertFalse(epics.isEmpty(), "Список эпиков не должен быть пуст");
        assertEquals(1, epics.size(), "В списке должен быть один эпик");
        assertTrue(epics.contains(epic), "Добавленного эпика нет в списке");
    }

    @Test // Проверка списка подзадач
    void shouldGetSubtasksList() {
        assertTrue(taskManager.getSubtasks().isEmpty(), "Список подзадач должен быть пуст");

        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        List<Subtask> subtasks = taskManager.getSubtasks();

        assertFalse(subtasks.isEmpty(), "Список подзадач не должен быть пуст");
        assertEquals(1, subtasks.size(), "В списке должна быть одна подзадача");
        assertTrue(subtasks.contains(subtask), "Добавленной подзадачи нет в списке");
    }

    @Test // Проверка удаления задачи по ID
    void shouldDeleteTaskById() {
        taskManager.createNewTask(task);

        int taskId = task.getTaskId();
        taskManager.deleteTaskById(taskId);

        assertNull(taskManager.getTaskById(taskId), "Задача должна быть удалена");
        assertFalse(taskManager.getTasks().contains(task), "Задача должна быть удалена из списка");
    }

    @Test // Проверка удаления эпика по ID
    void shouldDeleteEpicById() {
        taskManager.createNewEpic(epic);

        int epicId = epic.getTaskId();
        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getEpicById(epicId), "Эпик должен быть удален");
        assertFalse(taskManager.getEpics().contains(epic), "Эпик должен быть удален из списка");
    }

    @Test // Проверка удаления подзадачи по ID
    void shouldDeleteSubtaskById() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        int subtaskId = subtask.getTaskId();
        taskManager.deleteSubtaskById(subtaskId);

        assertNull(taskManager.getSubtaskById(subtaskId), "Подзадача должна быть удалена");
        assertFalse(taskManager.getSubtasks().contains(subtask),"Подзадача должна быть удалена из списка");
    }

    @Test // Проверка удаления всех задач
    void shouldDeleteAllTasks() {
        taskManager.createNewTask(task);
        taskManager.deleteAllTasks();
        assertTrue(taskManager.getTasks().isEmpty(), "Не все задачи удалены");
    }

    @Test // Проверка удаления всех эпиков
    void shouldDeleteAllEpics() {
        taskManager.createNewEpic(epic);
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getEpics().isEmpty(), "Не все эпики удалены");
    }

    @Test // Проверка удаления всех подзадач при удалении эпика
    void subtasksShouldBeDeletedWithTheirEpic() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.deleteAllEpics();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Подзадачи удаленного эпика не удалились");
    }

    @Test // Проверка удаления всех подзадач
    void shouldDeleteAllSubtasks() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.deleteAllSubtasks();
        assertTrue(taskManager.getSubtasks().isEmpty(), "Не все подзадачи удалились");
    }

    @Test // Проверка обновления задачи
    void tasksShouldBeUpdated() {
        taskManager.createNewTask(task);
        int taskId = task.getTaskId();

        Task newTask = new Task(taskId, "Задача №1", "Решено", TaskStatus.DONE);
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

    @Test // Проверка обновления эпика
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

    @Test // Проверка обновления подзадачи
    void  subtasksShouldBeUpdated() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        int subtaskId = subtask.getTaskId();

        Subtask newSubtask = new Subtask(subtaskId, "Подзадача 1",
                            "Проверка", TaskStatus.IN_PROGRESS);

        Subtask result = taskManager.updateSubtask(newSubtask);
        assertNotNull(result, "Метод updateSubtask должен вернуть новую версию эпика");

        Subtask retrievedSubtask = taskManager.getSubtaskById(subtaskId);
        assertEquals(subtaskId, retrievedSubtask.getTaskId(), "ID подзадачи не должен меняться");
        assertEquals(newSubtask.getTaskName(), retrievedSubtask.getTaskName(), "Имя подзадачи не обновилось");
        assertEquals(newSubtask.getTaskDescription(), retrievedSubtask.getTaskDescription(),
                "Описание не обновилось");
        assertEquals(newSubtask.getTaskStatus(), retrievedSubtask.getTaskStatus(), "Статус не обновился");
    }

    @Test // Проверка равенства двух задач с одинаковым ID
    void tasksWithSameIdShouldBeEqual() {
        taskManager.createNewTask(task);
        Task anotherTask = new Task(1, "Задача №2",
                                    "Описание 2", TaskStatus.IN_PROGRESS);
        assertEquals(task, anotherTask, "Задачи с одинаковым id должны быть равны");
    }

    @Test // Проверка равенства двух эпиков с одинаковым ID
    void epicsWithSameIdShouldBeEqual() {
        taskManager.createNewEpic(epic);
        Epic anotherEpic = new Epic(1, "Эпик №2", "Описание 2");
        assertEquals(epic, anotherEpic, "Эпики с одинаковым id должны быть равны");
    }

    @Test // Проверка равенства двух подзадач с одинаковым ID
    void subtasksWithSameIdShouldBeEqual() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        Subtask anotherSubtask = new Subtask(2, "Подзадача 2",
                                             "Описание 2", TaskStatus.DONE);
        assertEquals(subtask, anotherSubtask, "Подзадачи с одинаковым id должны быть равны");
    }

    @Test // Проверка невозможности сделать подзадачу своим эпиком
    void subtaskCantBeItsOwnEpic() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        subtask.setEpicIdentifier(subtask.getTaskId());
        assertNotEquals(subtask.getTaskId(), subtask.getEpicIdentifier(),
                "Подзадача не может быть своим эпиком");
    }

    @Test // Проверка невозможности добавления эпика в самого себя в виде подзадачи
    void epicCantBeAddedIntoItself() {
        taskManager.createNewEpic(epic);
        Subtask anotherSubtask = new Subtask(epic.getTaskId(), epic.getTaskName(),
                                             epic.getTaskDescription(), epic.getTaskStatus());
        taskManager.createNewSubtask(epic,anotherSubtask);
        assertTrue(taskManager.getEpicSubtasks(epic).isEmpty(),
                "Список подзадач эпика должен быть пустым");
    }

    @Test // Проверка менеджера истории на возврат старой версии задачи
    void historyManagerShouldContainPreviousVersionsOfTasks() {
        taskManager.createNewTask(task);
        taskManager.getTaskById(task.getTaskId());

        String oldName = task.getTaskName();
        String oldDescription = task.getTaskDescription();
        TaskStatus oldStatus = task.getTaskStatus();

        Task newTask = new Task(task.getTaskId(), "Задача 1",
                   "Обновление", TaskStatus.IN_PROGRESS);
        taskManager.updateTask(newTask);

        assertTrue(isContainsTask(taskManager.getHistory(), task.getTaskId(),
                                  oldName, oldDescription, oldStatus),
                          "История должна содержать предыдущую версию задачи");
    }

    @Test // Проверка менеджера истории при изменении задачи
    void historyManagerShouldNotContainUpdatedTasksVersions() {
        taskManager.createNewTask(task);
        taskManager.getTaskById(task.getTaskId());

        Task newTask = new Task(task.getTaskId(), "Задача 1",
                   "Обновление", TaskStatus.IN_PROGRESS);
        taskManager.updateTask(newTask);

        assertFalse(isContainsTask(taskManager.getHistory(), newTask.getTaskId(),
                                   newTask.getTaskName(), newTask.getTaskDescription(),
                                   newTask.getTaskStatus()),
                           "История не должна содержать обновленную версию задачи до повторного просмотра");

    }

    @Test // Проверка менеджера истории на возврат старой версии эпика
    void historyManagerShouldContainPreviousVersionsOfEpics() {
        taskManager.createNewEpic(epic);
        taskManager.getEpicById(epic.getTaskId());

        String oldName = epic.getTaskName();
        String oldDescription = epic.getTaskDescription();
        TaskStatus oldStatus = epic.getTaskStatus();

        Epic newEpic = new Epic(epic.getTaskId(), "Эпик 1", "Обновление");
        taskManager.updateEpic(newEpic);

        assertTrue(isContainsTask(taskManager.getHistory(), epic.getTaskId(),
                                  oldName, oldDescription, oldStatus),
                          "История должна содержать предыдущую версию эпика");

        taskManager.createNewSubtask(newEpic, subtask); //Проверка при добавлении подзадачи
        taskManager.updateEpic(newEpic);

        assertTrue(isContainsTask(taskManager.getHistory(), epic.getTaskId(),
                                  oldName, oldDescription, oldStatus),
                          "История должна содержать предыдущую версию эпика");
    }

    @Test  // Проверка менеджера истории при изменении эпика
    void historyManagerShouldNotContainUpdatedEpicVersions() {
        taskManager.createNewEpic(epic);
        taskManager.getEpicById(epic.getTaskId());

        String oldName = epic.getTaskName();
        String oldDescription = epic.getTaskDescription();
        TaskStatus oldStatus = epic.getTaskStatus();

        Epic newEpic = new Epic(epic.getTaskId(), "Эпик 1", "Обновление");
        taskManager.updateEpic(newEpic);

        List<Task> history = taskManager.getHistory();

        boolean isContainsOldVersion = isContainsTask(history, epic.getTaskId(), oldName, oldDescription, oldStatus);
        boolean isContainsNewVersion = isContainsTask(history, newEpic.getTaskId(), newEpic.getTaskName(),
                                                      newEpic.getTaskDescription(), newEpic.getTaskStatus());

        assertTrue(isContainsOldVersion, "История должна содержать старую версию эпика");
        assertFalse(isContainsNewVersion,
                "История не должна содержать новую версию эпика без повторного просмотра");
    }

    @Test // Проверка менеджера истории на возврат старой версии подзадачи
    void historyManagerShouldContainPreviousVersionsOfSubtasks() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.getSubtaskById(subtask.getTaskId());

        Subtask oldSubtask = taskManager.getSubtaskById(subtask.getTaskId());

        String oldName = oldSubtask.getTaskName();
        String oldDescription = oldSubtask.getTaskDescription();
        TaskStatus oldStatus = oldSubtask.getTaskStatus();
        int oldEpicId = oldSubtask.getEpicIdentifier();

        Subtask newSubtask = new Subtask(oldSubtask.getTaskId(),"Подзадача 1",
                            "Обновление", TaskStatus.DONE);
        newSubtask.setEpicIdentifier(oldEpicId);
        taskManager.updateSubtask(newSubtask);

        assertTrue(isContainsTask(taskManager.getHistory(), oldSubtask.getTaskId(),
                                  oldName, oldDescription, oldStatus, oldEpicId),
                          "История должна содержать предыдущую версию подзадачи");

    }

    @Test  // Проверка менеджера истории при изменении подзадачи
    void historyManagerShouldNotContainUpdatedSubtasksVersions() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        taskManager.getSubtaskById(subtask.getTaskId());

        Subtask oldSubtask = taskManager.getSubtaskById(subtask.getTaskId());

        String oldName = oldSubtask.getTaskName();
        String oldDescription = oldSubtask.getTaskDescription();
        TaskStatus oldStatus = oldSubtask.getTaskStatus();
        int oldEpicId = oldSubtask.getEpicIdentifier();

        Subtask newSubtask = new Subtask(oldSubtask.getTaskId(),"Подзадача 1",
                "Обновление", TaskStatus.DONE);
        newSubtask.setEpicIdentifier(oldEpicId);
        taskManager.updateSubtask(newSubtask);

        List<Task> history = taskManager.getHistory();

        boolean isContainsOldVersion = isContainsTask(history, oldSubtask.getTaskId(), oldName,
                                                      oldDescription, oldStatus, oldEpicId);
        boolean isContainsNewVersion = isContainsTask(history, newSubtask.getTaskId(),
                                                      newSubtask.getTaskName(), newSubtask.getTaskDescription(),
                                                      newSubtask.getTaskStatus(), newSubtask.getEpicIdentifier());

        assertTrue(isContainsOldVersion, "История должна содержать старую версию подзадачи");
        assertFalse(isContainsNewVersion,
                "История не должна содержать новую версию подзадачи без повторного просмотра");
    }

    @Test // Проверка задачи на неизменность при добавлении
    void taskShouldRemainUnchangedAfterAdding() {
        String taskName = task.getTaskName();
        String taskDescription = task.getTaskDescription();
        TaskStatus status = task.getTaskStatus();

        taskManager.createNewTask(task);

        assertTrue(task.getTaskId() > 0, "ID задачи не установлен");
        assertEquals(taskName, task.getTaskName(), "Имя задачи изменилось");
        assertEquals(taskDescription, task.getTaskDescription(), "Описание задачи изменилось");
        assertEquals(status, task.getTaskStatus(), "Статус задачи изменился");
    }

    @Test // Проверка эпика на неизменность при добавлении
    void epicShouldRemainUnchangedAfterAdding() {
        String epicName = epic.getTaskName();
        String epicDescription = epic.getTaskDescription();

        taskManager.createNewEpic(epic);

        assertTrue(epic.getTaskId() > 0, "ID эпика не установлен");
        assertEquals(epicName, epic.getTaskName(), "Имя эпика изменилось");
        assertEquals(epicDescription, epic.getTaskDescription(), "Описание эпика изменилось");
        assertEquals(TaskStatus.NEW, epic.getTaskStatus(), "Статус эпика изменился");
        assertTrue(epic.getSubtasks().isEmpty(), "Список подзадач не пуст");
    }

    @Test // Проверка подзадачи на неизменность при добавлении
    void subtaskShouldRemainUnchangedAfterAdding() {
        taskManager.createNewEpic(epic);
        int epicId = epic.getTaskId();
        assertTrue(epicId > 0, "Эпик должен получить свой ID");

        String subtaskName = subtask.getTaskName();
        String subtaskDescription = subtask.getTaskDescription();
        TaskStatus status = subtask.getTaskStatus();

        taskManager.createNewSubtask(epic, subtask);

        assertTrue(subtask.getTaskId() > 0, "ID подзадачи не установлен");
        assertEquals(subtaskName, subtask.getTaskName(), "Имя подзадачи изменилось");
        assertEquals(subtaskDescription, subtask.getTaskDescription(), "Описание подзадачи изменилось");
        assertEquals(status, subtask.getTaskStatus(), "Статус подзадачи изменился");
        assertEquals(epicId, subtask.getEpicIdentifier(), "ID эпика изменился");

    }

    @Test // Проверка задач на отсутствие конфликтов при ручном и автоматическом задании ID
    void tasksShouldNotConflict() {
        taskManager.createNewTask(task);
        Task newTask = new Task(20,"Задача 1", "Вручную", TaskStatus.NEW);
        taskManager.createNewTask(newTask);

        // Проверка на конфликт ручного и автоматического ID
        assertNotEquals(newTask.getTaskId(), task.getTaskId(), "Конфликт ID");

        // Проверяем доступность задач
        assertNotNull(taskManager.getTaskById(task.getTaskId()), "Задача недоступна");
        assertNotNull(taskManager.getTaskById(newTask.getTaskId()), "Новая задача недоступна");
    }

    @Test // Проверка эпиков на отсутствие конфликтов при ручном и автоматическом задании ID
    void epicsShouldNotConflict() {
        taskManager.createNewEpic(epic);
        Epic newEpic = new Epic(20, "Эпик 1", "Вручную");
        taskManager.createNewEpic(newEpic);

        assertNotEquals(newEpic.getTaskId(), epic.getTaskId(), "Конфликт ID");

        assertNotNull(taskManager.getEpicById(epic.getTaskId()), "Эпик недоступен");
        assertNotNull(taskManager.getEpicById(newEpic.getTaskId()), "Новый эпик недоступен");
    }

    @Test // Проверка подзадач на отсутствие конфликтов при ручном и автоматическом задании ID
    void subtasksShouldNotConflict() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);
        Subtask newSubtask = new Subtask(20,"Подзадача 1",
                             "Вручную", TaskStatus.IN_PROGRESS);
        taskManager.createNewSubtask(epic, newSubtask);

        assertNotEquals(newSubtask.getTaskId(), subtask.getTaskId(), "Конфликт ID");
        assertNotNull(taskManager.getSubtaskById(subtask.getTaskId()), "Подзадача недоступна");
        assertNotNull(taskManager.getSubtaskById(newSubtask.getTaskId()), "Новая подзадача недоступна");
    }

    @Test // Проверка автоматического обновления статуса эпика
    void epicStatusShouldBeAutoUpdated() {
        taskManager.createNewEpic(epic);
        assertEquals(TaskStatus.NEW, epic.getTaskStatus(), "Статус нового эпика не NEW");

        taskManager.createNewSubtask(epic, subtask);
        assertEquals(subtask.getTaskStatus(), epic.getTaskStatus(),
                "Статус эпика не изменился при добавлении подзадачи с другим статусом");

    }

    @Test // Проверка на возврат NULL при пустом списке подзадач эпика
    void shouldReturnNullWhenSubtasksListIsEmpty() {
        List<Subtask> subtasks = taskManager.getSubtasks();
        assertTrue(subtasks.isEmpty());


    }

    @Test // Проверка на возврат списка подзадач эпика
    void shouldReturnSubtasksList() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        List<Subtask> subtasks = taskManager.getSubtasks();

        assertEquals(1, subtasks.size(), "Должна быть одна подзадача");
        assertEquals(subtask, subtasks.getFirst(), "Подзадача должна совпадать с добавленной");

        Subtask newSubtask = new Subtask("Подзадача 1", "Проверка", TaskStatus.DONE);
        taskManager.createNewSubtask(epic, newSubtask);

        subtasks = taskManager.getSubtasks();

        assertEquals(2, subtasks.size(), "Должно быть две подзадачи");
        assertTrue(subtasks.contains(subtask) && subtasks.contains(newSubtask),
                   "Обе подзадачи должны быть в списке");

    }

    @Test // Проверка на различие ID при создании
    void testTaskIdsAreUnique() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic, subtask);

        assertNotEquals(task.getTaskId(), epic.getTaskId(), "ID задач должны быть разными");
        assertNotEquals(task.getTaskId(), subtask.getTaskId(), "ID задач должны быть разными");
    }

    @Test // Проверка удаления задачи из истории при удалении задачи
    void shouldRemoveTaskFromHistoryIfTaskDeleted(){
        taskManager.createNewTask(task);
        taskManager.getTaskById(task.getTaskId());

        assertTrue(taskManager.getHistory().contains(task), "Задача должна быть в истории");

        taskManager.deleteTaskById(task.getTaskId());

        assertTrue(taskManager.getHistory().isEmpty(), "Задача должна быть удалена из истории");
    }

    @Test // Проверка удаления подзадачи из истории при удалении подзадачи
    void shouldRemoveSubtaskFromHistoryIfSubtaskDeleted() {
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic,subtask);
        taskManager.getSubtaskById(subtask.getTaskId());

        assertTrue(taskManager.getHistory().contains(subtask), "Подзадача должна быть в истории");

        taskManager.deleteSubtaskById(subtask.getTaskId());

        assertTrue(taskManager.getHistory().isEmpty(), "Подзадача должна быть удалена из истории");
    }

    @Test // Проверка удаления эпика из истории при удалении эпика
    void shouldRemoveEpicFromHistoryIfEpicDeleted() {
        taskManager.createNewEpic(epic);
        taskManager.getEpicById(epic.getTaskId());

        assertTrue(taskManager.getHistory().contains(epic), "Эпик должен быть в истории");

        taskManager.deleteEpicById(epic.getTaskId());

        assertTrue(taskManager.getHistory().isEmpty(), "Эпик должен быть удален из истории");
    }

    @Test // Проверка удаления подзадач из истории при удалении эпика
    void shouldRemoveSubtasksFromHistoryIfEpicDeleted() {
        taskManager.createNewEpic(epic);
        Subtask subtask2 = new Subtask("Подзадача2","Описание2",TaskStatus.IN_PROGRESS);
        taskManager.createNewSubtask(epic,subtask);
        taskManager.createNewSubtask(epic,subtask2);

        taskManager.getSubtaskById(subtask.getTaskId());
        taskManager.getSubtaskById(subtask2.getTaskId());

        assertTrue(taskManager.getHistory().contains(subtask), "Подзадача должна быть в истории");
        assertTrue(taskManager.getHistory().contains(subtask2), "Подзадача2 должна быть в истории");

        taskManager.deleteEpicById(epic.getTaskId());

        assertTrue(taskManager.getHistory().isEmpty(), "Подзадачи должны быть удалены из истории");
    }

    @Test // Проверка удаления задач из истории при удалении абсолютно всех задач
    void shouldRemoveTasksFromHistoryIfTasksDeleted() {
        Task task2 = new Task("Задача2","Описание2",TaskStatus.DONE);
        taskManager.createNewTask(task);
        taskManager.createNewTask(task2);

        taskManager.getTaskById(task.getTaskId());
        taskManager.getTaskById(task2.getTaskId());
        taskManager.deleteAllTasks();

        assertTrue(taskManager.getHistory().isEmpty(), "Задачи не удалились");
    }

    @Test // Проверка удаления подзадач из истории при удалении абсолютно всех подзадач
    void shouldRemoveSubtasksFromHistoryIfSubtasksDeleted() {
        taskManager.createNewEpic(epic);
        Subtask newSubtask = new Subtask("Подзадача 2", "Проверка", TaskStatus.DONE);
        taskManager.createNewSubtask(epic,subtask);
        taskManager.createNewSubtask(epic,newSubtask);

        taskManager.getSubtaskById(subtask.getTaskId());
        taskManager.getSubtaskById(newSubtask.getTaskId());
        taskManager.deleteAllSubtasks();

        assertTrue(taskManager.getHistory().isEmpty(), "Подзадачи должны быть удалены из истории");
    }

    @Test // Проверка удаления эпиков из истории при удалении абсолютно всех эпиков
    void shouldRemoveEpicsFromHistoryIfEpicsDeleted() {
        Epic epic2 = new Epic("Эпик2","Описание2");
        taskManager.createNewEpic(epic);
        taskManager.createNewEpic(epic2);

        taskManager.getEpicById(epic.getTaskId());
        taskManager.getSubtaskById(epic2.getTaskId());
        taskManager.deleteAllEpics();

        assertTrue(taskManager.getHistory().isEmpty(), "Эпики должны быть удалены из истории");
    }

    @Test // Проверка удаления подзадач из истории при удалении абсолютно всех эпиков
    void shouldRemoveSubtasksFromHistoryIfEpicsDeleted() {
        Epic epic2 = new Epic("Эпик2","Описание2");
        taskManager.createNewEpic(epic);
        taskManager.createNewEpic(epic2);

        Subtask newSubtask = new Subtask("Подзадача 2", "Проверка", TaskStatus.DONE);
        taskManager.createNewSubtask(epic,subtask);
        taskManager.createNewSubtask(epic,newSubtask);

        taskManager.getSubtaskById(subtask.getTaskId());
        taskManager.getSubtaskById(newSubtask.getTaskId());
        taskManager.getEpicById(epic2.getTaskId());
        taskManager.deleteAllEpics();

        assertTrue(taskManager.getHistory().isEmpty(), "Подзадачи должны быть удалены из истории");
    }

    @Test // Проверка удаления только задач из истории
    void shouldRemoveOnlyTasksFromHistory() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic,subtask);

        taskManager.getTaskById(task.getTaskId());
        taskManager.getEpicById(epic.getTaskId());
        taskManager.getSubtaskById(subtask.getTaskId());

        taskManager.deleteAllTasks();
        List<Task> history = taskManager.getHistory();

        assertTrue(history.contains(epic));
        assertTrue(history.contains(subtask));
        assertFalse(history.contains(task));
    }

    @Test // Проверка удаления только подзадач из истории
    void shouldRemoveOnlySubtasksFromHistory() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic,subtask);

        taskManager.getTaskById(task.getTaskId());
        taskManager.getEpicById(epic.getTaskId());
        taskManager.getSubtaskById(subtask.getTaskId());

        taskManager.deleteAllSubtasks();
        List<Task> history = taskManager.getHistory();

        assertTrue(history.contains(epic));
        assertTrue(history.contains(task));
        assertFalse(history.contains(subtask));
    }

    @Test // Проверка удаления только эпиков из истории - должны остаться только задачи
    void shouldRemainOnlyTasksInHistory() {
        taskManager.createNewTask(task);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubtask(epic,subtask);

        taskManager.getTaskById(task.getTaskId());
        taskManager.getEpicById(epic.getTaskId());
        taskManager.getSubtaskById(subtask.getTaskId());

        taskManager.deleteAllEpics();
        List<Task> history = taskManager.getHistory();

        assertFalse(history.contains(epic));
        assertTrue(history.contains(task));
        assertFalse(history.contains(subtask));
    }

    @Test // Проверка нахождения в эпиках только актуальных ID подзадач
    void shouldBeActualSubtaskIdsInEpic() {
        taskManager.createNewEpic(epic);
        Subtask newSubtask = new Subtask("Подзадача 2", "Проверка", TaskStatus.DONE);
        taskManager.createNewSubtask(epic,subtask);
        taskManager.createNewSubtask(epic,newSubtask);

        List<Integer> subtasksIds;

        subtasksIds = epic.getSubtasksIds();
        assertTrue(subtasksIds.contains(subtask.getTaskId()), "В эпике нет ID подзадачи 1");
        assertTrue(subtasksIds.contains(newSubtask.getTaskId()), "В эпике нет ID подзадачи 2");

        taskManager.deleteSubtaskById(subtask.getTaskId());

        subtasksIds = epic.getSubtasksIds();
        assertFalse(subtasksIds.contains(subtask.getTaskId()), "В эпике осталось ID подзадачи 1");
        assertTrue(subtasksIds.contains(newSubtask.getTaskId()), "В эпике нет ID подзадачи 2");
    }

    // Проверка наличия задачи/эпика в истории
    private boolean isContainsTask(List<Task> history, int taskIdentifier,
                                   String name, String description,
                                   TaskStatus status) {
        for (Task task : history) {
            if (task.getTaskId() == taskIdentifier &&
                task.getTaskName().equals(name) &&
                task.getTaskDescription().equals(description) &&
                task.getTaskStatus() == status) {
                    return true;
            }
        }
        return false;
    }

    // Проверка наличия подзадачи в истории
    private boolean isContainsTask(List<Task> history, int taskIdentifier,
                                   String name, String description,
                                   TaskStatus status, int epicIdentifier) {
        for (Task task : history) {
            if (task.getTaskId() == taskIdentifier &&
                task.getTaskName().equals(name) &&
                task.getTaskDescription().equals(description) &&
                task.getTaskStatus() == status &&
                task instanceof Subtask) {
                    return ((Subtask) task).getEpicIdentifier() == epicIdentifier;
            }
        }
        return false;
    }


}
