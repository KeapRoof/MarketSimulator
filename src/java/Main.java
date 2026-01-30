import com.market.assets.Asset;
import com.market.db.dao.AssetDao;

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
            showMenu(scanner);
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

    private static void showMenu(Scanner scanner) {
        int choice = -1;
        AssetDao assetDao = new AssetDao();
        List<Asset> market =  assetDao.getAllAssets();

        while (choice != 6) {
            System.out.println();
            System.out.println("========== MARKET SIMULATOR ==========");
            System.out.println("1. List all assets");
            System.out.println("2. Search an asset by ticker");
            System.out.println("3. Buy an asset");
            System.out.println("4. Sell an asset");
            System.out.println("5. View your portfolio");
            System.out.println("6. Exit");
            System.out.println("======================================");
            System.out.print("Your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {;
                    System.out.println("Listing all assets:");
                    for (Asset asset : market) {
                        System.out.println(asset.toString());
                    }
                }
                case 2 -> {
                    System.out.print("Enter ticker symbol: ");
                    String ticker = scanner.nextLine();
                    Asset asset = market.stream()
                            .filter(a -> a.getTicker().equalsIgnoreCase(ticker))
                            .findFirst()
                            .orElse(null);
                    if (asset != null) {
                        System.out.printf("Found asset: %s%n", asset);
                    } else {
                        System.out.println("Asset not found.");
                    }
                }
                case 3 -> System.out.println("Buying asset...");
                case 4 -> System.out.println("Selling asset...");
                case 5 -> System.out.println("Displaying portfolio...");
                case 6 -> {
                    assetDao.saveAllAssets(market);
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Unknown option. Try again.");
            }
        }
    }
}