package tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {
    private Epic epic;
    private Subtask firstSubtask;
    private Subtask secondSubtask;

    @BeforeEach
    public void beforeEach() {
        epic = new Epic("Эпик","Проверка");
        firstSubtask = new Subtask("Подзадача 1", "Описание 1", TaskStatus.NEW);
        secondSubtask = new Subtask("Подзадача 2", "Описание 2", TaskStatus.IN_PROGRESS);
    }

    @Test
    public void getSubtasksShouldReturnList() {
        epic.addSubtask(firstSubtask);
        List<Subtask> list = epic.getSubtasks();

        assertEquals(1, list.size(), "В списке должна быть одна подзадача");
        assertEquals(firstSubtask, list.getFirst(), "Подзадачи должны совпадать");
    }

    @Test
    public void setSubtaskShouldReplaceSubtask() {
        ArrayList<Subtask> list = new ArrayList<>();
        list.add(firstSubtask);
        epic.setSubtasks(list);

        assertEquals(1, epic.getSubtasks().size(), "Размер списка не обновился");
        assertEquals(firstSubtask, epic.getSubtasks().getFirst(), "Подзадачи не совпадают");
    }

    @Test
    public void setSubtasksShouldHandleNull() {
        epic.setSubtasks(null);
        assertNotNull(epic.getSubtasks(), "При Null должен создаваться пустой список");
        assertTrue(epic.getSubtasks().isEmpty(), "Список должен быть пустым");
    }

    @Test
    public void getSubtasksIdsShouldReturnCorrectIds() {
        epic.addSubtask(firstSubtask);
        epic.addSubtask(secondSubtask);
        List<Integer> list = epic.getSubtasksIds();

        assertEquals(2, list.size(), "Должно быть 2 ID");
        assertTrue(list.contains(firstSubtask.getTaskId()) && list.contains(secondSubtask.getTaskId()), "ID подзадач должны совпадать");
    }

    @Test
    public void getSubtasksIdsShouldReturnEmptyListIfNoSubtasks() {
        List<Integer> list = epic.getSubtasksIds();
        assertTrue(list.isEmpty(), "Для эпика без подзадач должен возвращаться пустой список");
    }

}
