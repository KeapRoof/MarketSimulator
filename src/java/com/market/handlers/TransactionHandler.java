package com.market.handlers;

import com.market.assets.Asset;
import com.market.services.TransactionService;
import com.market.users.User;
import com.market.views.WalletView;
import com.market.wallet.Position;
import com.market.wallet.Wallet;

import java.util.List;
import java.util.Scanner;

public class TransactionHandler {

    private final TransactionService transactionService;
    private final WalletView walletView; // On ajoute la Vue ici

    // On injecte la Vue dans le constructeur
    public TransactionHandler(TransactionService transactionService, WalletView walletView) {
        this.transactionService = transactionService;
        this.walletView = walletView;
    }

    public void handleTransactionMenu(Scanner scanner, User user, List<Asset> market) {
        while (true) {
            System.out.println("\n=== TRANSACTION MODE ===");
            System.out.println("1. Acheter un actif");
            System.out.println("2. Vendre un actif");
            System.out.println("0. Retour");
            System.out.print("Choix : ");

            if (!scanner.hasNextInt()) {
                System.out.println("Erreur : Veuillez entrer un nombre.");
                scanner.nextLine();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> handleBuy(scanner, user, market);
                case 2 -> handleSell(scanner, user, market);
                case 0 -> {
                    return;
                }
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    private void handleBuy(Scanner scanner, User user, List<Asset> market) {
        System.out.println("\n--- ACHAT D'ACTIF ---");

        // 1. Afficher le marché
        displayMarket(market);

        System.out.print("\nEntrez le ticker (ex: AAPL, BTC) : ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        // Recherche par Ticker
        Asset asset = market.stream()
                .filter(a -> a.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);

        if (asset == null) {
            System.out.println("❌ Erreur : Actif introuvable sur le marché.");
            return;
        }

        // Affichage info simple
        System.out.printf("Actif sélectionné : %s - Prix : %.2f €%n", asset.getName(), asset.getPrice());
        System.out.printf("Votre solde : %.2f €%n", user.getBalance());

        System.out.print("Quantité à acheter : ");
        if (!scanner.hasNextDouble()) {
            System.out.println("❌ Erreur : Saisie invalide.");
            scanner.nextLine();
            return;
        }
        double quantity = scanner.nextDouble();
        scanner.nextLine();

        if (quantity <= 0) {
            System.out.println("❌ Quantité invalide.");
            return;
        }

        double totalCost = transactionService.calculatePurchaseAmount(asset, quantity);
        System.out.println("\n--- RÉCAPITULATIF ACHAT ---");
        System.out.printf("Total à payer : %.2f €%n", totalCost);
        System.out.printf("Solde après   : %.2f €%n", user.getBalance().doubleValue() - totalCost);

        System.out.print("\nConfirmer l'achat ? (o/n) : ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("o")) {
            System.out.println("❌ Annulé.");
            return;
        }

        try {
            transactionService.executeBuyOrder(user, asset, quantity);
            System.out.println("\n✅ Achat réussi !");
        } catch (Exception e) {
            System.out.println("\n❌ Erreur : " + e.getMessage());
        }
    }

    /**
     * Gère la vente (Version Corrigée et Améliorée)
     */
    private void handleSell(Scanner scanner, User user, List<Asset> market) {
        System.out.println("\n--- VENTE D'ACTIF ---");

        walletView.displayWallet(user, market);

        System.out.print("\nEntrez le ticker à vendre : ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        Asset liveAsset = market.stream()
                .filter(a -> a.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);

        if (liveAsset == null) {
            System.out.println("❌ Cet actif n'existe pas sur le marché.");
            return;
        }

        Wallet wallet = transactionService.getUserWallet(user.getId(), liveAsset);

        // Grâce au equals() dans Asset, cette vérification fonctionne directement
        if (wallet == null || !wallet.hasAsset(liveAsset)) {
            System.out.println("❌ Vous ne possédez pas cet actif (" + liveAsset.getTicker() + ").");
            return;
        }

        Position position = wallet.findPositionByAsset(liveAsset).orElseThrow();

        // Indispensable : On remplace l'asset DB par l'asset Live pour avoir le bon prix
        position.setAsset(liveAsset);

        System.out.println("\n--- INFO VENTE ---");
        System.out.printf("Actif         : %s%n", liveAsset.getName());
        System.out.printf("Prix actuel   : %.2f €%n", liveAsset.getPrice());
        System.out.printf("Disponibles   : %.4f%n", position.getQuantity());
        System.out.printf("Prix d'achat  : %.2f €%n", position.getAveragePurchasePrice());

        System.out.print("Quantité à vendre : ");
        if (!scanner.hasNextDouble()) {
            System.out.println("❌ Saisie invalide.");
            scanner.nextLine();
            return;
        }
        double quantity = scanner.nextDouble();
        scanner.nextLine();

        if (quantity <= 0 || quantity > position.getQuantity()) {
            System.out.println("❌ Quantité invalide. Maximum : " + position.getQuantity());
            return;
        }

        double totalGain = transactionService.calculateSaleAmount(liveAsset, quantity);
        double profit = (liveAsset.getPrice() - position.getAveragePurchasePrice()) * quantity;

        System.out.println("\n--- CONFIRMATION ---");
        System.out.printf("Vente de      : %.4f %s%n", quantity, liveAsset.getTicker());
        System.out.printf("Montant total : %.2f €%n", totalGain);
        System.out.printf("Résultat P/L  : %.2f €%n", profit);
        System.out.printf("Solde après   : %.2f €%n", user.getBalance().doubleValue() + totalGain);

        System.out.print("\nConfirmer ? (o/n) : ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("o")) {
            System.out.println("❌ Vente annulée.");
            return;
        }

        try {
            transactionService.executeSellOrder(user, liveAsset, quantity);
            System.out.println("\n✅ Vente réussie !");
        } catch (Exception e) {
            System.out.println("\n❌ Erreur : " + e.getMessage());
        }
    }

    // --- Utilitaires ---
    private void displayMarket(List<Asset> market) {
        System.out.println("\n--- MARCHÉ DISPONIBLE ---");
        System.out.printf("%-10s %-20s %-10s%n", "TICKER", "NOM", "PRIX");
        System.out.println("-".repeat(45));
        for (Asset a : market) {
            System.out.printf("%-10s %-20s %.2f €%n", a.getTicker(),
                    a.getName().length() > 20 ? a.getName().substring(0, 17) + "..." : a.getName(),
                    a.getPrice());
        }
    }
}