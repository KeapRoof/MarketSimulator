package com.market.menu;

import com.market.assets.Asset;
import com.market.db.dao.AssetDAO;
import com.market.handlers.TransactionHandler;
import com.market.users.User;
import com.market.views.WalletView;

import java.util.List;
import java.util.Scanner;

public class Menu {
    private final TransactionHandler transactionHandler;
    private final WalletView walletView;

    public Menu(TransactionHandler transactionHandler, WalletView walletView) {
        this.transactionHandler = transactionHandler;
        this.walletView = walletView;
    }

    public void showMenu(Scanner scanner, User user) {
        int choice = -1;
        AssetDAO assetDao = new AssetDAO();
        List<Asset> market = assetDao.getAll();

        while (choice != 5) {
            System.out.println();
            System.out.println("========== MARKET SIMULATOR ==========");
            System.out.println("1. List all assets");
            System.out.println("2. Search an asset by ticker");
            System.out.println("3. Trade (Buy / Sell)");      // Menu Transaction
            System.out.println("4. View your portfolio");    // Menu Consultation
            System.out.println("5. Exit");
            System.out.println("======================================");
            System.out.print("Your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input.");
                scanner.nextLine();
                continue;
            }

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    System.out.println("Listing all assets:");
                    market.forEach(System.out::println);
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
                case 3 -> transactionHandler.handleTransactionMenu(scanner, user, market);
                case 4 -> walletView.displayWallet(user, market);
                case 5 -> {
                    assetDao.saveAllAssets(market);
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Unknown option.");
            }
        }
    }
}