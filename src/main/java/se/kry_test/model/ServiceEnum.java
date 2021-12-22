package se.kry_test.model;

public enum ServiceEnum {

  URL("url"),
  NAME("name"),
  STATUS("status"),
  CREATIONDATE("creationDate"),
  USERCOOKIE("userCookie"),
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
