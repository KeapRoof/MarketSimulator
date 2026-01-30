import com.market.assets.Asset;
import com.market.db.dao.AssetDAO;
import menu.Menu;

import java.sql.*;
import java.util.List;
import java.util.Scanner;


public class Main {

    public static final String DB_URL = "jdbc:mariadb://localhost:3306/market";
    public static final String DB_USER = "market_user";
    public static final String DB_PASS = "market_pass";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authenticate(username, password)) {
            System.out.println("Authentication successful!");
            Menu.showMenu(scanner);
        } else {
            System.out.println("Authentication failed. Invalid username or password.");
        }

    }

    private static boolean authenticate(String username, String password) {

        String sql = """
                    SELECT password
                    FROM users
                    WHERE username = ?
                """;

        try (Connection conn = DriverManager.getConnection(
                DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String storedPassword = rs.getString("password");

            return storedPassword.equals(password);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}