package manager;

import tasks.*;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private FileBackedTaskManager(Map<Integer, Task> tasks, Map<Integer, Epic> epics,
                                  Map<Integer, Subtask> subtasks, int id, File file) {
        super(tasks, epics, subtasks, id);
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        int maxId = -1;
        // Наполнение данными из файла
        try {
            String resString = Files.readString(file.toPath());

            if (resString.isEmpty()) {
                return new FileBackedTaskManager(file);
            }

            Map<Integer, Task> tasks = new HashMap<>();
            Map<Integer, Epic> epics = new HashMap<>();
            Map<Integer, Subtask> subtasks = new HashMap<>();

            String[] lines = resString.split(System.lineSeparator());

            // Сначала создаем все задачи, сохраняя оригинальные ID
            for (int i = 1; i < lines.length; i++) {
                Task task = CSVFormatter.fromString(lines[i]);
                maxId = Math.max(task.getTaskId(), maxId);

                if (task instanceof Epic epic) {
                    epics.put(epic.getTaskId(), epic);
                } else if (task instanceof Subtask subtask) {
                    subtasks.put(subtask.getTaskId(), subtask);
                } else {
                    tasks.put(task.getTaskId(), task);
                }
            }

            // Второй проход: добавляем подзадачи
            for (Subtask subtask : subtasks.values()) {
                int epicId = subtask.getEpicIdentifier();
                Epic epic = epics.get(epicId);
                epic.addSubtask(subtask);
            }

            return new FileBackedTaskManager(tasks, epics, subtasks, maxId + 1, file);

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            throw new UncheckedIOException(e);
        }

    }

    // Задачи

    @Override
    public void createNewTask(Task task) {
        super.createNewTask(task);
        save();
    }

    @Override
    public void deleteAllTasks(){
        super.deleteAllTasks();
        save();
    }

    @Override
    public Task updateTask(Task task) {
        Task result = super.updateTask(task);
        save();
        return result;
    }

    @Override
    public Task deleteTaskById(int id) {
        Task result = super.deleteTaskById(id);
        save();
        return result;
    }

    // Эпики

    @Override
    public void createNewEpic(Epic epic) {
        super.createNewEpic(epic);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic result = super.updateEpic(epic);
        save();
        return result;
    }

    @Override
    public Epic deleteEpicById(int id) {
        Epic result = super.deleteEpicById(id);
        save();
        return result;
    }

    // Подзадачи

    @Override
    public void createNewSubtask(Epic epic, Subtask subtask) {
        super.createNewSubtask(epic, subtask);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask result = super.updateSubtask(subtask);
        save();
        return result;
    }

    @Override
    public Subtask deleteSubtaskById(int id) {
        Subtask result = super.deleteSubtaskById(id);
        save();
        return result;
    }

    // Сохранение изменений в файл
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            writer.write(CSVFormatter.getHeader()); // заголовок
            writer.newLine();

            for (Task task : getTasks()) {
                writer.write(CSVFormatter.toString(task));
                writer.newLine();
            }

            for (Epic epic : getEpics()) {
                writer.write(CSVFormatter.toString(epic));
                writer.newLine();
            }

            for (Subtask subtask : getSubtasks()) {
                writer.write(CSVFormatter.toString(subtask));
                writer.newLine();
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл " + file.getName(), e);
        }
    }
}
