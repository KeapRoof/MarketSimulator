package menu;

import com.market.assets.Asset;
import com.market.db.dao.AssetDAO;

import java.util.List;
import java.util.Scanner;

public class Menu {

    public static void showMenu(Scanner scanner) {
        int choice = -1;
        AssetDAO assetDao = new AssetDAO();
        List<Asset> market = assetDao.getAll();

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
                case 1 -> {
                    ;
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
