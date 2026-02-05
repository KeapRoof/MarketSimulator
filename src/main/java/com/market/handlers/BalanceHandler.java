package com.market.handlers;

import com.market.db.dao.UserDAO;
import com.market.users.User;

import java.math.BigDecimal;
import java.util.Scanner;

public class BalanceHandler {

    private final UserDAO userDAO;

    public BalanceHandler(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void handleBalanceMenu(Scanner scanner, User user) {
        while (true) {
            System.out.println("\n=== BALANCE MANAGEMENT ===");
            System.out.printf("Current balance: %.2f €%n", user.getBalance());
            System.out.println("1. Deposit funds");
            System.out.println("2. Withdraw funds");
            System.out.println("0. Back");
            System.out.print("Your choice: ");

            if (!scanner.hasNextInt()) {
                System.out.println("❌ Invalid input.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> handleDeposit(scanner, user);
                case 2 -> handleWithdraw(scanner, user);
                case 0 -> {
                    return;
                }
                default -> System.out.println("❌ Unknown option.");
            }
        }
    }

    public void handleDeposit(Scanner scanner, User user) {
        System.out.println("\n--- DEPOSIT FUNDS ---");
        System.out.printf("Current balance: %.2f €%n", user.getBalance());
        System.out.print("Amount to deposit: ");

        if (!scanner.hasNextDouble()) {
            System.out.println("❌ Invalid amount.");
            scanner.nextLine();
            return;
        }

        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("❌ Amount must be positive.");
            return;
        }

        // Confirmation
        System.out.printf("\nDeposit %.2f € to your account?%n", amount);
        System.out.print("Confirm (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("❌ Deposit cancelled.");
            return;
        }

        // Exécuter le dépôt
        BigDecimal depositAmount = BigDecimal.valueOf(amount);
        boolean success = userDAO.addBalance(user.getId(), depositAmount);

        if (success) {
            // Mettre à jour l'objet user en mémoire
            user.setBalance(user.getBalance().add(depositAmount));
            System.out.println("\n✅ Deposit successful!");
            System.out.printf("New balance: %.2f €%n", user.getBalance());
        } else {
            System.out.println("❌ Deposit failed. Please try again.");
        }
    }

    public void handleWithdraw(Scanner scanner, User user) {
        System.out.println("\n--- WITHDRAW FUNDS ---");
        System.out.printf("Current balance: %.2f €%n", user.getBalance());
        System.out.print("Amount to withdraw: ");

        if (!scanner.hasNextDouble()) {
            System.out.println("❌ Invalid amount.");
            scanner.nextLine();
            return;
        }

        double amount = scanner.nextDouble();
        scanner.nextLine();

        if (amount <= 0) {
            System.out.println("❌ Amount must be positive.");
            return;
        }

        if (user.getBalance().doubleValue() < amount) {
            System.out.println("❌ Insufficient balance.");
            System.out.printf("Available: %.2f €%n", user.getBalance());
            return;
        }

        // Confirmation
        System.out.printf("\nWithdraw %.2f € from your account?%n", amount);
        System.out.print("Confirm (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("y") && !confirm.equals("yes")) {
            System.out.println("❌ Withdrawal cancelled.");
            return;
        }

        // Exécuter le retrait
        BigDecimal withdrawAmount = BigDecimal.valueOf(amount);
        boolean success = userDAO.withdrawBalance(user.getId(), withdrawAmount);

        if (success) {
            // Mettre à jour l'objet user en mémoire
            user.setBalance(user.getBalance().subtract(withdrawAmount));
            System.out.println("\n✅ Withdrawal successful!");
            System.out.printf("New balance: %.2f €%n", user.getBalance());
        } else {
            System.out.println("❌ Withdrawal failed. Insufficient funds or database error.");
        }
    }

    public void displayBalanceHistory(User user) {
        System.out.println("\n--- BALANCE HISTORY ---");
        System.out.printf("Current balance: %.2f €%n", user.getBalance());
        System.out.println("💡 Transaction history feature coming soon...");
    }
}