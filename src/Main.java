import manager.Managers;
import manager.TaskManager;
import tasks.Epic;
import tasks.Subtask;
import tasks.Task;
import tasks.TaskStatus;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        //Создание задач
        Task task = new Task("Магазин", "Купить продукты", TaskStatus.NEW);
        Task anotherTask = new Task("Обучение", "Прочитать лекции", TaskStatus.NEW);
        taskManager.createNewTask(task);
        taskManager.createNewTask(anotherTask);

        //Создание эпиков
        Epic epic = new Epic("Переезд", "Подготовиться к переезду");
        Epic anotherEpic = new Epic("Магазин", "Купить продукты на неделю");
        taskManager.createNewEpic(epic);
        taskManager.createNewEpic(anotherEpic);

        //Создание подзадач
        Subtask firstSubtask = new Subtask("Вещи", "Упаковать вещи", TaskStatus.NEW);
        Subtask secondSubtask = new Subtask("Мясо", "Купить говядину", TaskStatus.NEW);
        Subtask thirdSubtask = new Subtask("Овощи", "Купить картошку", TaskStatus.NEW);
        taskManager.createNewSubtask(epic, firstSubtask);
        taskManager.createNewSubtask(anotherEpic, secondSubtask);
        taskManager.createNewSubtask(anotherEpic, thirdSubtask);

        //Получение списков всех задач, эпиков и подзадач, а также списков всех подзадач для эпиков
        System.out.println("Все задачи: " + taskManager.getTasks());
        System.out.println("Все эпики: " + taskManager.getEpics());
        System.out.println("Все подзадачи" + taskManager.getSubtasks() + "\n");
        System.out.println("Все подзадачи первого эпика: " + taskManager.getEpicSubtasks(epic));
        System.out.println("Все подзадачи первого эпика: " + taskManager.getEpicSubtasks(anotherEpic));

        //Получение по идентификатору
        System.out.println("Получение задачи по идентификатору: " + taskManager.getTaskById(task.getTaskId()));
        System.out.println("Получение эпика по идентификатору: " + taskManager.getEpicById(epic.getTaskId()));
        System.out.println("Получение подзадачи по идентификатору: " + taskManager.getSubtaskById(firstSubtask.getTaskId()) + "\n");

        //Обновление задач, подзадач, эпиков
        Task updatedTask = new Task(task.getTaskId(), "Магазин", "Купить продукты на завтра", TaskStatus.DONE);
        Task anotherUpdatedTask = new Task(anotherTask.getTaskId(), "Дистанционное обучение", "Выполнить ДЗ", TaskStatus.IN_PROGRESS);
        System.out.println("Обновлена задача: " + taskManager.updateTask(updatedTask));
        System.out.println("Обновлена задача: " + taskManager.updateTask(anotherUpdatedTask));

        Epic updatedEpic = new Epic(epic.getTaskId(), "Переезд", "Закончить подготовку");
        System.out.println("Обновлен эпик: " + taskManager.updateEpic(updatedEpic));

        Subtask updatedFirstSubtask = new Subtask(firstSubtask.getTaskId(), "Вещи", "Упаковать вещи", TaskStatus.DONE);
        Subtask updatedSecondSubtask = new Subtask(secondSubtask.getTaskId(), "Мясо", "Купить говядину", TaskStatus.DONE);
        Subtask updatedThirdSubtask = new Subtask(thirdSubtask.getTaskId(), "Овощи", "Купить картошку", TaskStatus.IN_PROGRESS);
        System.out.println("Обновлена подзадача: " + taskManager.updateSubtask(updatedFirstSubtask));
        System.out.println("Обновлена подзадача: " + taskManager.updateSubtask(updatedSecondSubtask));
        System.out.println("Обновлена подзадача: " + taskManager.updateSubtask(updatedThirdSubtask) + "\n");
        System.out.println("Задачи после обновления: " + taskManager.getTasks());
        System.out.println("Подзадачи после обновления: " + taskManager.getSubtasks());
        System.out.println("Эпики после обновления: " + taskManager.getEpics() + "\n");

        //Удаление по идентификатору
        System.out.println("Удалена задача: " + taskManager.deleteTaskById(task.getTaskId()));
        System.out.println("Удален эпик: " + taskManager.deleteEpicById(epic.getTaskId()));
        System.out.println("Удалена подзадача: " + taskManager.deleteSubtaskById(thirdSubtask.getTaskId()) + "\n");
        System.out.println("Все задачи после удаления: " + taskManager.getTasks());
        System.out.println("Все эпики после удаления: " + taskManager.getEpics());
        System.out.println("Все подзадачи после удаления: " + taskManager.getSubtasks() + "\n");

        //Удаление всех задач, подзадач и эпиков
        System.out.println("Удаление абсолютно всех задач.");
        taskManager.deleteAllSubtasks();
        taskManager.deleteAllEpics();
        taskManager.deleteAllTasks();
        System.out.println("Оставшиеся задачи после удаления: " + taskManager.getTasks());
        System.out.println("Оставшиеся эпики после удаления: " + taskManager.getEpics());
        System.out.println("Оставшиеся задачи после удаления: " + taskManager.getSubtasks());

        System.out.println("\nИстория: ");
        System.out.println(taskManager.getHistory());
    }
}
