package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubtaskTest {
    private Subtask subtask;
    @BeforeEach
    void beforeEach() {
        subtask = new Subtask("Вещи", "Упаковать вещи", TaskStatus.NEW);
        subtask.setEpicIdentifier(2);
    }

    @Test
    void shouldReturnEpicId() {
        assertEquals(2, subtask.getEpicIdentifier(), "Должен вернуть id эпика");
    }

    @Test
    void shouldSetEpicId() {
        subtask.setEpicIdentifier(5);
        assertEquals(5, subtask.getEpicIdentifier(), "Должен поменять id эпика");
    }

    @Test
    void shouldNotSetWhenEqualsTaskId() {
        int epicId = subtask.getEpicIdentifier();
        subtask.setEpicIdentifier(2);
        assertEquals(epicId, subtask.getEpicIdentifier(),
                "Не должен менять id эпика при попытке установить taskId");
    }

    @Test // Проверка заполнения всех полей через конструктор
    void shouldCreateSubtaskWithDefaultFields() {
        assertNotNull(subtask.getTaskName());
        assertNotNull(subtask.getTaskDescription());
        assertNotNull(subtask.getTaskStatus());

        assertEquals(2, subtask.getEpicIdentifier());
    }

    @Test // Проверка заполнения всех полей через конструктор
    void shouldCreateSubtaskWithAllFields() {
        Subtask subtask = new Subtask(4, "Тест", "Описание", TaskStatus.NEW);

        assertEquals(4, subtask.getTaskId());

        assertEquals("Тест", subtask.getTaskName());
        assertEquals("Описание", subtask.getTaskDescription());
        assertEquals(TaskStatus.NEW, subtask.getTaskStatus());

        assertEquals(0, subtask.getEpicIdentifier());
    }
}
