package manager;


import tasks.Task;

import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    Map<Integer, Node> nodes;

    // Указатель на первый элемент списка
    private Node head;

    // Указатель на последний элемент списка
    private Node tail;

    public InMemoryHistoryManager() {
        nodes = new HashMap<>();
    }

    // Добавление узла в конец списка
    public void addLast(Task element) {
        final Node oldTail = tail;
        final Node newNode = new Node(tail, element, null);

        tail = newNode;

        if (oldTail == null) {
            head = newNode;
        } else {
            oldTail.next = newNode;
        }

    }

    // Возвращает историю
    public List<Task> getHistory() {
        ArrayList<Task> tasks = new ArrayList<>();
        Node node = head;

        while (node != null) {
            tasks.add(node.data);
            node = node.next;
        }

        return tasks;
    }

    // Добавление задачи в историю
    public void addInHistory(Task task) {
        if (task == null || task.getTaskId() <= 0) return;

        removeFromHistory(task.getTaskId());

        addLast(task);
        nodes.put(task.getTaskId(), tail);
    }

    @Override // Удаление из истории
    public void removeFromHistory(int id) {
        Node node = nodes.get(id);

        if (node != null) {
            removeNode(node);
            nodes.remove(id);
        }
    }

    // Удаление узла связного списка
    private void removeNode(Node node) {

        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        node.prev = null;
        node.next = null;
    }

    private static class Node {
        private Task data;
        private Node next;
        private Node prev;

        public Node(Node prev, Task data, Node next) {
            this.data = data;
            this.prev = prev;
            this.next = next;
        }
    }
}
