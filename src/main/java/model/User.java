package model;

import java.util.List;

public class User {

    private final String nick;
    private final String login;
    private final String pass;

    public User(String nick, String login, String pass) {
        this.nick = nick;
        this.login = login;
        this.pass = pass;
    }

    public static List<User> getUserListTestingSample() {
        return List.of(
                new User("nick_1", "l1", "p1"),
                new User("nick_2", "l2", "p2"),
                new User("nick_3", "l3", "p3"),
                new User("nick_4", "l4", "p4")
        );
    }

    public String getNick() {
        return nick;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }
}