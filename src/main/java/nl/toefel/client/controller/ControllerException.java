package nl.toefel.client.controller;

public class ControllerException extends RuntimeException {
  public ControllerException(String message) {
    super(message);
  }

  public ControllerException(String message, Throwable cause) {
    super(message, cause);
  }

  public ControllerException(Throwable cause) {
    super(cause);
  }
}
