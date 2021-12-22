package se.kry_test.model;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;
import java.util.Objects;

public class Service implements Serializable {

  private String url;
  private String name;
  private String creationDate;
  private String status;
  private String userCookie;

  public Service() {
  }

  public Service(String url, String name, String creationDate, String status, String userCookie) {
    this.url = url;
    this.name = name;
    this.creationDate = creationDate;
    this.status = status;
    this.userCookie = userCookie;
  }

  public JsonObject toJson() {
    JsonObject service = new JsonObject()
      .put("url", this.url)
      .put("name", this.name)
      .put("creationDate", this.creationDate)
      .put("status", this.status)
      .put("userCookie", this.userCookie);

    JsonObject retVal = new JsonObject()
      .put("service", service);

    return retVal;
  }

  public String getUrl() {
    return url;
  }

  public String getName() {
    return name;
  }

  public String getCreationDate() {
    return creationDate;
  }

  public String getStatus() {
    return status;
  }

  public String getUserCookie() {
    return userCookie;
  }

  public void setCreationDate(String creationDate) {
    this.creationDate = creationDate;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public void setUserCookie(String userCookie) {
    this.userCookie = userCookie;
  }

  @Override
  public int hashCode() {
    return Objects.hash(url)+Objects.hash(userCookie);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;

    final Service that = (Service) obj;

    return Objects.equals(url, that.url) && Objects.equals(userCookie, that.userCookie);
  }

  @Override
  public String toString() {
    return "Service {" +
      "url=" + url +
      ", name=" + name +
      ", creationDate=" + creationDate +
      ", status=" + status +
      ", userCookie=" + userCookie +
      '}';
  }
}
