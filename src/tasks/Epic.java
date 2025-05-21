package tasks;

import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks; // Список подзадач эпика

    public Epic(String taskName, String taskDescription) {
        super(taskName, taskDescription, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    public Epic(int taskIdentifier, String taskName, String taskDescription) {
        super(taskIdentifier, taskName, taskDescription, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
    }

    // Получение списка подзадач эпика
    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    // Добавление подзадачи в список подзадач эпика
    public void addSubtask(Subtask subtask){
        if (subtask != null && subtask.getEpicIdentifier() == this.getTaskId()) {
            subtasks.add(subtask);
        }
    }

    // Получение списка идентификаторов подзадач для эпика
    public ArrayList<Integer> getSubtasksIds() {
        ArrayList<Integer> result = new ArrayList<>();
        for (Subtask subtask : subtasks) {
            result.add(subtask.getTaskId());
        }
        return result;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "taskIdentifier = " + super.getTaskId() +
                ", taskName = '" + super.getTaskName() + '\'' +
                ", taskDescription = '" + super.getTaskDescription() + '\'' +
                ", taskStatus = " + super.getTaskStatus() +
                ", subtasksIds = " + getSubtasksIds() +
                '}';
    }
}
