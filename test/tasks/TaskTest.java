package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private Task task;
    private final long duration = 99;
    private final LocalDateTime date = LocalDateTime.of(2025,5,12,14,54);

    @BeforeEach
    void beforeEach() {
        task = new Task(1,"Задача","Описание",TaskStatus.NEW, duration, date);
    }

    @Test // Проверка получения ID задачи
    void shouldReturnTaskId() {
        assertEquals(1, task.getTaskId(), "ID не совпадают");
    }

    @Test // Проверка изменения ID задачи
    void shouldSetTaskId(){
        task.setTaskId(4);
        assertEquals(4, task.getTaskId(), "ID не изменился");
    }

    @Test // Проверка игнорирования попыток установить неверный ID задачи
    void shouldIgnoreTasksWithInvalidId() {
        task.setTaskId(-1);
        task.setTaskId(0); // ID задач в программе начинаются с 1
        assertEquals(1, task.getTaskId(), "Неверный ID установлен");
    }

    @Test // Проверка получения имени
    void shouldReturnName() {
        assertFalse(task.getTaskName().isEmpty(), "Имя не получено");
    }

    @Test // Проверка получения описания
    void shouldReturnDescription() {
        assertFalse(task.getTaskDescription().isEmpty(), "Описание не получено");
    }

    @Test // Проверка получения статуса
    void shouldReturnStatus() {
        assertNotNull(task.getTaskStatus(), "Статус не получен");
    }

    @Test // Проверка получения времени начала задачи
    void shouldReturnCorrectStartTime() {
        assertNotNull(task.getStartTime(), "Время начала задачи не получено");
        assertEquals(date, task.getStartTime(), "Не совпадает время начала задачи" );
    }

    @Test // Проверка получения длительности задачи
    void shouldReturnDuration() {
        assertNotNull(task.getDuration(), "Длительность задачи не получена");
        assertEquals(duration, task.getDuration().toMinutes(), "Не совпадают длительности задачи");
    }

    @Test // Проверка получения времени завершения задачи
    void shouldReturnCorrectEndTime() {
        assertNotNull(task.getEndTime());
        assertEquals(task.getStartTime().plus(task.getDuration()), task.getEndTime(),
                "Времена окончания задачи не совпадают");
    }

    @Test // Проверка возможности изменить длительность задачи
    void shouldChangeDuration() {
        long newDuration = 88;
        task.setDuration(newDuration);
        assertEquals(Duration.ofMinutes(newDuration), task.getDuration(), "Длительность задачи не изменилась");
    }

    @Test // Проверка возможности изменить время начала задачи
    void shouldChangeStartTime() {
        LocalDateTime newStartTime = LocalDateTime.of(2025,7,27,13,57);
        task.setStartTime(newStartTime);

        assertEquals(newStartTime, task.getStartTime(), "Время начала задачи не изменилось");
        assertNotEquals(date, task.getStartTime(), "Время начала задачи не изменилось");
    }

    @Test // Проверка получения времени начала задачи
    void shouldReturnStartTime() {
        assertNotNull(task.getStartTime(), "Время начала задачи не задано");
    }

    @Test // Проверка изменения статуса
    void shouldSetTaskStatus() {
        task.setTaskStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getTaskStatus(), "Статус не изменился");
    }

    @Test // Статус задачи не должен быть null
    void taskStatusShouldNotBeNull() {
        String expected = "Статус задачи не может быть null.";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.setTaskStatus(null),
                "Метод должен выбрасывать IllegalArgumentException при null"

        );
        assertEquals(expected, exception.getMessage());
    }

    @Test // Проверка обработки отрицательной длительности
    void shouldHandleNegativeDuration() {
        long negativeDuration = -300;
        task.setDuration(negativeDuration);
        assertNotEquals(negativeDuration, task.getDuration().toMinutes(),
                "Длительность не может быть отрицательной");
        assertEquals(duration, task.getDuration().toMinutes(),
                "Длительность должна была остаться неизменной");
    }

    @Test // Время начала задачи не должен быть null
    void taskStartTimeShouldNotBeNull() {
        String expected = "Время начала задачи не может быть null.";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> task.setStartTime(null),
                "Метод должен выбрасывать IllegalArgumentException при null"
        );
        assertEquals(expected, exception.getMessage());
    }
}
