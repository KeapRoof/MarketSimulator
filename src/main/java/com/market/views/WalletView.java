package com.market.views;

import com.market.assets.Asset;
import com.market.db.dao.WalletDAO;
import com.market.users.User;
import com.market.wallet.Position;
import com.market.wallet.Wallet;

import java.util.List;
import java.util.Optional;

public class WalletView {

    private final WalletDAO walletDAO;

    public WalletView(WalletDAO walletDAO) {
        this.walletDAO = walletDAO;
    }

    private static void printPositionRow(Position position) {
        double currentPrice = position.getAsset().getPrice();
        double profitLoss = position.getProfitLoss();
        String statusIcon = position.isProfitable() ? "✅" : "🔻";

        String profitLossStr = String.format("%.2f € (%.2f%%)",
                profitLoss,
                position.getProfitLossPercent());

        System.out.printf("%-10s %-20s %12.4f %12.2f %12.2f %-15s %s%n",
                position.getAsset().getTicker(),
                truncate(position.getAsset().getName(), 20),
                position.getQuantity(),
                position.getAveragePurchasePrice(),
                currentPrice,
                profitLossStr,
                statusIcon);
    }

    private static void printGlobalSummary(double totalValue, double totalPL) {
        String trend = totalPL >= 0 ? "GAIN" : "PERTE";

        System.out.println("\n" + "=".repeat(95));
        System.out.printf(" VALEUR TOTALE DU PORTEFEUILLE : %10.2f €%n", totalValue);
        System.out.printf(" PERFORMANCE GLOBALE (%s)    : %10.2f €%n", trend, totalPL);
        System.out.println("=".repeat(95));
    }

    private static String truncate(String str, int width) {
        if (str == null) return "";
        if (str.length() <= width) return str;
        return str.substring(0, width - 3) + "...";
    }

    public void displayWallet(User user, List<Asset> liveMarket) {
        System.out.println("\n=== VOS POSITIONS (" + user.getUsername().toUpperCase() + ") ===");

        List<Wallet> wallets = walletDAO.findAllByUser(user.getId());

        if (wallets == null || wallets.isEmpty()) {
            System.out.println("❌ Vous n'avez aucun portefeuille actif.");
            return;
        }

        synchronizePrices(wallets, liveMarket);

        boolean hasPositions = false;
        double totalGlobalValue = 0;
        double totalGlobalPL = 0;

        for (Wallet wallet : wallets) {
            if (!wallet.isEmpty()) {
                hasPositions = true;
                printWalletSection(wallet);
                totalGlobalValue += wallet.getTotalValue();
                totalGlobalPL += wallet.getTotalProfitLoss();
            }
        }

        if (!hasPositions) {
            System.out.println("Votre portefeuille est actuellement vide.");
        } else {
            printGlobalSummary(totalGlobalValue, totalGlobalPL);
        }
    }

    private void synchronizePrices(List<Wallet> wallets, List<Asset> liveMarket) {
        for (Wallet wallet : wallets) {
            for (Position position : wallet.getPositions()) {
                Optional<Asset> liveAsset = liveMarket.stream()
                        .filter(a -> a.getTicker().equalsIgnoreCase(position.getAsset().getTicker()))
                        .findFirst();

                liveAsset.ifPresent(position::setAsset);
            }
        }
    }

    private void printWalletSection(Wallet wallet) {
        System.out.println("\n--- WALLET " + wallet.getAssetType() + " ---");
        System.out.printf("Valeur totale : %.2f €%n", wallet.getTotalValue());

        double pl = wallet.getTotalProfitLoss();
        double plPercent = wallet.getTotalProfitLossPercent();
        String plColor = pl >= 0 ? "✅" : "🔻";

        System.out.printf("P/L total     : %.2f € (%.2f%%) %s%n", pl, plPercent, plColor);

        System.out.println("\nPositions :");
        System.out.printf("%-10s %-20s %-12s %-12s %-12s %-15s%n",
                "TICKER", "NOM", "QUANTITÉ", "PRIX ACH.", "PRIX ACT.", "P/L");
        System.out.println("-".repeat(95));

        for (Position position : wallet.getPositions()) {
            printPositionRow(position);
        }
    }
}