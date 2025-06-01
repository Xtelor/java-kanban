package tasks;

public class Subtask extends Task {
    private int epicIdentifier; // Идентификатор эпика для подзадач, входящих в него

    public Subtask(String taskName, String taskDescription, TaskStatus taskStatus) {
        super(taskName, taskDescription, taskStatus);
    }

    public Subtask(int taskIdentifier, String taskName, String taskDescription, TaskStatus taskStatus) {
        super(taskIdentifier, taskName, taskDescription, taskStatus);
    }



    // Получение идентификатора эпика для подзадачи
    public int getEpicIdentifier() {
        return epicIdentifier;
    }

    // Установка идентификатора эпика для подзадачи
    public void setEpicIdentifier(int epicIdentifier) {
        if (epicIdentifier == this.getTaskId()) {
            return;
        }
        this.epicIdentifier = epicIdentifier;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "taskIdentifier = " + super.getTaskId() +
                ", taskName = '" + super.getTaskName() + '\'' +
                ", taskDescription = '" + super.getTaskDescription() + '\'' +
                ", taskStatus = " + super.getTaskStatus() +
                ", EpicIdentifier = " + epicIdentifier +
                '}';
    }
}
