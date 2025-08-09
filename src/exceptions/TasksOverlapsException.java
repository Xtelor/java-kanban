package exceptions;

public class TasksOverlapsException extends RuntimeException {
  public TasksOverlapsException(final String message) {
    super(message);
  }
}
