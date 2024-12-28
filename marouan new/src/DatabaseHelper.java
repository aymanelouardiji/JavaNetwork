import java.sql.*;

public class DatabaseHelper {
    private static final String URL = "jdbc:postgresql://localhost:5432/ensajava";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void saveFileDetails(String fileName, long fileSize) {
        String query = "INSERT INTO files (file_name, file_size) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, fileName);
            statement.setLong(2, fileSize);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("File details saved successfully.");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}