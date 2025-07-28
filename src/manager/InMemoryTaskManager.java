package manager;

import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> taskMap;
    private final Map<Integer, Epic> epicMap;
    private final Map<Integer, Subtask> subtaskMap;
    private int generatorId; // Поле для генерации идентификатора
    private final HistoryManager historyManager; // История просмотров задач
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));

    public InMemoryTaskManager() {
        this.taskMap = new HashMap<>();
        this.epicMap = new HashMap<>();
        this.subtaskMap = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.generatorId = 1;
    }

    protected InMemoryTaskManager(Map<Integer, Task> tasks, Map<Integer, Epic> epics,
                                  Map<Integer, Subtask> subtasks, int id) {
        this.taskMap = tasks;
        this.epicMap = epics;
        this.subtaskMap = subtasks;
        this.historyManager = Managers.getDefaultHistory();
        this.generatorId = id;

        List<Task> checkedTasks = tasks.values().stream()
                .filter(task -> !task.getStartTime().equals(LocalDateTime.MIN)).toList();
        List<Subtask> checkedSubtasks = subtasks.values().stream()
                .filter(subtask -> !subtask.getStartTime().equals(LocalDateTime.MIN)).toList();

        this.prioritizedTasks.addAll(checkedTasks);
        this.prioritizedTasks.addAll(checkedSubtasks);
    }

    /*
     *  Задачи
     */

    // Создание новой задачи
    @Override
    public void createNewTask(Task task) {
        if (!task.getStartTime().equals(LocalDateTime.MIN)) {
            boolean hasConflict = prioritizedTasks.stream()
                    .anyMatch(existingTask -> areTasksIntersect(existingTask, task));

            if (hasConflict) {
                throw new IllegalStateException("Задача пересекается по времени с уже существующей задачей.");
            }
        }

        task.setTaskId(getNextId());
        taskMap.put(task.getTaskId(), task);

        if (!task.getStartTime().equals(LocalDateTime.MIN)) {
            prioritizedTasks.add(task);
        }
    }

    // Получение списка задач
    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    // Удаление всех задач
    @Override
    public void deleteAllTasks() {
        taskMap.values().stream()
                .map(Task::getTaskId)
                .forEach(id -> {
                    historyManager.removeFromHistory(id);
                    prioritizedTasks.remove(taskMap.get(id));
                });

        taskMap.clear();
    }

    // Обновление задачи
    @Override
    public Task updateTask(Task task) {
        if (taskMap.containsKey(task.getTaskId())) {
            Task oldTask = taskMap.get(task.getTaskId());

            if (!task.getStartTime().equals(LocalDateTime.MIN)) {
                boolean hasConflict = prioritizedTasks.stream()
                        .anyMatch(existingTask -> areTasksIntersect(existingTask, task));

                if (hasConflict) {
                    prioritizedTasks.add(oldTask);
                    throw new IllegalStateException("Новое время для задачи пересекается с существующей.");
                }

                prioritizedTasks.add(task);
            }

            if (!oldTask.getStartTime().equals(LocalDateTime.MIN)) {
                prioritizedTasks.remove(oldTask);
            }

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
        prioritizedTasks.remove(task);
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
        epicMap.values().forEach(epic -> {
            epic.getSubtasks().stream()
                    .map(Subtask::getTaskId)
                    .forEach(id -> {
                        historyManager.removeFromHistory(id);
                        prioritizedTasks.remove(subtaskMap.get(id));
                    });
            historyManager.removeFromHistory(epic.getTaskId());
        });

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
            epic.updateDurationAndTime();
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
           epic.getSubtasks().stream()
                   .map(Subtask::getTaskId)
                   .forEach(id -> {
                       prioritizedTasks.removeIf(task -> task.getTaskId() == id);
                       subtaskMap.remove(id);
                       historyManager.removeFromHistory(id);
                   });
           historyManager.removeFromHistory(identifier);
       }
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
    public void createNewSubtask(Epic epic, Subtask subtask) {
        if (epic.getTaskId() == subtask.getTaskId()) {
            return;
        }

        if (!subtask.getStartTime().equals(LocalDateTime.MIN)) {
            boolean hasConflict = prioritizedTasks.stream()
                    .anyMatch(existingTask -> areTasksIntersect(existingTask, subtask));

            if (hasConflict) {
                throw new IllegalStateException("Подзадача пересекается по времени с уже существующей подзадачей.");
            }
        }

        subtask.setTaskId(getNextId());
        subtask.setEpicIdentifier(epic.getTaskId());
        subtaskMap.put(subtask.getTaskId(), subtask);
        epic.addSubtask(subtask);

        if (!subtask.getStartTime().equals(LocalDateTime.MIN)) {
            prioritizedTasks.add(subtask);
        }

        updateEpicStatus(epic);
        epic.updateDurationAndTime();
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
        subtaskMap.keySet().forEach(id -> {
            historyManager.removeFromHistory(id);
            prioritizedTasks.removeIf(task -> task.getTaskId() == id);
        });

        subtaskMap.clear();

        epicMap.values().forEach(epic -> {
            epic.getSubtasks().clear();
            updateEpicStatus(epic);
            epic.updateDurationAndTime();
        });
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
                prioritizedTasks.remove(subtask);
                updateEpicStatus(epic);
                epic.updateDurationAndTime();
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

            if (!subtask.getStartTime().equals(LocalDateTime.MIN)) {
                boolean hasConflict = prioritizedTasks.stream()
                        .anyMatch(existingTask -> areTasksIntersect(existingTask, subtask));

                if (hasConflict) {
                    prioritizedTasks.add(currentSubtask);
                    throw new IllegalStateException("Новое время для подзадачи пересекается с существующей.");
                }

                prioritizedTasks.add(subtask);
            }

            if (!currentSubtask.getStartTime().equals(LocalDateTime.MIN)) {
                prioritizedTasks.remove(currentSubtask);
            }

            subtaskMap.put(subtask.getTaskId(), subtask);
            Epic epic = epicMap.get(subtask.getEpicIdentifier());

            if (epic != null) {
                // Замена подзадачи в списке эпика с обновлением статуса
                List<Subtask> subtasks = epic.getSubtasks();
                IntStream.range(0, subtasks.size())
                        .filter(i -> subtasks.get(i).getTaskId() == subtask.getTaskId())
                        .findFirst()
                        .ifPresent(i -> subtasks.set(i, subtask));
                epic.updateDurationAndTime();
            }
            return subtask;
        }
        return null;
    }

    // Получение списка задач и подзадач по приоритету (по времени начала)
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Возвращает историю
    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Проверка двух задач на пересечение по времени
    private <T extends Task> boolean areTasksIntersect(T firstTask, T secondTask) {
        return firstTask.getEndTime().isAfter(secondTask.getStartTime())
                && secondTask.getEndTime().isAfter(firstTask.getStartTime());
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
        List<Subtask> subtasks = epic.getSubtasks();

        boolean isInProgress = subtasks.stream()
                .anyMatch(subtask -> subtask.getTaskStatus() == TaskStatus.IN_PROGRESS);

        if (isInProgress) {
            epic.setTaskStatus(TaskStatus.IN_PROGRESS);
            return;
        }

        boolean isAllDone = subtasks.stream()
                .allMatch(subtask -> subtask.getTaskStatus() == TaskStatus.DONE);

        boolean isAllNew = subtasks.stream()
                .allMatch(subtask -> subtask.getTaskStatus() == TaskStatus.NEW);

        epic.setTaskStatus(isAllDone ? TaskStatus.DONE :
                isAllNew ? TaskStatus.NEW : TaskStatus.IN_PROGRESS);
    }
}
