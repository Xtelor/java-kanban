package manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import tasks.*;

import static org.junit.jupiter.api.Assertions.*;

public class CSVFormatterTest {

    private Task task;
    private Epic epic;
    private Subtask subtask;

    @BeforeEach
    void beforeEach() {
        task = new Task(1,"Task1", "Task-1-info", TaskStatus.NEW);
        epic = new Epic(2,"Epic1", "Epic-1-info");
        subtask = new Subtask(3,"Subtask1", "Subtask-1-info", TaskStatus.DONE);
        subtask.setEpicIdentifier(epic.getTaskId());
        epic.setTaskStatus(subtask.getTaskStatus());
    }

    @Test // Проверка корректности вывода заголовка CSV-файла
    void shouldReturnHeader() {
        String result = CSVFormatter.getHeader();
        String expected = "id,type,name,status,description,epic";

        assertNotNull(result, "Заголовок CSV-файла пуст");
        assertEquals(expected, CSVFormatter.getHeader(), "Неверный заголовок");
    }

    @Test // Проверка правильности строкового представления задачи
    void shouldReturnCorrectStringForTask() {
        String result = CSVFormatter.toString(task);
        String expected = "1,TASK,Task1,NEW,Task-1-info,";

        assertEquals(expected, result, "Неверное строковое представление задачи");
    }

    @Test // Проверка правильности строкового представления эпика
    void shouldReturnCorrectStringForEpic() {
        String result = CSVFormatter.toString(epic);
        String expected = "2,EPIC,Epic1,DONE,Epic-1-info,";

        assertEquals(expected, result, "Неверное строковое представление эпика");
    }

    @Test // Проверка правильности строкового представления подзадачи
    void shouldReturnCorrectStringForSubtask() {
        String result = CSVFormatter.toString(subtask);
        String expected = "3,SUBTASK,Subtask1,DONE,Subtask-1-info,2";

        assertEquals(expected, result, "Неверное строковое представление подзадачи");
    }

    @Test // Проверка обработки методом toString аргумента null
    void toString_shouldHandleNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormatter.toString(null),
                "Метод должен выбрасывать IllegalArgumentException при null"
        );

        assertEquals("На вход метода toString передан null", exception.getMessage());
    }

    @Test // Проверка правильности создания задачи из строки
    void shouldCreateTaskFromString() {
        String inputString = "1,TASK,Task1,NEW,Task-1-info,";
        Task task = CSVFormatter.fromString(inputString);

        assertNotNull(task, "Задача не создана");
        assertFalse(task instanceof Epic, "Создавалась задача, а не эпик");
        assertFalse(task instanceof Subtask, "Создавалась задача, а не подзадача");

        assertEquals(1, task.getTaskId(), "Неверный id задачи");
        assertEquals("Task1", task.getTaskName(), "Неверное название задачи");
        assertEquals(TaskStatus.NEW, task.getTaskStatus(), "Неверный статус задачи");
        assertEquals("Task-1-info", task.getTaskDescription(), "Неверное описание задачи");
    }

    @Test // Проверка правильности создания эпика из строки
    void shouldCreateEpicFromString() {
        String inputString = "2,EPIC,Epic1,DONE,Epic-1-info,";
        Epic epic = CSVFormatter.fromString(inputString);

        assertNotNull(epic, "Эпик не создан");

        assertEquals(2, epic.getTaskId(), "Неверный id эпика");
        assertEquals("Epic1", epic.getTaskName(), "Неверное название эпика");
        assertEquals(TaskStatus.DONE, epic.getTaskStatus(), "Неверный статус эпика");
        assertEquals("Epic-1-info", epic.getTaskDescription(), "Неверное описание эпика");
    }

    @Test // Проверка правильности создания подзадачи из строки
    void shouldCreateSubtaskFromString() {
        String inputString = "3,SUBTASK,Subtask1,DONE,Subtask-1-info,2";
        Subtask subtask = CSVFormatter.fromString(inputString);

        assertNotNull(subtask, "Подзадача не создана");

        assertEquals(3, subtask.getTaskId(), "Неверный id подзадачи");
        assertEquals("Subtask1", subtask.getTaskName(), "Неверное название подзадачи");
        assertEquals(TaskStatus.DONE, subtask.getTaskStatus(), "Неверный статус подзадачи");
        assertEquals("Subtask-1-info", subtask.getTaskDescription(), "Неверное описание подзадачи");
        assertEquals(2, subtask.getEpicIdentifier(), "Неверный id эпика проверяемой подзадачи");
    }

    @Test // Проверка обработки методом fromString пустой строки или null
    void fromString_shouldHandleNullOrBlankString() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormatter.fromString(null),
                "Метод должен выбрасывать IllegalArgumentException при null или пустой строке"
        );
        assertEquals("Строка данных не может быть пустой или null", exception.getMessage());
    }

    @Test // Проверка обработки методом fromString строки данных неверного формата
    void shouldHandleIncorrectString() {
        String inputString = "3,SUBTASK,Subtask1,DONE,";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> CSVFormatter.fromString(inputString),
                "Метод должен выбрасывать IllegalArgumentException при строке неверного формата"
        );
        
        assertEquals("Неверный формат строки данных", exception.getMessage());
    }
}
