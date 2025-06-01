package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private Task task;

    @BeforeEach
    public void beforeEach() {
        task = new Task(1,"Задача","Описание",TaskStatus.NEW);
    }

    @Test // Проверка получения ID задачи
    public void shouldReturnTaskId() {
        assertEquals(1, task.getTaskId(), "ID не совпадают");
    }

    @Test // Проверка изменения ID задачи
    public void shouldSetTaskId(){
        task.setTaskId(4);
        assertEquals(4, task.getTaskId(), "ID не изменился");
    }

    @Test // Проверка получения имени
    public void shouldReturnName() {
        assertFalse(task.getTaskName().isEmpty(), "Имя не получено");
    }

    @Test // Проверка получения описания
    public void shouldReturnDescription() {
        assertFalse(task.getTaskDescription().isEmpty(), "Описание не получено");
    }

    @Test // Проверка получения статуса
    public void shouldReturnStatus() {
        assertNotNull(task.getTaskStatus(), "Статус не получен");
    }

    @Test // Проверка изменения статуса
    public void shouldSetTaskStatus() {
        task.setTaskStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getTaskStatus(), "Статус не изменился");
    }
}
