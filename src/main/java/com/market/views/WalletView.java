package com.market.views;

import com.market.assets.Asset;
import com.market.db.dao.WalletDAO;
import com.market.users.User;
import com.market.wallet.Position;
import com.market.wallet.Wallet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WalletView {

    private final WalletDAO walletDAO;

    public WalletView(WalletDAO walletDAO) {
        this.walletDAO = walletDAO;
    }

    public void displayWallet(User user, List<Asset> market) {
        // 1. Synchronisation avec les prix du marché
        Map<String, Double> currentMarketPrices = market.stream()
                .collect(Collectors.toMap(Asset::getTicker, Asset::getPrice));

        System.out.println("\n========== 👤 YOUR PORTFOLIO ==========");
        System.out.printf("User: %s%n", user.getUsername());
        System.out.printf("Cash balance: %.2f €%n", user.getBalance());
        System.out.println("======================================");

        List<Wallet> wallets = walletDAO.findAllByUser(user.getId());

        if (wallets.isEmpty()) {
            System.out.println("\n📭 No investments yet.");
            System.out.println("Start trading to build your portfolio!");
            return;
        }

        double totalPortfolioValue = 0.0;
        double totalProfitLoss = 0.0;
        boolean hasPositions = false;

        for (Wallet wallet : wallets) {
            // Mise à jour des prix avant calcul
            updateWalletPrices(wallet, currentMarketPrices);

            if (!wallet.isEmpty()) {
                hasPositions = true;
                displayWalletDetails(wallet);
                totalPortfolioValue += wallet.getTotalValue();
                totalProfitLoss += wallet.getTotalProfitLoss();
            }
        }

        if (!hasPositions) {
            System.out.println("\n📭 No active positions in your wallets.");
        } else {
            displayPortfolioSummary(user, totalPortfolioValue, totalProfitLoss);
        }
    }

    private void updateWalletPrices(Wallet wallet, Map<String, Double> prices) {
        for (Position position : wallet.getPositions()) {
            String ticker = position.getAsset().getTicker();
            if (prices.containsKey(ticker)) {
                position.getAsset().setPrice(prices.get(ticker));
            }
        }
    }

    private void displayWalletDetails(Wallet wallet) {
        System.out.println("\n--- 💼 " + wallet.getAssetType() + " WALLET ---");
        System.out.printf("Total value: %.2f €%n", wallet.getTotalValue());
        System.out.printf("Total P/L: %.2f € (%.2f%%)%n",
                wallet.getTotalProfitLoss(),
                wallet.getTotalProfitLossPercent());

        System.out.println("\nPositions:");
        System.out.printf("%-10s %-20s %12s %12s %12s %15s%n",
                "TICKER", "NAME", "QUANTITY", "AVG PRICE", "CURR PRICE", "P/L");
        System.out.println("-".repeat(95));

        for (Position position : wallet.getPositions()) {
            String profitLossStr = String.format("%.2f € (%.1f%%)",
                    position.getProfitLoss(),
                    position.getProfitLossPercent());

            // Choix de l'emoji selon la performance
            String emoji = position.isProfitable() ? "✅" : "❌";

            System.out.printf("%-10s %-20s %12.4f %12.2f %12.2f %15s %s%n",
                    position.getAsset().getTicker(),
                    truncate(position.getAsset().getName(), 20),
                    position.getQuantity(),
                    position.getAveragePurchasePrice(),
                    position.getAsset().getPrice(),
                    profitLossStr,
                    emoji);
        }
    }

    private void displayPortfolioSummary(User user, double totalPortfolioValue, double totalProfitLoss) {
        double totalWealth = user.getBalance().doubleValue() + totalPortfolioValue;
        double investedCapital = totalPortfolioValue - totalProfitLoss;
        double plPercent = investedCapital > 0 ? (totalProfitLoss / investedCapital) * 100 : 0;

        System.out.println("\n" + "=".repeat(95));
        System.out.println("📊 PORTFOLIO SUMMARY");
        System.out.println("=".repeat(95));
        System.out.printf("Cash balance:        %12.2f €%n", user.getBalance());
        System.out.printf("Invested value:      %12.2f €%n", totalPortfolioValue);
        System.out.printf("Total wealth:        %12.2f €%n", totalWealth);
        System.out.printf("Total P/L:           %12.2f € (%.2f%%) %s%n",
                totalProfitLoss,
                plPercent,
                totalProfitLoss >= 0 ? "🚀" : "📉");
        System.out.println("=".repeat(95));
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return (str.length() <= maxLength) ? str : str.substring(0, maxLength - 3) + "...";
    }
}