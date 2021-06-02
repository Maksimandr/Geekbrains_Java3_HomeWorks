package lesson2;

import java.sql.*;

/**
 * код с урока проверять не надо
 */
public class DatabaseApp {

    private static final String DATABASE_URL = "jdbc:sqlite:javadb.db";
    private static Connection connection;
    private static Statement statement;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException {
        DatabaseApp databaseApp = new DatabaseApp();
        databaseApp.createTable();
        databaseApp.insertNewBike("asdsdf", "123456");
    }

    public void createTable() throws SQLException {
        String createTable = "create table bike (" +
                "id integer not null primary key, " +
                "model varchar(30) not null, " +
                "serial_no varchar(10))";
        statement.execute(createTable);
    }

    public void insertNewBike(String model, String serial) throws SQLException {
        String insertSql = "insert into bike (model, serial_no) values ('" + model + "', '" + serial + "')";
        statement.execute(insertSql);
    }
}
