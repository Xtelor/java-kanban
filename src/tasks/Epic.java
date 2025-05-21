import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks ;

    public Epic(String taskName, String taskDescription) {
        super(taskName, taskDescription, TaskStatus.NEW);
        this.subtasks = new ArrayList<>();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }


}
