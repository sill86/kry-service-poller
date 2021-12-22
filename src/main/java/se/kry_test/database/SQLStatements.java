package se.kry_test.database;

public interface SQLStatements {
    String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS service " +
      "(`url` VARCHAR(128) NOT NULL, " +
      "`name` text NOT NULL, " +
      "`creationDate` VARCHAR(255), " +
      "`status` VARCHAR(255) DEFAULT NULL, " +
      "`userCookie` VARCHAR(255) NOT NULL, " +
      "PRIMARY KEY (url, userCookie))";
    String SQL_INSERT = "INSERT INTO `service` (`url`, `name`, `creationDate`, `status`, `userCookie`) VALUES (?, ?, ?, ?, ?)";
    String SQL_QUERY_ALL = "SELECT * FROM service";
    String SQL_QUERY_ALL_BY_USER = "SELECT * FROM service where `userCookie` = ?";
    String SQL_UPDATE_NAME = "UPDATE `service` SET `name` = ? WHERE `url` = ? AND `userCookie` = ?";
    String SQL_UPDATE_STATUS = "UPDATE `service` SET `status` = ? WHERE `url` = ?";
    String SQL_DELETE = "DELETE FROM `service` WHERE `url` = ? AND `userCookie` = ?";
}
