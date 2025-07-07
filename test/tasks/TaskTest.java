package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {
    private Task task;

    @BeforeEach
    void beforeEach() {
        task = new Task(1,"Задача","Описание",TaskStatus.NEW);
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

    @Test // Проверка изменения статуса
    void shouldSetTaskStatus() {
        task.setTaskStatus(TaskStatus.DONE);
        assertEquals(TaskStatus.DONE, task.getTaskStatus(), "Статус не изменился");
    }

    @Test // Статус задачи не должен быть null
    void taskStatusShouldNotBeNull() {
        task.setTaskStatus(null);
        assertEquals(TaskStatus.NEW, task.getTaskStatus(), "Статус изменился на null");
    }
}
