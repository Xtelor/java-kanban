import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private Map<Integer, Task> taskMap;
    private Map<Integer, Epic> epicMap;
    private Map<Integer, Subtask> subtaskMap;
    private int generatorId; // Поле для генерации идентификатора

    public TaskManager () {
        this.taskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.generatorId = 1;
    }

    /*
     *  Задачи
     */

    // Создание новой задачи
    public void createNewTask(Task task) {
        task.setTaskId(getNextId());
        taskMap.put(task.getTaskId(), task);
    }

    // Получение списка задач
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    // Удаление всех задач
    public void deleteAllTasks() {
        taskMap.clear();
    }

    // Обновление задачи
    public Task updateTask(Task task) {
        if (taskMap.containsKey(task.getTaskId())) {
            taskMap.put(task.getTaskId(), task);
            return task;
        }
        return null;
    }

    // Получение задачи по идентификатору
    public Task getTaskById(int identifier) {
        return taskMap.get(identifier);
    }

    //Удаление задачи по идентификатору
    public Task deleteTaskById(int identifier) {
        return taskMap.remove(identifier);
    }

    /*
     *  Эпики
     */

    //Создание нового эпика
    public void createNewEpic(Epic epic) {
        epic.setTaskId(getNextId());
        epicMap.put(epic.getTaskId(), epic);
    }

    // Получение списка эпиков
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epicMap.values());
    }

    // Удаление всех эпиков, включая их подзадачи
    public void deleteAllEpics() {
        epicMap.clear();
        subtaskMap.clear();
    }

    // Обновление эпика
    public Epic updateEpic(Epic epic) {
        if (epicMap.containsKey(epic.getTaskId())) {
            Epic currentEpic = epicMap.get(epic.getTaskId());
            epic.setSubtasks(currentEpic.getSubtasks());
            epicMap.put(epic.getTaskId(), epic);
            updateEpicStatus(epic);
            return epic;
        }
        return null;
    }

    // Получение эпика по идентификатору
    public Epic getEpicById(int identifier) {
        return epicMap.get(identifier);
    }

    // Удаление эпика по идентификатору
    public Epic deleteEpicById(int identifier) {
       Epic epic = epicMap.remove(identifier);
       if (epic != null) {
           for (Subtask subtask : epic.getSubtasks()) {
               subtaskMap.remove(subtask.getTaskId());
           }
       }
       return epic;
    }

    // Получение списка подзадач эпика
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtasks();
    }

    /*
     *  Подзадачи
     */

    // Создание новой подзадачи
    public void createNewSubtask(Epic epic, Subtask subtask){
        subtask.setTaskId(getNextId());
        subtask.setEpicIdentifier(epic.getTaskId());
        subtaskMap.put(subtask.getTaskId(), subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
    }

    // Получения списка всех подзадач
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    // Получение подзадачи по идентификатору
    public Subtask getSubtaskById(int identifier) {
        return subtaskMap.get(identifier);
    }

    // Удаление всех подзадач
    public void deleteAllSubtasks() {
        subtaskMap.clear();

        for (Epic epic : epicMap.values()) {
            epic.getSubtasks().clear();
            updateEpicStatus(epic);
        }
    }

    // Удаление подзадачи по идентификатору
    public Subtask deleteSubtaskById(int identifier) {
        Subtask subtask = subtaskMap.remove(identifier);

        if (subtask != null) {
            Epic epic = epicMap.get(subtask.getEpicIdentifier());
            if (epic != null) {
                epic.getSubtasks().remove(subtask);
                updateEpicStatus(epic);
            }
        }

        return subtask;
    }

    // Обновление подзадачи
    public Subtask updateSubtask(Subtask subtask) {
        if (subtaskMap.containsKey(subtask.getTaskId())) {
            Subtask currentSubtask = subtaskMap.get(subtask.getTaskId());
            subtask.setEpicIdentifier(currentSubtask.getEpicIdentifier()); // Сохранение связи подзадачи с эпиком

            subtaskMap.put(subtask.getTaskId(), subtask);
            Epic epic = epicMap.get(subtask.getEpicIdentifier());

            if (epic != null) {
                // Замена подзадачи в списке эпика с обновлением статуса
                List<Subtask> subtasks = epic.getSubtasks();
                for (int i = 0; i < subtasks.size(); i++) {
                    if (subtasks.get(i).getTaskId() == subtask.getTaskId()) {
                        subtasks.set(i, subtask);
                        break;
                    }
                }
                updateEpicStatus(epic);
            }
            return subtask;
        }
        return null;
    }

    // Получение идентификатора для следующей задачи
    private int getNextId() {
        return generatorId++;
    }

    // Обновление статуса эпика
    private void updateEpicStatus(Epic epic) {
        boolean isAllNew = true;
        boolean isAllDone = true;

        for (Subtask subtask : epic.getSubtasks()) {
            TaskStatus status = subtask.getTaskStatus();

            if (status != TaskStatus.NEW) {
                isAllNew = false;
            }

            if (status != TaskStatus.DONE) {
                isAllDone = false;
            }

            if (status == TaskStatus.IN_PROGRESS) {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                return;
            }
        }

        if (isAllDone) {
            epic.setTaskStatus(TaskStatus.DONE);
        } else if (isAllNew) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
