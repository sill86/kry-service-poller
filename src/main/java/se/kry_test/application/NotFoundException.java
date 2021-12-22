package se.kry_test.application;

public class NotFoundException extends Exception {

  public NotFoundException() {
  }

  public NotFoundException(final String message) {
    super(message);
  }
}
