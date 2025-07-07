package tasks;

import java.util.Objects;

public class Task {
    private int taskIdentifier;
    private final String taskName;
    private final String taskDescription;
    private TaskStatus taskStatus;

    public Task(String taskName, String taskDescription, TaskStatus taskStatus) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
    }

    public Task(int taskIdentifier, String taskName, String taskDescription, TaskStatus taskStatus) {
        this.taskIdentifier = taskIdentifier;
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
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
        if (taskStatus != null) {
            this.taskStatus = taskStatus;
        }
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskIdentifier = " + taskIdentifier +
                ", taskName = '" + taskName + '\'' +
                ", taskDescription = '" + taskDescription + '\'' +
                ", taskStatus = " + taskStatus +
                '}';
    }
}
