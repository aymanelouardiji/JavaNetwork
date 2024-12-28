import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class Client {

    private static final int PORT = 5000;
    private static final String SERVER_ADDRESS = "localhost";

    public Client() {
        setupGUI();
    }

    private void setupGUI() {
        // Frame setup
        JFrame frame = new JFrame("File Sharing Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(750, 550);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setResizable(false);

        // Header Label with a sleek design
        JLabel header = new JLabel("File Sharing Client", JLabel.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 26));
        header.setForeground(Color.WHITE);
        header.setBackground(new Color(0, 122, 204)); // Soft blue color
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(frame.getWidth(), 70));

        // Create a panel for content with GridBagLayout for better control
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240)); // Light gray background
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;

        // Server code input
        JLabel serverCodeLabel = createLabel("Server Code:");
        JTextField serverCodeField = createTextField();
        JButton connectButton = createButton("Connect");

        // File selection input
        JLabel fileLabel = createLabel("Choose File:");
        JButton fileButton = createButton("Browse");
        JLabel filePathLabel = new JLabel();
        filePathLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        JTextField commentField = createCommentField();
        JButton sendButton = createButton("Send");

        // Arrange components in the panel using GridBagLayout
        addComponentsToPanel(panel, gbc, serverCodeLabel, serverCodeField, connectButton,
                fileLabel, fileButton, filePathLabel, commentField, sendButton);

        // Add the header and panel to the frame
        frame.add(header, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        // Button hover effect
        addButtonHoverEffect(fileButton, connectButton, sendButton);

        // File chooser action
        fileButton.addActionListener(e -> chooseFile(filePathLabel));

        // Connect button action
        connectButton.addActionListener(e -> connectToServer(serverCodeField));

        // Send button action
        sendButton.addActionListener(e -> sendFile(serverCodeField, filePathLabel, commentField));
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        return textField;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(0, 122, 204)); // Blue color for buttons
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }

    private JTextField createCommentField() {
        JTextField commentField = new JTextField("Add a comment...", 20);
        commentField.setFont(new Font("Arial", Font.PLAIN, 14));
        commentField.setForeground(Color.GRAY);
        commentField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (commentField.getText().equals("Add a comment...")) {
                    commentField.setText("");
                    commentField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (commentField.getText().isEmpty()) {
                    commentField.setText("Add a comment...");
                    commentField.setForeground(Color.GRAY);
                }
            }
        });
        return commentField;
    }

    private void addComponentsToPanel(JPanel panel, GridBagConstraints gbc,
                                      JLabel serverCodeLabel, JTextField serverCodeField, JButton connectButton,
                                      JLabel fileLabel, JButton fileButton, JLabel filePathLabel,
                                      JTextField commentField, JButton sendButton) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(serverCodeLabel, gbc);
        gbc.gridx = 1;
        panel.add(serverCodeField, gbc);
        gbc.gridx = 2;
        panel.add(connectButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(fileLabel, gbc);
        gbc.gridx = 1;
        panel.add(fileButton, gbc);
        gbc.gridx = 2;
        panel.add(filePathLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(commentField, gbc);
        gbc.gridx = 1;
        panel.add(sendButton, gbc);
    }

    private void addButtonHoverEffect(JButton... buttons) {
        for (JButton button : buttons) {
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(new Color(85, 153, 255)); // Lighter blue
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    button.setBackground(new Color(0, 122, 204)); // Original blue
                }
            });
        }
    }

    private void chooseFile(JLabel filePathLabel) {
        JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathLabel.setText(selectedFile.getAbsolutePath());
        }
    }

    private void connectToServer(JTextField serverCodeField) {
        String serverCode = serverCodeField.getText().trim();
        if (serverCode.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Server code must be provided!");
            return;
        }
        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream())) {
                dos.writeUTF("Client Connected: " + serverCode);
                JOptionPane.showMessageDialog(null, "Connected to server!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error connecting to server: " + ex.getMessage());
            }
        }).start();
    }

    private void sendFile(JTextField serverCodeField, JLabel filePathLabel, JTextField commentField) {
        String serverCode = serverCodeField.getText().trim();
        String filePath = filePathLabel.getText().trim();
        String comment = commentField.getText().trim();

        if (serverCode.isEmpty() || filePath.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Server code and file must be provided!");
            return;
        }

        new Thread(() -> {
            try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                 DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                 FileInputStream fis = new FileInputStream(filePath)) {

                File file = new File(filePath);
                dos.writeUTF(file.getName());
                dos.writeUTF(comment);
                dos.writeLong(file.length());

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, bytesRead);
                }
                JOptionPane.showMessageDialog(null, "File sent successfully!");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::new);
    }
}
