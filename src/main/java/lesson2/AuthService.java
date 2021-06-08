package lesson2;

import java.util.Optional;

/**
 * Сервис авторизации
 */
public interface AuthService {
    /**
     * запустить сервис
     */
    void start();

    /**
     * Остановить сервис
     */
    void stop();

    /**
     * Получить никнейм
     */
    Optional<String> getNickByLoginAndPass(String login, String pass);

    /**
     * Поменять свой ник на другой
     */
    default boolean changeNick(String nick, String newNick) {
        return false;
    }

    /**
     * Проверяет наличие ника в БД
     */
    default boolean isNickExist(String nick) {
        return false;
    }
}