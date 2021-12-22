package se.kry_test.database;

import io.vertx.core.Future;
import se.kry_test.model.Service;

import java.util.List;
import java.util.Map;

public interface ServiceDAO {
  Future<Void> createTableIfNotExist();
  Future<List<Service>> getServices();
  Future<List<Service>> getServicesByUser(String userCookie);
  Future<Void> add(Service service);
  Future<Void> update(Service service);
  Future<Void> updateStatus(Service service);
  Future<Void> updateStatus(Map<String, String> services);
  Future<Void> delete(Service service);
}
