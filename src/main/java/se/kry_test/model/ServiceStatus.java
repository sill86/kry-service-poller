package se.kry_test.model;

public enum ServiceStatus {
  FAIL("FAIL"),
  OK("OK"),
  UNKNOWN("UNKNOWN");

  private final String label;

  ServiceStatus(final String label) {
    this.label = label;
  }

  public String label() {
    return this.label;
  }
}
