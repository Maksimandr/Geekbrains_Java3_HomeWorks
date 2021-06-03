package lesson2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Непосредственно сервер
 */
public class MyServer {

    private List<ClientHandler> clients;
    private AuthService authService;

    public MyServer() {
        try (ServerSocket server = new ServerSocket(ChatConstants.PORT)) {
            authService = new DataBaseAuthService();
            authService.start();
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Сервер ожидает подключения");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    public AuthService getAuthService() {
        return authService;
    }

    public synchronized boolean isClientOnline(String nick) {
        return clients.stream().anyMatch(client -> client.getName().equals(nick));
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClients();
    }

    public synchronized void unsubscribe(ClientHandler clientHandler) {
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
        sendToGroup(message, nicknames);
    }

    /**
     * Мептод отправляет сообщение заданному списку клиентов
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
        List<String> nicknames = Collections.singletonList(name);
        StringBuilder message = new StringBuilder().append("[" + ChatConstants.MSG_FROM_SERVER + "]: ");
        System.out.println(messageFromClient);

        if (splitMessage.size() < 2) {
            message.append("Новое имя не может быть пустым!");
        } else if (authService.isNickExist(splitMessage.get(1))) {
            message.append("<").append(splitMessage.get(1)).append("> этот ник уже используется");
        } else if (authService.changeNick(name, splitMessage.get(1))) {
            clientHandler.setName(splitMessage.get(1));
            message.append("Пользователь с ником <").append(name).append("> изменил ник на <").append(splitMessage.get(1)).append(">");
            nicknames = clients.stream().map(ClientHandler::getName).collect(Collectors.toList());
        }
        sendToGroup(message.toString(), nicknames);
    }
}