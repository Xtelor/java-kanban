package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private Subtask subtask;
    private final long duration = 99;
    private final LocalDateTime date = LocalDateTime.of(2025,5,12,14,54);

    @BeforeEach
    void beforeEach() {
        subtask = new Subtask("Вещи", "Упаковать вещи", TaskStatus.NEW, duration, date);
        subtask.setEpicIdentifier(2);
    }

    @Test
    void shouldReturnEpicId() {
        assertEquals(2, subtask.getEpicIdentifier(), "Должен вернуть id эпика");
    }

    @Test
    void shouldSetEpicId() {
        subtask.setEpicIdentifier(5);
        assertEquals(5, subtask.getEpicIdentifier(), "Должен поменяться id эпика");
    }

    @Test // Проверка получения времени начала подзадачи
    void shouldReturnCorrectStartTime() {
        assertNotNull(subtask.getStartTime(), "Время начала подзадачи не получено");
        assertEquals(date, subtask.getStartTime(), "Не совпадает время начала подзадачи");
    }

    @Test // Проверка получения длительности подзадачи
    void shouldReturnCorrectDuration() {
        assertNotNull(subtask.getDuration(), "Длительность подзадачи не получена");
        assertEquals(duration, subtask.getDuration().toMinutes(), "Не совпадает длительность подзадачи");
    }

    @Test
    void shouldReturnCorrectEndTime() {
        assertNotNull(subtask.getEndTime(), "Время окончания подзадачи не получено");
        assertEquals(subtask.getStartTime().plus(subtask.getDuration()), subtask.getEndTime(),
                "Не совпадает время окончания подзадачи");
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
        assertEquals(duration, subtask.getDuration().toMinutes());
        assertEquals(date, subtask.getStartTime());
    }

    @Test // Проверка заполнения всех полей через конструктор
    void shouldCreateSubtaskWithAllFields() {
        LocalDateTime newDate = date.plusDays(1);
        long newDuration = duration - 33;

        Subtask subtask = new Subtask(4, "Тест", "Описание",
                TaskStatus.NEW, 66, newDate);

        assertEquals(4, subtask.getTaskId());

        assertEquals("Тест", subtask.getTaskName());
        assertEquals("Описание", subtask.getTaskDescription());
        assertEquals(TaskStatus.NEW, subtask.getTaskStatus());

        assertEquals(0, subtask.getEpicIdentifier());
        assertEquals(newDuration, subtask.getDuration().toMinutes());
        assertEquals(newDate, subtask.getStartTime());
    }

    @Test // Проверка возможности изменить длительность подзадачи
    void shouldChangeDuration() {
        long newDuration = 88;
        subtask.setDuration(newDuration);
        assertEquals(Duration.ofMinutes(newDuration), subtask.getDuration(),
                "Длительность подзадачи не изменилась");
    }

    @Test // Проверка возможности изменить длительность подзадачи
    void shouldChangeStartTime() {
        LocalDateTime newStartTime = LocalDateTime.of(2025,7,27,13,57);
        subtask.setStartTime(newStartTime);
        assertEquals(newStartTime, subtask.getStartTime(), "Время начала подзадачи не изменилось");
    }

    @Test // Проверка обработки отрицательной длительности
    void shouldHandleNegativeDuration() {
        long negativeDuration = -300;
        subtask.setDuration(negativeDuration);
        assertNotEquals(negativeDuration, subtask.getDuration().toMinutes(),
                "Длительность не может быть отрицательной");
        assertEquals(duration, subtask.getDuration().toMinutes(),
                "Длительность должна была остаться неизменной");
    }
}
