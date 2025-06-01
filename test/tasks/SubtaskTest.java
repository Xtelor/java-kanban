package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {
    private Subtask subtask;
    @BeforeEach
    public void beforeEach() {
        subtask = new Subtask("Вещи", "Упаковать вещи", TaskStatus.NEW);
        subtask.setEpicIdentifier(2);
    }

    @Test
    public void shouldReturnEpicId() {
        assertEquals(2, subtask.getEpicIdentifier(), "Должен вернуть id эпика");
    }

    @Test
    public void shouldSetEpicId() {
        subtask.setEpicIdentifier(5);
        assertEquals(5, subtask.getEpicIdentifier(), "Должен поменять id эпика");
    }

    @Test
    public void shouldNotSetWhenEqualsTaskId() {
        int epicId = subtask.getEpicIdentifier();
        subtask.setEpicIdentifier(2);
        assertEquals(epicId, subtask.getEpicIdentifier(),
                "Не должен менять id эпика при попытке установить taskId");
    }
}
