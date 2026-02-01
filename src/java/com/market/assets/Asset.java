package com.market.assets;

import com.market.commons.enums.AssetType;

public abstract class Asset {
    private final AssetType type;
    private String name;
    private String ticker;
    private double price;
    private double volatilityRate;

    public Asset(String name, String ticker, double price, double volatilityRate, AssetType type) {
        this.name = name;
        this.ticker = ticker;
        this.price = price;
        this.volatilityRate = volatilityRate;
        this.type = type;
        this.startPriceUpdater();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    public double getVolatilityRate() {
        return volatilityRate;
    }

    public void setVolatilityRate(double volatilityRate) {
        this.volatilityRate = volatilityRate;
    }

    public AssetType getType() {
        return type;
    }

    public abstract boolean isMarketOpen();

    private void startPriceUpdater() {
        Thread priceUpdater = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    this.fluctuate();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        priceUpdater.setDaemon(true);
        priceUpdater.start();
    }

    public void fluctuate() {
        double change = (Math.random() * 2 - 1) * this.volatilityRate;
        this.price *= (1 + change);
        if (this.price < 0.01) this.price = 0.01;
    }

    @Override
    public String toString() {
        return String.format("%s (%s): $%.2f [%s]", name, ticker, price, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return ticker.equalsIgnoreCase(asset.ticker);
    }

    @Override
    public int hashCode() {
        return ticker.toUpperCase().hashCode();
    }
}