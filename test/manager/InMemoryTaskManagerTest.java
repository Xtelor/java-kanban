package manager;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return (InMemoryTaskManager) Managers.getDefault();
    }

    @Test // Проверка равенства двух задач с одинаковым ID
    void tasksWithSameIdShouldBeEqual() {
        taskManager.createNewTask(task);
        Task anotherTask = new Task(1, "Задача №2",
                "Описание 2", TaskStatus.IN_PROGRESS, 11, LocalDateTime.now());
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
                "Описание 2", TaskStatus.DONE, 111, LocalDateTime.now());
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
                epic.getTaskDescription(), epic.getTaskStatus(), epic.getDuration().toMinutes(), epic.getStartTime());
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
                "Обновление", TaskStatus.IN_PROGRESS, 122, LocalDateTime.now());
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
                "Обновление", TaskStatus.IN_PROGRESS, 222, LocalDateTime.now());
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
                "Обновление", TaskStatus.DONE, 11, LocalDateTime.now());
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
                "Обновление", TaskStatus.DONE, 12, LocalDateTime.now());
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
        Task newTask = new Task(20,"Задача 1", "Вручную", TaskStatus.NEW, 12, LocalDateTime.now());
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
                "Вручную", TaskStatus.IN_PROGRESS, 13, LocalDateTime.now());
        taskManager.createNewSubtask(epic, newSubtask);

        assertNotEquals(newSubtask.getTaskId(), subtask.getTaskId(), "Конфликт ID");
        assertNotNull(taskManager.getSubtaskById(subtask.getTaskId()), "Подзадача недоступна");
        assertNotNull(taskManager.getSubtaskById(newSubtask.getTaskId()), "Новая подзадача недоступна");
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
        Subtask subtask2 = new Subtask("Подзадача2","Описание2",TaskStatus.IN_PROGRESS, 15, LocalDateTime.now());
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
        Task task2 = new Task("Задача2","Описание2",TaskStatus.DONE, 16, LocalDateTime.now());
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
        Subtask newSubtask = new Subtask("Подзадача 2", "Проверка", TaskStatus.DONE, 17, LocalDateTime.now());
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
        taskManager.getEpicById(epic2.getTaskId());
        taskManager.deleteAllEpics();

        assertTrue(taskManager.getHistory().isEmpty(), "Эпики должны быть удалены из истории");
    }

    @Test // Проверка удаления подзадач из истории при удалении абсолютно всех эпиков
    void shouldRemoveSubtasksFromHistoryIfEpicsDeleted() {
        Epic epic2 = new Epic("Эпик2","Описание2");
        taskManager.createNewEpic(epic);
        taskManager.createNewEpic(epic2);

        Subtask newSubtask = new Subtask("Подзадача 2", "Проверка", TaskStatus.DONE, 18, LocalDateTime.now());
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

    // Проверка наличия задачи/эпика в истории
    private boolean isContainsTask(List<Task> history, int taskIdentifier,
                                   String name, String description,
                                   TaskStatus status) {

        return history.stream().anyMatch(task ->
                task.getTaskId() == taskIdentifier &&
                        Objects.equals(task.getTaskName(), name) &&
                        Objects.equals(task.getTaskDescription(), description) &&
                        task.getTaskStatus() == status
        );
    }

    // Проверка наличия подзадачи в истории
    private boolean isContainsTask(List<Task> history, int taskIdentifier,
                                   String name, String description,
                                   TaskStatus status, int epicIdentifier) {

        return history.stream()
                .filter(task -> task.getTaskId() == taskIdentifier &&
                        Objects.equals(task.getTaskName(), name) &&
                        Objects.equals(task.getTaskDescription(), description) &&
                        task.getTaskStatus() == status &&
                        task instanceof Subtask
                )
                .map(Subtask.class::cast)
                .anyMatch(subtask -> subtask.getEpicIdentifier() == epicIdentifier);
    }

}
