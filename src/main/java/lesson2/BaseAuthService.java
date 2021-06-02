package lesson2;

import java.util.List;
import java.util.Optional;

/**
 * Простейшая реализация сервиса аутентификации, которая работает на встроенном списке
 */
public class BaseAuthService implements AuthService {

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

    public BaseAuthService() {
        entries = List.of(
                new Entry("nick1", "l1", "p1"),
                new Entry("nick2", "l2", "p2"),
                new Entry("nick3", "l3", "p3"),
                new Entry("nick4", "l4", "p4")
        );
    }

    @Override
    public void start() {
        System.out.println(this.getClass().getName() +" server started");
    }

    @Override
    public void stop() {
        System.out.println(this.getClass().getName() +" server stopped");
    }

    @Override
    public Optional<String> getNickByLoginAndPass(String login, String pass) {
        return entries.stream()
                .filter(entry -> entry.login.equals(login) && entry.pass.equals(pass))
                .map(entry -> entry.nick)
                .findFirst();
       /* for (Entry entry : entries) {
            if (entry.login.equals(login) && entry.pass.equals(pass)) {
                return entry.nick;
            }
        }*/
        //return null;
    }
}