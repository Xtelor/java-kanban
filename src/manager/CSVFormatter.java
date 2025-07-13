package manager;

import tasks.*;

public class CSVFormatter {

    private CSVFormatter() {

    }

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }

    public static <T extends Task> String toString(T task) {
        if (task == null) {
            throw new IllegalArgumentException("На вход метода toString передан null");
        }

        StringBuilder result = new StringBuilder();

        result.append(task.getTaskId()).append(",");

        if (task instanceof Subtask) {
            result.append(TaskType.SUBTASK).append(",");
        } else if (task instanceof Epic) {
            result.append(TaskType.EPIC).append(",");
        } else {
            result.append(TaskType.TASK).append(",");
        }

        result.append(task.getTaskName()).append(",");
        result.append(task.getTaskStatus()).append(",");

        result.append(task.getTaskDescription()).append(",");

        if (task instanceof Subtask) {
            result.append(((Subtask) task).getEpicIdentifier());
        }

        return result.toString();
    }

    public static <T extends Task> T fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Строка данных не может быть пустой или null");
        }

        String[] fields = value.split(",");

        if (fields.length < 5) {
            throw new IllegalArgumentException("Неверный формат строки данных");
        }


        int id = Integer.parseInt(fields[0]); // ID задачи
        TaskType taskType = TaskType.valueOf(fields[1].toUpperCase()); // Тип задачи
        String taskName = fields[2]; // Имя задачи
        TaskStatus taskStatus = TaskStatus.valueOf(fields[3].toUpperCase()); // Статус задачи
        String taskDescription = fields[4]; // Описание задачи

        switch (taskType) {
            case SUBTASK-> {
                Subtask subtask = new Subtask(id, taskName, taskDescription, taskStatus);
                // ID эпика подзадачи
                if (fields.length >= 6 && !fields[5].isBlank()) {
                    subtask.setEpicIdentifier(Integer.parseInt(fields[5]));
                }

                return (T) subtask;
            }
            case EPIC -> {
                Epic epic = new Epic(id, taskName, taskDescription);
                epic.setTaskStatus(taskStatus);
                return (T) epic;
            }
            case TASK -> {
                return (T) new Task(id, taskName, taskDescription, taskStatus);
            }
            default -> {
                throw new IllegalArgumentException("Неизвестный тип задачи: " + taskType);
            }
        }
    }

}
