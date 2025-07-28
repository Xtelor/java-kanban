package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;
    private Subtask firstSubtask;
    private Subtask secondSubtask;

    @BeforeEach
    void beforeEach() {
        epic = new Epic("Эпик","Проверка");
        firstSubtask = new Subtask("Подзадача 1", "Описание 1", TaskStatus.NEW,
                12, LocalDateTime.of(2025,7,13,14,44));
        secondSubtask = new Subtask("Подзадача 2", "Описание 2", TaskStatus.IN_PROGRESS,
                21, LocalDateTime.of(2025,5,12,14,54));
    }

    @Test
    void shouldHandleNullStartTime() {
        String expected = "Время начала эпика не может быть null.";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> epic.setStartTime(null),
                "Метод должен выбрасывать IllegalArgumentException при null"
        );

        assertEquals(expected, exception.getMessage());
    }

    @Test // Проверка обновления длительности и даты начала эпика
    void shouldUpdateDurationAndDate() {
        epic.addSubtask(firstSubtask);
        epic.updateDurationAndTime();

        long duration = epic.getDuration().toMinutes();
        LocalDateTime date = epic.getStartTime();

        assertNotNull(date, "Дата не получена");
        assertNotEquals(0, duration, "Длительность не получена");

        epic.addSubtask(secondSubtask);
        epic.updateDurationAndTime();

        long newDuration = epic.getDuration().toMinutes();
        LocalDateTime newDate = epic.getStartTime();

        assertNotNull(newDate, "Дата не получена");
        assertNotEquals(0, newDuration, "Длительность не получена");

        assertNotEquals(duration, newDuration, "Длительности не должны совпадать");
        assertNotEquals(date, newDate, "Дата не изменилась");
    }

    @Test // Проверка длительности эпика без подзадач
    void shouldHandleZeroDuration() {
        assertNotNull(epic.getDuration(), "Длительность эпика без подзадач не получена");
        assertEquals(0, epic.getDuration().toMinutes(),
                "Длительность эпика без подзадач должна быть равна нулю");
    }

    @Test // Проверка отсутствия времени начала эпика при отсутствии подзадач
    void shouldHandleZeroStartTime() {
        assertEquals(LocalDateTime.MIN, epic.getStartTime(),
                "У эпика без подзадач не должно быть времени начала");
    }

    @Test // Проверка отсутствия времени конца эпика при отсутствии подзадач
    void shouldHandleZeroEndTime() {
        assertEquals(LocalDateTime.MIN, epic.getEndTime(),
                "У эпика без подзадач не должно быть времени окончания");
    }

    @Test // Проверка отсутствия времени начала эпика при отсутствии подзадач после обновления
    void shouldHandleZeroStartTimeAfterUpdate() {
        epic.updateDurationAndTime();
        assertEquals(LocalDateTime.MIN, epic.getStartTime(),
                "У эпика без подзадач не должно быть времени начала");
    }

    @Test // Проверка отсутствия времени конца эпика при отсутствии подзадач после обновления
    void shouldHandleZeroEndTimeAfterUpdate() {
        epic.updateDurationAndTime();
        assertEquals(LocalDateTime.MIN, epic.getEndTime(),
                "У эпика без подзадач не должно быть времени окончания");
    }

    @Test // Проверка получения правильного времени начала эпика
    void shouldReturnCorrectStartTime() {
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        epic.updateDurationAndTime();

        assertNotNull(epic.getStartTime(), "Время начала эпика не получено");
        assertEquals(secondSubtask.getStartTime(), epic.getStartTime(), "Неверное время начала эпика");
    }

    @Test // Проверка получения правильной длительности эпика
    void shouldReturnCorrectDuration() {
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        epic.updateDurationAndTime();

        assertNotNull(epic.getDuration(), "Длительность эпика не получена");
        assertEquals(firstSubtask.getDuration().plus(secondSubtask.getDuration()), epic.getDuration(),
                "Длительность эпика должна быть равна длительности всех его подзадач");
    }

    @Test // Проверка получения правильного времени завершения эпика
    void shouldReturnCorrectEndTime() {
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        epic.updateDurationAndTime();

        assertNotNull(epic.getEndTime(), "Время окончания эпика не получено");
        assertEquals(firstSubtask.getEndTime(), epic.getEndTime(), "Неверное время окончания эпика");
    }

    @Test
    void getSubtasksShouldReturnList() {
        epic.addSubtask(firstSubtask);
        List<Subtask> list = epic.getSubtasks();

        assertEquals(1, list.size(), "В списке должна быть одна подзадача");
        assertEquals(firstSubtask, list.getFirst(), "Подзадачи должны совпадать");
    }

    @Test
    void setSubtaskShouldReplaceSubtask() {
        ArrayList<Subtask> list = new ArrayList<>();
        list.add(firstSubtask);
        epic.setSubtasks(list);

        assertEquals(1, epic.getSubtasks().size(), "Размер списка не обновился");
        assertEquals(firstSubtask, epic.getSubtasks().getFirst(), "Подзадачи не совпадают");
    }

    @Test
    void setSubtasksShouldHandleNull() {
        epic.setSubtasks(null);
        assertNotNull(epic.getSubtasks(), "При Null должен создаваться пустой список");
        assertTrue(epic.getSubtasks().isEmpty(), "Список должен быть пустым");
    }

    @Test
    void getSubtasksIdsShouldReturnCorrectIds() {
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        List<Integer> list = epic.getSubtasksIds();

        assertEquals(2, list.size(), "Должно быть 2 ID");
        assertTrue(list.contains(firstSubtask.getTaskId()) && list.contains(secondSubtask.getTaskId()),
                "ID подзадач должны совпадать");
    }

    @Test
    void getSubtasksIdsShouldReturnEmptyListIfNoSubtasks() {
        List<Integer> list = epic.getSubtasksIds();
        assertTrue(list.isEmpty(), "Для эпика без подзадач должен возвращаться пустой список");
    }

    @Test // Проверка обработки отрицательной длительности
    void shouldHandleNegativeDuration() {
        long negativeDuration = -300;
        epic.setDuration(negativeDuration);
        assertNotEquals(negativeDuration, epic.getDuration().toMinutes(),
                "Длительность не может быть отрицательной");
        assertEquals(0, epic.getDuration().toMinutes(),
                "Длительность должна была остаться неизменной");
    }
}
