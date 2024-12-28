import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 5000;
    private String serverCode;
    private JTextArea logArea;

    public Server() {
        serverCode = "01";
        setupGUI();
    }

    private void setupGUI() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("File Sharing Server");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(700, 500);
            frame.setLocationRelativeTo(null);

            JPanel mainPanel = new JPanel(new BorderLayout());

            JLabel header = new JLabel("File Sharing Server", JLabel.CENTER);
            header.setFont(new Font("Arial", Font.BOLD, 28));
            header.setOpaque(true);
            header.setBackground(new Color(30, 144, 255));
            header.setForeground(Color.WHITE);
            mainPanel.add(header, BorderLayout.NORTH);

            JPanel centerPanel = new JPanel(new BorderLayout());
            centerPanel.setBackground(Color.LIGHT_GRAY);

            JLabel serverCodeLabel = new JLabel("Server Code: " + serverCode, JLabel.CENTER);
            serverCodeLabel.setFont(new Font("Arial", Font.BOLD, 16));
            centerPanel.add(serverCodeLabel, BorderLayout.NORTH);

            logArea = new JTextArea();
            logArea.setEditable(false);
            logArea.setBackground(Color.BLACK);
            logArea.setForeground(Color.GREEN);
            JScrollPane scrollPane = new JScrollPane(logArea);
            centerPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout());
            JButton receiveFileButton = new JButton("Receive File");
            JButton sendFileButton = new JButton("Send File");
            buttonPanel.add(receiveFileButton);
            buttonPanel.add(sendFileButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            mainPanel.add(centerPanel, BorderLayout.CENTER);
            frame.add(mainPanel);
            frame.setVisible(true);

            receiveFileButton.addActionListener(e -> logArea.append("Waiting for client to send file...\n"));
            sendFileButton.addActionListener(e -> handleSendFileButton());

            new Thread(this::startServer).start();
        });
    }

    private void handleSendFileButton() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            logArea.append("Selected file: " + file.getName() + "\n");
            sendFileToClient(file);
        }
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logArea.append("Server started on port " + PORT + "\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logArea.append("Client connected.\n");
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            logArea.append("Error: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream())) {

            String connectionMessage = dis.readUTF();
            logArea.append(connectionMessage + "\n");

            String fileName = dis.readUTF();
            long fileSize = dis.readLong();
            logArea.append("Receiving file: " + fileName + " (" + fileSize + " bytes)\n");

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select folder to save file");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                File saveDir = fileChooser.getSelectedFile();
                File receivedFile = new File(saveDir, fileName);

                saveReceivedFile(dis, fileSize, receivedFile);
            } else {
                logArea.append("File save canceled by user.\n");
            }

        } catch (IOException e) {
            logArea.append("Error handling client: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void saveReceivedFile(DataInputStream dis, long fileSize, File receivedFile) {
        try (FileOutputStream fos = new FileOutputStream(receivedFile)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            long remaining = fileSize;
            while ((bytesRead = dis.read(buffer, 0, (int) Math.min(buffer.length, remaining))) > 0) {
                fos.write(buffer, 0, bytesRead);
                remaining -= bytesRead;
            }
            logArea.append("File saved at: " + receivedFile.getAbsolutePath() + "\n");

            DatabaseHelper.saveFileDetails(receivedFile.getName(), fileSize);
        } catch (IOException e) {
            logArea.append("Error saving file: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void sendFileToClient(File file) {
        try (Socket socket = new Socket("localhost", PORT);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                dos.write(buffer, 0, bytesRead);
            }

            logArea.append("File sent successfully.\n");
        } catch (IOException e) {
            logArea.append("Error sending file: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Server::new);
    }
}
