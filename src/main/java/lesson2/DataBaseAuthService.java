package lesson2;

import model.User;

import java.sql.*;
import java.util.List;
import java.util.Optional;


public class DataBaseAuthService implements AuthService {

    private final List<User> userList;
    private static final String DATABASE_URL = "jdbc:sqlite:javadb.db";
    private static Connection connection;
    private static Statement statement;

    /**
     * В конструкторе задаем список клиентов для тестирования
     */
    public DataBaseAuthService() {
        userList = User.getUserListTestingSample();
    }

    /**
     * Создает таблицу с указанными параметрами
     */
    public void createTable() {
        String createTable = "create table if not exists clients (" +
                "id integer not null primary key autoincrement, " +
                "nick varchar(30) not null unique, " +
                "login varchar(30) not null, " +
                "password varchar(30))";
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
     * @param users список клиентов
     */
    public void insertNewClientPS(List<User> users) {
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("insert into clients (nick, login, password) values (?, ?, ?)")) {
            for (int i = 1; i <= users.size(); i++) {
                preparedStatement.setString(1, users.get(i - 1).getNick());
                preparedStatement.setString(2, users.get(i - 1).getLogin());
                preparedStatement.setString(3, users.get(i - 1).getPass());
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
        insertNewClientPS(userList);
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
        System.out.println(this.getClass().getName() + " service stopped");
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
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select nick from clients where login = ? and password = ?")) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, pass);
            ResultSet resultSet = preparedStatement.executeQuery();
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
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("update clients set nick = ? where nick = ?")) {
            preparedStatement.setString(1, newNick);
            preparedStatement.setString(2, nick);
            int rowCount = preparedStatement.executeUpdate();
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
        try (PreparedStatement preparedStatement =
                     connection.prepareStatement("select nick from clients where nick = ?")) {
            preparedStatement.setString(1, nick);
            ResultSet resultSet = preparedStatement.executeQuery();
            // если resultSet не пустой значит такой ник уже есть в БД
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
