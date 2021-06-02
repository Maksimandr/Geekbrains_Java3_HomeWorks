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

    public synchronized boolean isNickBusy(String nick) {
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
     * @param message
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
        // пропускаем первое слово (/list) и берем подряд следующие слова из сообщения, пока они соответствуют никам (дальше остается само сообщение)
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
        // берем второе слово из сообщения, которое указывает ник получателя (дальше остается само сообщение)
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
     * @param message   сообщение, разбитое на части
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

    public synchronized void changeClientName(String messageFromClient, String name) {
        // разбиваем собщение на части
        List<String> splitMessage = Arrays.asList(messageFromClient.split("\\s+"));
        System.out.println(messageFromClient);
        // берем второе слово из сообщения, которое указывает на новый ник клиента
        List<String> newName = Collections.singletonList(splitMessage.get(1));
        List<String> nicknames = new ArrayList<>();
        nicknames.add(name);

        if (newName.isEmpty()) {
            sendToGroup("[" + ChatConstants.MSG_FROM_SERVER + "]: " + "Новое имя не может быть пустым!", nicknames);
        } else if (isNickBusy(newName.get(0))) {
            String message = "[" + ChatConstants.MSG_FROM_SERVER + "]: " +
                    splitMessage.stream()
                            .skip(1)
                            .collect(Collectors.joining(" ")) + " - такой ник уже используется";
            personalMessage(message, ChatConstants.MSG_FROM_SERVER);
        }
        clients.stream()
                .filter(c -> c.getName().equals(name))
                .map(c -> c.setName(newName.get(0)));
        if (clients.stream().filter(c -> c.getName().equals(newName)).collect(Collectors.toList()).isEmpty()) {

        }
    }
}