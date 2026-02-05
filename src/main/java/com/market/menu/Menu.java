package com.market.menu;

import com.market.assets.Asset;
import com.market.db.dao.AssetDAO;
import com.market.handlers.BalanceHandler;
import com.market.handlers.MarketHandler;
import com.market.handlers.TransactionHandler;
import com.market.users.User;
import com.market.views.WalletView;

import java.util.List;
import java.util.Scanner;

public class Menu implements IMenu {
    private final TransactionHandler transactionHandler;
    private final WalletView walletView;
    private final BalanceHandler balanceHandler;
    private final MarketHandler marketHandler;

    public Menu(TransactionHandler transactionHandler,
                WalletView walletView,
                BalanceHandler balanceHandler,
                MarketHandler marketHandler) {
        this.transactionHandler = transactionHandler;
        this.walletView = walletView;
        this.balanceHandler = balanceHandler;
        this.marketHandler = marketHandler;
    }

    public void showMenu(Scanner scanner, User user) {
        int choice = -1;
        AssetDAO assetDao = new AssetDAO();
        List<Asset> market = assetDao.getAll();

        while (choice != 6) {
            System.out.println();
            System.out.println("========== MARKET SIMULATOR ==========");
            System.out.println("1. List all assets");
            System.out.println("2. Search an asset by ticker");
            System.out.println("3. Trade (Buy / Sell)");
            System.out.println("4. View your portfolio");
            System.out.println("5. Manage balance");
            System.out.println("6. Exit");
            System.out.println("======================================");
            System.out.printf("Current balance: %.2f €%n", user.getBalance());
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
                case 1 -> marketHandler.displayAllAssets(market);
                case 2 -> marketHandler.searchAsset(scanner, market);
                case 3 -> transactionHandler.handleTransactionMenu(scanner, user, market);
                case 4 -> walletView.displayWallet(user, market);
                case 5 -> balanceHandler.handleBalanceMenu(scanner, user);
                case 6 -> {
                    assetDao.saveAllAssets(market);
                    System.out.println("Goodbye!");
                }
                default -> System.out.println("Unknown option.");
            }
        }
    }
}