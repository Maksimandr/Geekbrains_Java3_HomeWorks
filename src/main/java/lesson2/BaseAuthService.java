package lesson2;

import model.User;

import java.util.List;
import java.util.Optional;

/**
 * Простейшая реализация сервиса аутентификации, которая работает на встроенном списке
 */
public class BaseAuthService implements AuthService {

    private final List<User> userList;

    public BaseAuthService() {
        userList = User.getUserListTestingSample();
    }

    @Override
    public void start() {
        System.out.println(this.getClass().getName() + " server started");
    }

    @Override
    public void stop() {
        System.out.println(this.getClass().getName() + " server stopped");
    }

    @Override
    public Optional<String> getNickByLoginAndPass(String login, String pass) {
        return userList.stream()
                .filter(entry -> entry.getLogin().equals(login) && entry.getPass().equals(pass))
                .map(User::getNick)
                .findFirst();
    }
}