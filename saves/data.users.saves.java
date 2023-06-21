import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class GameUserSaver {
    private Map<String, String> userMap;

    public GameUserSaver() {
        userMap = new HashMap<>();
    }

    public void addUser(String username, String password) {
        userMap.put(username, password);
    }

    public void saveToFile(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, String> entry : userMap.entrySet()) {
                writer.println("Username: " + entry.getKey());
                writer.println("Password: " + entry.getValue());
                writer.println();
            }
            System.out.println("User info saved to file: " + filePath);
        } catch (IOException e) {
            System.out.println("An error occurred while saving the user info to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        GameUserSaver userSaver = new GameUserSaver();
        userSaver.addUser("user1", "password1");
        userSaver.addUser("user2", "password2");
        userSaver.saveToFile("user_info.txt");
    }
}
