package lesson2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

/**
 * Обслуживает клиента (отвечает за связь между клиентом и сервером)
 */
public class ClientHandler {

    private MyServer server;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private String name;

    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            new Thread(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    closeConnection();
                }
            }).start();
            // поток закрывает соединение если вышел таймаут на подписку клиента
            new Thread(() -> {
                long timeOut = System.currentTimeMillis();
                while (true) {
                    try {
                        Thread.sleep(5000);
                        //каждые 5 сек. проверяем авторизовался ли клиент, если да завершаем поток
                        if (!name.isEmpty()) {
                            break;
                        } else if (System.currentTimeMillis() - timeOut > ChatConstants.CLIENT_AUTH_TIMEOUT) {
                            //если клиент не авторизовался и прошло больше 120 сек. закрываем соединение и завершаем поток
                            closeConnection();
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException ex) {
            System.out.println("Проблема при создании клиента");
        }
    }

    /**
     * Обрабатывает сообщение отпользователя
     *
     * @throws IOException
     */
    private void readMessages() throws IOException {
        while (true) {
            String messageFromClient = inputStream.readUTF();
            System.out.println("от " + name + ": " + messageFromClient);
            if (messageFromClient.equals(ChatConstants.STOP_WORD)) {
                return;
            } else if (messageFromClient.startsWith(ChatConstants.SEND_TO_LIST)) {
                server.broadcastMessageToClients(messageFromClient, name);
            } else if (messageFromClient.startsWith(ChatConstants.PERSONAL_MSG)) {
                server.personalMessage(messageFromClient, name);
            } else if (messageFromClient.startsWith(ChatConstants.CLIENTS_LIST)) {
                server.broadcastClients();
            } else if (messageFromClient.startsWith(ChatConstants.CHANGE_NAME)) {
                server.changeClientName(messageFromClient, name, this);
            } else {
                server.broadcastMessage("[" + name + "]: " + messageFromClient);
            }
        }
    }

    /**
     * Авторизует пользователя на сервере
     *
     * @throws IOException
     */
    private void authentication() throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(ChatConstants.AUTH_COMMAND)) {
                String[] parts = message.split("\\s+");
                Optional<String> nick = server.getAuthService().getNickByLoginAndPass(parts[1], parts[2]);
                if (nick.isPresent()) {
                    //проверим, что такого уже нет онлайн
                    if (!server.isClientOnline(nick.get())) {
                        sendMsg(ChatConstants.AUTH_OK + " " + nick);
                        name = nick.get();
                        server.subscribe(this);
                        server.broadcastMessage(name + " вошел в чат");
                        return;
                    } else {
                        sendMsg("Такой ник уже авторизован");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");
                }
            }
        }
    }

    /**
     * Отправляет сообщение на сервер
     *
     * @param message сообщение
     */
    public void sendMsg(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        server.unsubscribe(this);
        server.broadcastMessage(name + " вышел из чата");
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String setName(String name) {
        this.name = name;
        return this.name;
    }
}