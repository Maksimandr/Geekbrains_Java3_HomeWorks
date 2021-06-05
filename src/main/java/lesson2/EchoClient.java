package lesson2;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class EchoClient extends JFrame {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private String historyFileName;

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
                        historyFileName = "history_" + splitStr[1] + ".txt";
                        break;
                    }
                    chatArea.append(strFromServer);
                    chatArea.append("\n");
                }
                //read from server
                while (true) {
                    String strFromServer = inputStream.readUTF();
                    new Thread(() -> {
                        writeToFile(strFromServer);
                    }).start();
                    if (strFromServer.equals(ChatConstants.STOP_WORD)) {
                        break;
                    } else if (strFromServer.startsWith(ChatConstants.CLIENTS_LIST)) {
                        chatArea.append("Сейчас онлайн " + strFromServer);
                    } else {
                        chatArea.append(strFromServer);
                    }
                    chatArea.append("\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private synchronized void writeToFile(String string) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFileName, true))) {
            writer.write(string);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatHistory(int msgCount) {
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFileName))) {
            String str;
            while ((str = reader.readLine()) != null) {
                chatArea.append(str);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String ReadLastLine(File file) throws FileNotFoundException, IOException {
        String result = null;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            long startIdx = file.length();
            while (startIdx >= 0 && (result == null || result.length() == 0)) {
                raf.seek(startIdx);
                if (startIdx > 0)
                    raf.readLine();
                result = raf.readLine();
                startIdx--;
            }
        }
        return result;
    }

    public void closeConnection() {
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