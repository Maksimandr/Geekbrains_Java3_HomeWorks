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

    /**
     * В конструкторе задаем список клиентов для тестирования
     */
    public DataBaseAuthService() {
        entries = List.of(
                new Entry("nick_1", "l1", "p1"),
                new Entry("nick_2", "l2", "p2"),
                new Entry("nick_3", "l3", "p3"),
                new Entry("nick_4", "l4", "p4")
        );
    }

    /**
     * Создает таблицу с указанными параметрами
     */
    public void createTable() {
        String createTable = "create table if not exists clients (" +
                "id integer not null primary key autoincrement, " +
                "nick varchar(30) not null unique, " +
                "login varchar(30) not null, " +
                "password varchar(30) not null)";
        try {
            statement.execute(createTable);
        } catch (SQLException e) {
            System.out.println("Таблица видимо создана!");
            e.printStackTrace();
        }
    }

    /**
     * Метод записывает в базу список клиентов
     *
     * @param entries список клиентов
     */
    public void insertNewClientPS(List<Entry> entries) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("insert into clients (nick, login, password) values (?, ?, ?)")) {
            for (int i = 1; i <= entries.size(); i++) {
                preparedStatement.setString(1, entries.get(i - 1).nick);
                preparedStatement.setString(2, entries.get(i - 1).login);
                preparedStatement.setString(3, entries.get(i - 1).pass);
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Запуск сервиса
     */
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

    /**
     * Закрытие сервиса
     */
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

    /**
     * Возвращает ник по заданной паре логин/пароль
     *
     * @param login логин
     * @param pass  пароль
     * @return ник в обёртке Optional<String>
     */
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

    /**
     * Меняет у клиента ник на новый
     *
     * @param nick    текущий ник
     * @param newNick новый ник
     * @return true если замена произошла, false если нет
     */
    @Override
    public boolean changeNick(String nick, String newNick) {
        String updateNick = "update clients set nick = '" + newNick + "' where nick = '" + nick + "'";
        try {
            int rowCount = statement.executeUpdate(updateNick);
            // если число обновленных строк больше чем ноль, значит обновили
            if (rowCount > 0) {
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Не удалось поменять ник!");
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Проверяет наличие ника в БД
     *
     * @param nick ник
     * @return true если ник существует, false если нет
     */
    @Override
    public boolean isNickExist(String nick) {
        String sql = "select nick from clients where nick = '" + nick + "'";
        try {
            ResultSet resultSet = statement.executeQuery(sql);
            // если resultSet не пустой значит такой ник уже есть в БД
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
