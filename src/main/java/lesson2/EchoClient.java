package lesson2;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.io.input.ReversedLinesFileReader;

public class EchoClient extends JFrame {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private File historyFile;
    private final int historyLinesToLoad = 100;
    private BufferedReader fileReader;
    private BufferedWriter fileWriter;

    private JTextArea chatArea;
    private JTextField inputField;

    public EchoClient() {
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        initGUI();
    }

    /**
     * Открываются соединения и поток для получения и обработки сообщений от сервера
     *
     * @throws IOException
     */
    private void openConnection() throws IOException {
        socket = new Socket(ChatConstants.HOST, ChatConstants.PORT);
        inputStream = new DataInputStream(socket.getInputStream());
        outputStream = new DataOutputStream(socket.getOutputStream());


        new Thread(() -> {
            try {
                //auth
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    String[] splitStr = strFromServer.split("\\s+");
                    if (strFromServer.startsWith(ChatConstants.AUTH_OK) && splitStr.length > 1) {
                        // создаем имя для файла истории чата по логину и загружаем историю если она есть
                        historyFile = new File("history_" + splitStr[1] + ".txt");
                        fileReader = new BufferedReader(new FileReader(historyFile));
                        fileWriter = new BufferedWriter(new FileWriter(historyFile, true));
                        for (String str : loadChatHistory(historyLinesToLoad)) {
                            chatArea.append(str);
                            chatArea.append("\n");
                        }
                        break;
                    }
                    chatArea.append(strFromServer);
                    chatArea.append("\n");
                }
                //read from server
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    if (strFromServer.equals(ChatConstants.STOP_WORD)) {
                        break;
                    } else if (strFromServer.startsWith(ChatConstants.CLIENTS_LIST)) {
                        chatArea.append("Сейчас онлайн " + strFromServer);
                    } else {
                        // записываем сообщение в историю в отдельном потоке
                        if (strFromServer.startsWith("[")) {
                            new Thread(() -> {
                                writeToFile(strFromServer);
                            }).start();
                        }
                        chatArea.append(strFromServer);
                    }
                    chatArea.append("\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    /**
     * Записывает сообщение в файл
     *
     * @param string записываемое сообщение
     */
    private synchronized void writeToFile(String string) {
        try {
            fileWriter.write(string);
            fileWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Загружает из файла последние N строк (читается построчно весь файл, что как-то нехорошо если файл будет большой)
     *
     * @param historyLines    количество последних строк для считывания
     * @return список со строками истории чата
     */
    private List<String> loadChatHistory(int historyLines) {
        List<String> stringList = new LinkedList<>();
        try {
            String line;
            while ((line = fileReader.readLine()) != null) {
                if (stringList.size() >= historyLines) {
                    stringList.remove(0);
                }
                stringList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringList;
    }

    /**
     * Попробовал реализовать через RandomAccessFile, но не получается нормально читать русские символы
     *
     * @param historyLines    количество последних строк для считывания
     * @return список со строками истории чата
     */
    private List<String> loadChatHistory2(int historyLines) {
        List<String> arrayList = new ArrayList<>();
        List<String> reversedArrayList = new ArrayList<>();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(historyFile, "r")) {
            int lines = 0;
            long length = randomAccessFile.length();
            for (long pointer = length - 2; pointer >= 0; pointer--) {
                randomAccessFile.seek(pointer);
                int c = randomAccessFile.read();
                if ((char) c == '\n' || pointer == 0) {
                    if (pointer == 0) {
                        randomAccessFile.seek(pointer);
                    }
                    arrayList.add(randomAccessFile.readLine());
                    // readUTF тоже не помогает
//                    String str = randomAccessFile.readUTF();
                    lines++;
                    if (lines == historyLines) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (arrayList.size() > 1) {
            for (int i = arrayList.size() - 1; i >= 0; i--) {
                reversedArrayList.add(arrayList.get(i));
            }
        }
        return reversedArrayList;
    }

    /**
     * Метод загружает из файла последние N строк через ReversedLinesFileReader
     *
     * @param historyLines    количество последних строк для считывания
     * @return список со строками истории чата
     */
    private List<String> loadChatHistory3(int historyLines) {
        List<String> arrayList = new ArrayList<>();
        List<String> reversedArrayList = new ArrayList<>();
        String line;
        int counter = 0;
        try {
            ReversedLinesFileReader reader = new ReversedLinesFileReader(historyFile, StandardCharsets.UTF_8);
            while (counter < historyLines) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                arrayList.add(line);
                counter++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (arrayList.size() > 1) {
            for (int i = arrayList.size() - 1; i >= 0; i--) {
                reversedArrayList.add(arrayList.get(i));
            }
        }
        return reversedArrayList;
    }

    /**
     * Освобождает ресурсы
     */
    public void closeConnection() {
        try {
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    /**
     * Создает окно чата
     */
    public void initGUI() {
        setBounds(600, 300, 500, 500);
        setTitle("Клиент");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //Message area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        //Input panel
        JPanel panel = new JPanel(new BorderLayout());

        //InputField
        inputField = new JTextField();
        panel.add(inputField, BorderLayout.CENTER);

        //SendMessageButton
        JButton sendButton = new JButton("Send");
        panel.add(sendButton, BorderLayout.EAST);

        add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                try {
                    outputStream.writeUTF(ChatConstants.STOP_WORD);
                    closeConnection();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        setVisible(true);
    }

    /**
     * Отсылает сообщение на сервер
     */
    private void sendMessage() {
        if (!inputField.getText().trim().isEmpty()) {
            try {
                outputStream.writeUTF(inputField.getText());
                inputField.setText("");
                inputField.grabFocus();
            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Send error occured");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(EchoClient::new);
    }
}