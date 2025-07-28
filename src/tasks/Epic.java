package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Epic extends Task {
    private List<Subtask> subtasks; // Список подзадач эпика

    public Epic(String taskName, String taskDescription) {
        super(taskName, taskDescription, TaskStatus.NEW, 0, LocalDateTime.MIN);
        this.subtasks = new ArrayList<>();
    }

    public Epic(int taskIdentifier, String taskName, String taskDescription) {
        super(taskIdentifier, taskName, taskDescription, TaskStatus.NEW, 0, LocalDateTime.MIN);
        this.subtasks = new ArrayList<>();
    }

    // Сохранение подзадач эпика
    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
    }

    @Override // Получение времени начала эпика
    public LocalDateTime getStartTime() {
        return super.getStartTime();
    }

    @Override // Получение длительности выполнения эпика
    public Duration getDuration() {
        return super.getDuration();
    }

    @Override  // Получение времени окончания эпика
    public LocalDateTime getEndTime() {
       return calculateEndTime();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "taskIdentifier = " + super.getTaskId() +
                ", taskName = '" + super.getTaskName() + '\'' +
                ", taskDescription = '" + super.getTaskDescription() + '\'' +
                ", taskStatus = " + super.getTaskStatus() +
                ", subtasksIds = " + getSubtasksIds() +
                ", taskDuration = " + super.getDuration().toMinutes() + " min" +
                ", taskStartTime = " + super.getStartTime() +
                '}';
    }

    // Получение списка подзадач эпика
    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    // Добавление подзадачи в список подзадач эпика
    public void addSubtask(Subtask subtask) {
        if (subtask != null && subtask.getEpicIdentifier() == this.getTaskId()) {
            subtasks.add(subtask);
        }
    }

    // Получение списка идентификаторов подзадач для эпика
    public List<Integer> getSubtasksIds() {
        return subtasks.stream()
                .map(Task::getTaskId)
                .collect(Collectors.toList());
    }

    // Обновление длительности и времени начала эпика
    public void updateDurationAndTime() {
        setDuration(calculateDuration().toMinutes());
        setStartTime(calculateStartTime());
    }

    @Override // Метод для изменения времени начала эпика
    public void setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("Время начала эпика не может быть null.");
        }
        super.setStartTime(startTime);
    }

    @Override // Метод для изменения длительности эпика
    public void setDuration(long duration) {
        if (duration > 0) {
            super.setDuration(duration);
        }
    }

    // Вычисление длительности выполнения эпика - сумма продолжительностей всех подзадач
    private Duration calculateDuration() {
        return subtasks.stream()
                .map(Subtask::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    // Вычисление времени начала эпика - дата старта самой ранней подзадачи
    private LocalDateTime calculateStartTime() {
        return subtasks.stream()
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }

    // Вычисление времени завершения эпика - дата окончания самой поздней подзадачи
    private LocalDateTime calculateEndTime() {
        return subtasks.stream()
                .map(subtask -> subtask.getStartTime().plus(subtask.getDuration()))
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.MIN);
    }
}
