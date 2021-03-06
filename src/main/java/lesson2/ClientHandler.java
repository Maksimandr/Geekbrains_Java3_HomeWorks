package lesson2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    private String name;

    public ClientHandler(MyServer server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.name = "";
            server.getExecutorService().execute(() -> {
                try {
                    authentication();
                    readMessages();
                } catch (IOException e) {
                    LOGGER.error(e);
                    e.printStackTrace();
                } finally {
                    server.broadcastMessage(name + " вышел из чата");
                    closeConnection();
                }
            });
            // поток закрывает соединение если вышел таймаут на подписку клиента
            server.getExecutorService().execute(() -> {
                long timeOut = System.currentTimeMillis();
                while (true) {
                    try {
                        Thread.sleep(5000);
                        //каждые 5 сек. проверяем авторизовался ли клиент, если да завершаем поток
                        if (!name.isEmpty()) {
                            break;
                        } else if (System.currentTimeMillis() - timeOut > ChatConstants.CLIENT_AUTH_TIMEOUT) {
                            //если клиент не авторизовался и прошло больше 120 сек. закрываем соединение и завершаем поток
                            LOGGER.info("Вышел таймаут для авторизации");
                            closeConnection();
                            break;
                        }
                    } catch (InterruptedException e) {
                        LOGGER.error(e);
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            LOGGER.error(e);
            System.out.println("Проблема при создании клиента");
        }
    }

    /**
     * Обрабатывает сообщение от пользователя
     */
    private void readMessages() throws IOException {
        while (true) {
            String messageFromClient = inputStream.readUTF();
            System.out.println("от " + name + ": " + messageFromClient);
            if (messageFromClient.equals(ChatConstants.STOP_WORD)) {
                LOGGER.info("<{}> отправил команду {}", name, ChatConstants.STOP_WORD);
                return;
            } else if (messageFromClient.startsWith(ChatConstants.SEND_TO_LIST)) {
                LOGGER.info("<{}> отправил команду {}", name, ChatConstants.SEND_TO_LIST);
                server.broadcastMessageToClients(messageFromClient, name);
            } else if (messageFromClient.startsWith(ChatConstants.PERSONAL_MSG)) {
                LOGGER.info("<{}> отправил команду {}", name, ChatConstants.PERSONAL_MSG);
                server.personalMessage(messageFromClient, name);
            } else if (messageFromClient.startsWith(ChatConstants.CLIENTS_LIST)) {
                LOGGER.info("<{}> отправил команду {}", name, ChatConstants.CLIENTS_LIST);
                server.broadcastClients();
            } else if (messageFromClient.startsWith(ChatConstants.CHANGE_NAME)) {
                LOGGER.info("<{}> отправил команду {}", name, ChatConstants.CHANGE_NAME);
                server.changeClientName(messageFromClient, name, this);
            } else {
                LOGGER.info("<{}> отправил сообщение в чат", name);
                server.broadcastMessage("[" + name + "]: " + messageFromClient);
            }
        }
    }

    /**
     * Авторизует пользователя на сервере
     */
    private void authentication() throws IOException {
        while (true) {
            String message = inputStream.readUTF();
            if (message.startsWith(ChatConstants.AUTH_COMMAND)) {
                String[] splitStr = message.split("\\s+");
                if (!(splitStr.length < 3)) {
                    Optional<String> nick = server.getAuthService().getNickByLoginAndPass(splitStr[1], splitStr[2]);
                    if (nick.isPresent()) {
                        //проверим, что такого уже нет онлайн
                        if (!server.isClientOnline(nick.get())) {
                            sendMsg(ChatConstants.AUTH_OK + " " + splitStr[1]);
                            name = nick.get();
                            server.subscribe(this);
                            server.broadcastMessage(name + " вошел в чат");
                            return;
                        } else {
                            LOGGER.warn("<{}> уже авторизован", nick.get());
                            sendMsg("Такой ник уже авторизован");
                        }
                    }
                }
                LOGGER.info("Неудачная попытка авторизации");
                sendMsg("Неверные логин/пароль");
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
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        server.unsubscribe(this);
        try {
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}