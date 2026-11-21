package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class DBConnection {

    public static Connection getConnection() throws SQLException {

        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream("db.properties")) {
            properties.load(input);

        } catch (IOException e) {
            throw new SQLException("Cannot load db.properties", e);
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }
}
