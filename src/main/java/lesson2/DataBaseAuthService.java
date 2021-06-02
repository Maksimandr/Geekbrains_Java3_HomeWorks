package lesson2;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class DataBaseAuthService implements AuthService {

    private class Entry {
        private final String nick;
        private final String login;
        private final String pass;

        public Entry(String nick, String login, String pass) {
            this.nick = nick;
            this.login = login;
            this.pass = pass;
        }
    }

    private final List<Entry> entries;
    private static final String DATABASE_URL = "jdbc:sqlite:javadb.db";
    private static Connection connection;
    private static Statement statement;

    public DataBaseAuthService() {
        entries = List.of(
                new Entry("nick_1", "l1", "p1"),
                new Entry("nick_2", "l2", "p2"),
                new Entry("nick_3", "l3", "p3"),
                new Entry("nick_4", "l4", "p4")
        );
    }

    public void createTable() {
        String createTable = "create table clients (" +
                "id integer not null primary key, " +
                "nick varchar(30) not null, " +
                "login varchar(30) not null, " +
                "password varchar(30))";
        try {
            statement.execute(createTable);
        } catch (SQLException e) {
            System.out.println("Таблица видимо создана!");
            e.printStackTrace();
        }
    }

    public void insertNewClientPS(List<Entry> entries) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("insert into clients (nick, login, password) values (?, ?, ?)")) {
            for (int i = 1; i <= entries.size(); i++) {
                preparedStatement.setString(1, entries.get(i - 1).nick);
                preparedStatement.setString(2, entries.get(i - 1).login);
                preparedStatement.setString(3, entries.get(i - 1).pass);
                preparedStatement.addBatch();
            }
            int[] ints = preparedStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            statement = connection.createStatement();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        createTable();
        insertNewClientPS(entries);
        System.out.println(this.getClass().getName() + " server started");
    }

    @Override
    public void stop() {
        try {
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println(this.getClass().getName() + " server stopped");
    }

    @Override
    public Optional<String> getNickByLoginAndPass(String login, String pass) {
        String sql = "select nick from clients where login = '" + login + "' and password = '" + pass + "'";
        ResultSet resultSet;
        try {
            resultSet = statement.executeQuery(sql);
            if (!resultSet.next()) {
                return Optional.empty();
            }
            return Optional.ofNullable(resultSet.getString("nick"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
