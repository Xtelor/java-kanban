package endpoints;

import com.google.gson.reflect.TypeToken;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public class TypeTokens {

    private TypeTokens() {

    }

    // Вспомогательный класс для задач
    static class TasksListTypeToken extends TypeToken<List<Task>> {

    }

    // Вспомогательный класс для эпиков
    static class EpicsListTypeToken extends TypeToken<List<Epic>> {

    }

    // Вспомогательный класс для подзадач
    static class SubtasksListTypeToken extends TypeToken<List<Subtask>> {

    }
}
