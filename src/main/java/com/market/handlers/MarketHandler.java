package com.market.handlers;

import com.market.assets.Asset;

import java.util.List;
import java.util.Scanner;

public class MarketHandler {

    public void displayAllAssets(List<Asset> market) {
        System.out.println("\n=== MARKET ASSETS ===");
        System.out.printf("%-10s %-30s %-12s %-10s%n", "TICKER", "NAME", "PRICE", "TYPE");
        System.out.println("-".repeat(65));

        market.forEach(asset ->
                System.out.printf("%-10s %-30s %12.2f € %-10s%n",
                        asset.getTicker(),
                        truncate(asset.getName(), 30),
                        asset.getPrice(),
                        asset.getType())
        );

        System.out.printf("%nTotal assets: %d%n", market.size());
    }

    public void searchAsset(Scanner scanner, List<Asset> market) {
        System.out.print("\nEnter ticker symbol: ");
        String ticker = scanner.nextLine().trim().toUpperCase();

        Asset asset = market.stream()
                .filter(a -> a.getTicker().equalsIgnoreCase(ticker))
                .findFirst()
                .orElse(null);

        if (asset != null) {
            displayAssetDetails(asset);
        } else {
            System.out.println("❌ Asset not found.");
            suggestSimilarTickers(ticker, market);
        }
    }

    private void displayAssetDetails(Asset asset) {
        System.out.println("\n✅ Asset found:");
        System.out.println("━".repeat(50));
        System.out.printf("Ticker:   %s%n", asset.getTicker());
        System.out.printf("Name:     %s%n", asset.getName());
        System.out.printf("Type:     %s%n", asset.getType());
        System.out.printf("Price:    %.2f €%n", asset.getPrice());
        System.out.printf("Volatility: %.2f%%%n", asset.getVolatilityRate());

        // Informations spécifiques selon le type
        if (asset instanceof com.market.assets.Stock) {
            com.market.assets.Stock stock = (com.market.assets.Stock) asset;
            if (stock.getDividendYield() > 0) {
                System.out.printf("Dividend:  %.2f%%%n", stock.getDividendYield());
            }
            if (stock.getMarketOpenTime() != null && stock.getMarketCloseTime() != null) {
                System.out.printf("Hours:     %s - %s%n",
                        stock.getMarketOpenTime(), stock.getMarketCloseTime());
            }
        } else if (asset instanceof com.market.assets.Crypto) {
            com.market.assets.Crypto crypto = (com.market.assets.Crypto) asset;
            System.out.printf("Blockchain: %s%n", crypto.getBlockchain());
        }

        System.out.println("━".repeat(50));
    }

    private void suggestSimilarTickers(String searchTicker, List<Asset> market) {
        List<String> suggestions = market.stream()
                .map(Asset::getTicker)
                .filter(ticker -> ticker.startsWith(searchTicker.substring(0, Math.min(2, searchTicker.length()))))
                .limit(3)
                .toList();

        if (!suggestions.isEmpty()) {
            System.out.println("💡 Did you mean: " + String.join(", ", suggestions) + "?");
        }
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}