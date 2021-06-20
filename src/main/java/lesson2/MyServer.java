package lesson2;

import lesson6.HomeWorkApp6;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Непосредственно сервер c управлением потоками через ExecutorService
 */
public class MyServer {

    private volatile List<ClientHandler> clients;
    private AuthService authService;
    private final ExecutorService executorService;
    private static final Logger LOGGER = LogManager.getLogger(MyServer.class);

    public MyServer() {
        executorService = Executors.newCachedThreadPool();
        try (ServerSocket server = new ServerSocket(ChatConstants.PORT)) {
            authService = new DataBaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            // добавил возможность остановить сервер командой из консоли
            executorService.execute(() -> {
                Scanner scanner = new Scanner(System.in);
                String string;
                while (true) {
                    string = scanner.nextLine();
                    if (string.startsWith(ChatConstants.STOP_WORD)) {
                        LOGGER.info("Сервер получил команду на остановку {}", string);
                        broadcastMessage("Сервер остановлен");
                        closeConnections();
                        try {
                            server.close();
                        } catch (IOException e) {
                            LOGGER.error(e);
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            });
            while (true) {
                System.out.println("Сервер ожидает подключения");
                LOGGER.info("Сервер ожидает подключения");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                LOGGER.info("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            LOGGER.error(e);
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
            executorService.shutdown();
        }
    }

    public void closeConnections() {
        for (int i = clients.size() - 1; i >= 0; i--) {
            clients.get(i).closeConnection();
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public synchronized boolean isClientOnline(String nick) {
        return clients.stream().anyMatch(client -> client.getName().equals(nick));
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        LOGGER.info("<{}> авторизовался", clientHandler.getName());
        clients.add(clientHandler);
        broadcastClients();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
        LOGGER.info("<{}> отключился", clientHandler.getName());
        clients.remove(clientHandler);
        broadcastClients();
    }

    /**
     * Отправляет сообщение всем пользователям
     *
     * @param message сообщение
     */
    public synchronized void broadcastMessage(String message) {
        clients.forEach(client -> client.sendMsg(message));
    }

    /**
     * Подготавливает отправку сообщения заданному списку клиентов
     *
     * @param messageFromClient сообщение от клиента (структура "ChatConstants.SEND_TO_LIST nick1 ... nickN MESSAGE")
     * @param name              имя отправителя
     */
    public synchronized void broadcastMessageToClients(String messageFromClient, String name) {
        // разбиваем собщение на части
        List<String> splitMessage = Arrays.asList(messageFromClient.split("\\s+"));
        // пропускаем первое слово (/list) и берем подряд следующие слова из сообщения, пока они соответствуют никам
        List<String> nicknames = splitMessage.stream()
                .skip(1)
                .takeWhile(word -> clients.stream().anyMatch(c -> c.getName().equals(word)))
                .collect(Collectors.toList());
        String message = "[" + name + "]: " +
                splitMessage.stream()
                        .skip(nicknames.size() + 1)
                        .collect(Collectors.joining(" "));
        LOGGER.info("<{}> отправил сообщение для <{}>", name, nicknames);
        sendToGroup(message, nicknames);
    }

    /**
     * Подготавливает отправку персонального сообщения (по сути упрощенная версия метода broadcastMessageToClients)
     *
     * @param messageFromClient сообщение от клиента (структура "ChatConstants.PERSONAL_MSG nick MESSAGE")
     * @param name              имя отправителя
     */
    public synchronized void personalMessage(String messageFromClient, String name) {
        // разбиваем собщение на части
        List<String> splitMessage = Arrays.asList(messageFromClient.split("\\s+"));
        // берем второе слово из сообщения, которое указывает ник получателя
        List<String> nicknames = Collections.singletonList(splitMessage.get(1));
        String message = "[" + name + "]: " +
                splitMessage.stream()
                        .skip(nicknames.size() + 1)
                        .collect(Collectors.joining(" "));
        LOGGER.info("<{}> отправил сообщение для <{}>", name, nicknames);
        sendToGroup(message, nicknames);
    }

    /**
     * Метод отправляет сообщение заданному списку клиентов
     *
     * @param message   сообщение от клиента
     * @param nicknames список получателей сообщения
     */
    public synchronized void sendToGroup(String message, List<String> nicknames) {
        clients.stream()
                .filter(c -> nicknames.contains(c.getName()))
                .forEach(c -> c.sendMsg(message));
    }

    /**
     * выводит в чат список подключенных клиентов
     */
    public synchronized void broadcastClients() {
        String clientsMessage = ChatConstants.CLIENTS_LIST +
                " " +
                clients.stream()
                        .map(ClientHandler::getName)
                        .collect(Collectors.joining(" "));
        clients.forEach(c -> c.sendMsg(clientsMessage));
    }

    /**
     * Меняет ник отправителя на не занятый
     *
     * @param messageFromClient сообщение от клиента
     * @param name              ник отправителя
     * @param clientHandler     указатель на обработчик для отправителя
     */
    public synchronized void changeClientName(String messageFromClient, String name, ClientHandler clientHandler) {
        // разбиваем собщение на части
        List<String> splitMessage = Arrays.asList(messageFromClient.split("\\s+"));
        // изначально в список адресатов пишем только отправителя
        List<String> nicknames = Collections.singletonList(name);
        // делаем заготовку сообщения от сервера
        StringBuilder message = new StringBuilder();

        if (splitMessage.size() < 2) { // если кроме команды /rename ничего нет то это нехорошо
            message.append(ChatConstants.MSG_FROM_SERVER + " ");
            message.append("Новое имя не может быть пустым!");
            LOGGER.info("<{}> пробует поменять имя на пустое", name);
        } else if (authService.isNickExist(splitMessage.get(1))) { // проверяем есть ли в БД ник на который нужно поменять
            message.append(ChatConstants.MSG_FROM_SERVER + " ");
            message.append("<").append(splitMessage.get(1)).append("> этот ник уже используется");
            LOGGER.info("<{}> пробует поменять имя на уже занятое <{}>", name, splitMessage.get(1));
        } else if (authService.changeNick(name, splitMessage.get(1))) { // если удалось поменять ник в БД меняем его в чате и оповещаем об этом всех
            clientHandler.setName(splitMessage.get(1));
            message.append("[" + ChatConstants.MSG_FROM_SERVER + "]: ");
            message.append("Пользователь с ником <").append(name).append("> изменил ник на <").append(splitMessage.get(1)).append(">");
            nicknames = clients.stream().map(ClientHandler::getName).collect(Collectors.toList());
            LOGGER.info("<{}> изменил ник на <{}>", name, splitMessage.get(1));
        }
        sendToGroup(message.toString(), nicknames);
    }
}