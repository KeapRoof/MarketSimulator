package com.market.wallet;

import com.market.assets.Asset;

public class Position {
    private Long id;
    private Long walletId;
    private Asset asset;
    private double quantity;
    private double averagePurchasePrice;

    // Constructeur complet
    public Position(Long id, Long walletId, Asset asset, double quantity, double averagePurchasePrice) {
        this.id = id;
        this.walletId = walletId;
        this.asset = asset;
        this.quantity = quantity;
        this.averagePurchasePrice = averagePurchasePrice;
    }

    // Constructeur pour nouvelle position (avant persistance)
    public Position(Asset asset, double quantity, double averagePurchasePrice) {
        this(null, null, asset, quantity, averagePurchasePrice);
    }

    public double getCurrentValue() {
        return quantity * asset.getPrice();
    }

    public double getTotalCost() {
        return quantity * averagePurchasePrice;
    }

    public double getProfitLoss() {
        return getCurrentValue() - getTotalCost();
    }

    public double getProfitLossPercent() {
        if (averagePurchasePrice == 0) return 0.0;
        return ((asset.getPrice() - averagePurchasePrice) / averagePurchasePrice) * 100;
    }

    public boolean isProfitable() {
        return asset.getPrice() > averagePurchasePrice;
    }

    public double getPriceChange() {
        return asset.getPrice() - averagePurchasePrice;
    }
    
    public double getWeightInWallet(double totalWalletValue) {
        if (totalWalletValue == 0) return 0.0;
        return (getCurrentValue() / totalWalletValue) * 100;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWalletId() {
        return walletId;
    }

    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getAveragePurchasePrice() {
        return averagePurchasePrice;
    }

    public void setAveragePurchasePrice(double averagePurchasePrice) {
        this.averagePurchasePrice = averagePurchasePrice;
    }

    @Override
    public String toString() {
        return String.format("Position{asset=%s, qty=%.4f, avgPrice=%.2f, currentValue=%.2f, P/L=%.2f%%}",
                asset.getTicker(), quantity, averagePurchasePrice, getCurrentValue(), getProfitLossPercent());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return id != null && id.equals(position.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}