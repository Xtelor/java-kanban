package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    private int taskIdentifier;
    private final String taskName;
    private final String taskDescription;
    private TaskStatus taskStatus;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String taskName, String taskDescription, TaskStatus taskStatus,
                long duration, LocalDateTime startTime) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.duration = Duration.ofMinutes(duration);
        this.startTime = startTime;
    }

    public Task(int taskIdentifier, String taskName, String taskDescription, TaskStatus taskStatus,
                long duration, LocalDateTime startTime) {
        this.taskIdentifier = taskIdentifier;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.duration = Duration.ofMinutes(duration);
        this.startTime = startTime;
    }

    @Override
    public final boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Task task = (Task) object;
        return taskIdentifier == task.taskIdentifier;
    }

    @Override
    public final int hashCode() {
        return Objects.hashCode(taskIdentifier);
    }

    // Получение времени начала задачи
    public LocalDateTime getStartTime() {
        return startTime;
    }

    // Получение длительности выполнения задачи
    public Duration getDuration() {
        return duration;
    }

    // Получение времени окончания задачи
    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    // Получение идентификатора задачи
    public int getTaskId() {
        return taskIdentifier;
    }

    // Установка идентификатора задачи
    public void setTaskId(int taskIdentifier) {
        if (taskIdentifier > 0) {
            this.taskIdentifier = taskIdentifier;
        }
    }

    // Получение имени задачи
    public String getTaskName() {
        return taskName;
    }

    // Получение описания задачи
    public String getTaskDescription() {
        return taskDescription;
    }

    // Получение статуса задачи
    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    // Установка статуса задачи
    public void setTaskStatus(TaskStatus taskStatus) {
        if (taskStatus == null) {
            throw new IllegalArgumentException("Статус задачи не может быть null.");
        }
        this.taskStatus = taskStatus;
    }

    // Обновление времени начала задачи
    protected void setStartTime(LocalDateTime startTime) {
        if (startTime == null) {
           throw new IllegalArgumentException("Время начала задачи не может быть null.");
        }
        this.startTime = startTime;
    }

    // Обновление длительности задачи
    protected void setDuration(long duration) {
        if (duration > 0) {
            this.duration = Duration.ofMinutes(duration);
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskIdentifier = " + taskIdentifier +
                ", taskName = '" + taskName + '\'' +
                ", taskDescription = '" + taskDescription + '\'' +
                ", taskStatus = " + taskStatus +
                ", taskDuration = " + duration.toMinutes() + " min" +
                ", taskStartTime = " + startTime +
                '}';
    }
}
