package se.kry_test.database;

import se.kry_test.application.NotFoundException;
import se.kry_test.model.ServiceStatus;
import se.kry_test.model.ServiceEnum;
import se.kry_test.model.Service;

import io.vertx.ext.sql.SQLConnection;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.core.Promise;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.stream.Collectors;
import java.util.*;

public class ServiceDAOImpl implements ServiceDAO {
  private final SQLClient client;
  private final JsonObject config;

  public ServiceDAOImpl(Vertx vertx) {
    //todo: move in a config.json
    config = new JsonObject()
     .put("url", "jdbc:mysql://localhost:3309/dev?allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true&useSSL=false")
      .put("user", "dev")
      .put("password", "secret")
      .put("driver_class", "com.mysql.jdbc.Driver");

    client = JDBCClient.createShared(vertx, config);
  }

  @Override
  public Future<Void> createTableIfNotExist() {
    return this.connection().compose(connection -> {
      final Promise<Void> promise = Promise.promise();
      connection.query(SQLStatements.CREATE_TABLE, resultSetAsyncResult -> {
        try {
          if (resultSetAsyncResult.failed()) {
            this.closeConnection(connection, promise, resultSetAsyncResult.cause());
            return;
          }
          connection.close();
          promise.complete();
        } catch (final Exception e) {
          this.closeConnection(connection, promise, e);
        }
      });
      return promise.future();
    });
  }

  @Override
  public Future<List<Service>> getServices() {
    return this.connection().compose(connection -> {
      final Promise<List<Service>> promise = Promise.promise();

      connection.query(SQLStatements.SQL_QUERY_ALL, resultSetAsyncResult -> {
        if (resultSetAsyncResult.failed()) {
          this.closeConnection(connection, promise, resultSetAsyncResult.cause());
          return;
        }
        final List<JsonObject> rows = resultSetAsyncResult.result().getRows();
        if (rows == null) {
          promise.complete(Collections.emptyList());
          return;
        }
        try {
          final List<Service> services = rows
            .stream()
            .map(row ->
              new Service(row.getString(ServiceEnum.URL.label()),
                row.getString(ServiceEnum.NAME.label()),
                row.getString(ServiceEnum.CREATIONDATE.label()),
                row.getString(ServiceEnum.STATUS.label()),
                row.getString(ServiceEnum.USERCOOKIE.label())))
            .collect(Collectors.toList());
          connection.close();
          promise.complete(services);
        } catch (final Exception e) {
          this.closeConnection(connection, promise, e);
        }
      });
      return promise.future();
    });
  }

  @Override
  public Future<List<Service>> getServicesByUser(String userCookie) {
    return this.connection().compose(connection -> {
      final Promise<List<Service>> promise = Promise.promise();
      JsonArray jsonArray = new JsonArray().add(userCookie);
      connection.queryWithParams(SQLStatements.SQL_QUERY_ALL_BY_USER, jsonArray, resultSetAsyncResult -> {
        if (resultSetAsyncResult.failed()) {
          this.closeConnection(connection, promise, resultSetAsyncResult.cause());
          return;
        }
        final List<JsonObject> rows = resultSetAsyncResult.result().getRows();
        if (rows == null) {
          promise.complete(Collections.emptyList());
          return;
        }
        try {
          final List<Service> services = rows
            .stream()
            .map(row ->
              new Service(row.getString(ServiceEnum.URL.label()),
                row.getString(ServiceEnum.NAME.label()),
                row.getString(ServiceEnum.CREATIONDATE.label()),
                row.getString(ServiceEnum.STATUS.label()),
                row.getString(ServiceEnum.USERCOOKIE.label()))).collect(Collectors.toList());
          connection.close();
          promise.complete(services);
        } catch (final Exception e) {
          this.closeConnection(connection, promise, e);
        }
      });
      return promise.future();
    });
  }

  @Override
  public Future<Void> add(Service service) {
    service.setCreationDate(new Date().toString());
    JsonArray jsonArray = new JsonArray()
      .add(service.getUrl())
      .add(service.getName())
      .add(service.getCreationDate())
      .add(ServiceStatus.UNKNOWN.label())
      .add(service.getUserCookie());

    return query(SQLStatements.SQL_INSERT, jsonArray);
  }

  @Override
  public Future<Void> update(Service service) {
    JsonArray jsonArray = new JsonArray().add(service.getName()).add(service.getUrl()).add(service.getUserCookie());
    return query(SQLStatements.SQL_UPDATE_NAME, jsonArray);
  }

  @Override
  public Future<Void> updateStatus(Service service) {
    JsonArray jsonArray = new JsonArray().add(service.getStatus()).add(service.getUrl());
    return query(SQLStatements.SQL_UPDATE_STATUS, jsonArray);
  }

  @Override
  public Future<Void> updateStatus(Map<String, String> services) {
    return this.connection().compose(connection -> {
      final Promise<Void> promise = Promise.promise();
      List<JsonArray> batch = new ArrayList<>();

      services.entrySet().stream().forEach(e -> {
        batch.add(new JsonArray().add(e.getValue()).add(e.getKey()));
      });

      connection.batchWithParams(SQLStatements.SQL_UPDATE_STATUS, batch, result -> {
        try {
          if (result.failed()) {
            this.closeConnection(connection, promise, result.cause());
            return;
          }
          connection.close();
          promise.complete();
        } catch (final Exception e) {
          this.closeConnection(connection, promise, e);
        }
      });
      return promise.future();
    });
  }

  @Override
  public Future<Void> delete(Service service) {
    JsonArray jsonArray = new JsonArray().add(service.getUrl()).add(service.getUserCookie());
    return query(SQLStatements.SQL_DELETE, jsonArray);
  }

  private Future<SQLConnection> connection() {
    final Promise<SQLConnection> promise = Promise.promise();
    this.client.getConnection(connectionAR -> {
      if (connectionAR.failed()) {
        promise.fail(connectionAR.cause());
        return;
      }
      promise.complete(connectionAR.result());
    });
    return promise.future();
  }

  private void closeConnection(final SQLConnection connection, final Promise promise, final Throwable e2) {
    connection.close();
    promise.fail(e2);
  }

  private Future<Void> query(String sqlStatement, JsonArray jsonArray) {
    return this.connection().compose(connection -> {
      final Promise<Void> promise = Promise.promise();
      connection.updateWithParams(sqlStatement, jsonArray, result -> {
        try {
          if (result.failed()) {
            this.closeConnection(connection, promise, result.cause());
            return;
          }
          if (!sqlStatement.equals(SQLStatements.SQL_INSERT) && result.result().getUpdated() == 0) {
            this.closeConnection(connection, promise, new NotFoundException("Url not found"));
            return;
          }
          connection.close();
          promise.complete();
        } catch (final Exception e) {
          this.closeConnection(connection, promise, e);
        }
      });
      return promise.future();
    });
  }

}
