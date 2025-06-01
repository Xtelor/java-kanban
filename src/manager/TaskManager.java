package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    // Создание новой задачи
    void createNewTask(Task task);

    // Получение списка задач
    ArrayList<Task> getTasks();

    // Удаление всех задач
    void deleteAllTasks();

    // Обновление задачи
    Task updateTask(Task task);

    // Получение задачи по идентификатору
    Task getTaskById(int identifier);

    //Удаление задачи по идентификатору
    Task deleteTaskById(int identifier);

    //Создание нового эпика
    void createNewEpic(Epic epic);

    // Получение списка эпиков
    ArrayList<Epic> getEpics();

    // Удаление всех эпиков, включая их подзадачи
    void deleteAllEpics();

    // Обновление эпика
    Epic updateEpic(Epic epic);

    // Получение эпика по идентификатору
    Epic getEpicById(int identifier);

    // Удаление эпика по идентификатору
    Epic deleteEpicById(int identifier);

    // Получение списка подзадач эпика
    ArrayList<Subtask> getEpicSubtasks(Epic epic);

    // Создание новой подзадачи
    void createNewSubtask(Epic epic, Subtask subtask);

    // Получения списка всех подзадач
    ArrayList<Subtask> getSubtasks();

    // Получение подзадачи по идентификатору
    Subtask getSubtaskById(int identifier);

    // Удаление всех подзадач
    void deleteAllSubtasks();

    // Удаление подзадачи по идентификатору
    Subtask deleteSubtaskById(int identifier);

    // Обновление подзадачи
    Subtask updateSubtask(Subtask subtask);

    // Возвращает историю
    List<Task> getHistory();
}
