package manager;

import java.io.File;

public class Managers {

    private Managers() {

    }

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(new File("tasks.csv"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
