package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskMap;
    private final Map<Integer, Epic> epicMap;
    private final Map<Integer, Subtask> subtaskMap;
    private int generatorId; // Поле для генерации идентификатора
    private final HistoryManager historyManager; // История просмотров задач

    public InMemoryTaskManager() {
        this.taskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.generatorId = 1;
    }

    /*
     *  Задачи
     */

    // Создание новой задачи
    @Override
    public void createNewTask(Task task) {
        task.setTaskId(getNextId());
        taskMap.put(task.getTaskId(), task);
    }

    // Получение списка задач
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    // Удаление всех задач
    @Override
    public void deleteAllTasks() {
        for (Task task : taskMap.values()) {
            historyManager.removeFromHistory(task.getTaskId());
        }
        taskMap.clear();
    }

    // Обновление задачи
    @Override
    public Task updateTask(Task task) {
        if (taskMap.containsKey(task.getTaskId())) {
            taskMap.put(task.getTaskId(), task);
            return task;
        }
        return null;
    }

    // Получение задачи по идентификатору
    @Override
    public Task getTaskById(int identifier) {
        Task task = taskMap.get(identifier);
        addInHistory(task);
        return task;
    }

    //Удаление задачи по идентификатору
    @Override
    public Task deleteTaskById(int identifier) {
        Task task = taskMap.remove(identifier);
        historyManager.removeFromHistory(identifier);
        return task;
    }

    /*
     *  Эпики
     */

    //Создание нового эпика
    @Override
    public void createNewEpic(Epic epic) {
        epic.setTaskId(getNextId());
        epicMap.put(epic.getTaskId(), epic);
    }

    // Получение списка эпиков
    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epicMap.values());
    }

    // Удаление всех эпиков, включая их подзадачи
    @Override
    public void deleteAllEpics() {
        for (Epic epic : epicMap.values()) {
            for (Subtask subtask : epic.getSubtasks()) {
                historyManager.removeFromHistory(subtask.getTaskId());
            }
            historyManager.removeFromHistory(epic.getTaskId());
        }

        subtaskMap.clear();
        epicMap.clear();
    }

    // Обновление эпика
    @Override
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
    @Override
    public Epic getEpicById(int identifier) {
        Epic epic = epicMap.get(identifier);
        addInHistory(epic);
        return epic;
    }

    // Удаление эпика по идентификатору
    @Override
    public Epic deleteEpicById(int identifier) {
       Epic epic = epicMap.remove(identifier);
       if (epic != null) {
           for (Subtask subtask : epic.getSubtasks()) {
               subtaskMap.remove(subtask.getTaskId());
               historyManager.removeFromHistory(subtask.getTaskId());
           }
       }
       historyManager.removeFromHistory(identifier);
       return epic;
    }

    // Получение списка подзадач эпика
    @Override
    public List<Subtask> getEpicSubtasks(Epic epic) {
        return epic.getSubtasks();
    }

    /*
     *  Подзадачи
     */

    // Создание новой подзадачи
    @Override
    public void createNewSubtask(Epic epic, Subtask subtask){
        if (epic.getTaskId() == subtask.getTaskId()) {
            return;
        }
        subtask.setTaskId(getNextId());
        subtask.setEpicIdentifier(epic.getTaskId());
        subtaskMap.put(subtask.getTaskId(), subtask);
        epic.addSubtask(subtask);
        updateEpicStatus(epic);
    }

    // Получения списка всех подзадач
    @Override
    public List<Subtask> getSubtasks() {
        return new ArrayList<>(subtaskMap.values());
    }

    // Получение подзадачи по идентификатору
    @Override
    public Subtask getSubtaskById(int identifier) {
        Subtask subtask = subtaskMap.get(identifier);
        addInHistory(subtask);
        return subtask;
    }

    // Удаление всех подзадач
    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtaskMap.values()) {
            historyManager.removeFromHistory(subtask.getTaskId());
        }
        subtaskMap.clear();

        for (Epic epic : epicMap.values()) {
            for (Subtask subtask : epic.getSubtasks()) {
                historyManager.removeFromHistory(subtask.getTaskId());
            }
            epic.getSubtasks().clear();
            updateEpicStatus(epic);
        }
    }

    // Удаление подзадачи по идентификатору
    @Override
    public Subtask deleteSubtaskById(int identifier) {
        Subtask subtask = subtaskMap.remove(identifier);

        if (subtask != null) {
            Epic epic = epicMap.get(subtask.getEpicIdentifier());
            if (epic != null) {
                historyManager.removeFromHistory(subtask.getTaskId());
                epic.getSubtasks().remove(subtask);
                updateEpicStatus(epic);
            }
        }
        historyManager.removeFromHistory(identifier);
        return subtask;
    }

    // Обновление подзадачи
    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask.getEpicIdentifier() == subtask.getTaskId()) {
            return null;
        }

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

    // Возвращает историю
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Добавление задачи в историю
    private void addInHistory(Task task) {
        historyManager.addInHistory(task);
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
