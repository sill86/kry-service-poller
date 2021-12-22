package se.kry_test.application;

public enum ServiceEnum {

  URL("url"),
  NAME("name"),
  STATUS("status"),
  CREATIONDATE("creationDate"),
  USERCOOKIE("userCookie"),
  STATUS_FAIL("FAIL"),
  STATUS_OK("OK"),
  STATUS_UNKNOWN("UNKNOWN"),
  HTTP_PORT("http_port"),
  COOKIE("kryClientCookie");

  private final String label;

  ServiceEnum(final String label) {
    this.label = label;
  }

  public String label() {
    return this.label;
  }
}
