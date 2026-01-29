package com.market.assets;

import com.market.commons.enums.AssetType;

public abstract class Asset {
    private String name;
    private String ticker;
    private double price;
    private AssetType type;
    private Thread priceUpdater;

    public Asset(String name, String ticker, double price, AssetType type) {
        this.name = name;
        this.ticker = ticker;
        this.price = price;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getTicker() {
        return ticker;
    }

    public double getPrice() {
        return price;
    }

    public AssetType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setType(AssetType type) {
        this.type = type;
    }

    private void startPriceUpdater() {
        this.priceUpdater = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                this.setPrice(this.fluctuatePrice(this.getPrice()));
                System.out.println("Updated price of " + this.getTicker() + ": " + this.getPrice());
            }
        });
        priceUpdater.setDaemon(true);
        priceUpdater.start();
    }

    private double fluctuatePrice(double currentPrice) {
        double changePercent = (Math.random() * 2 - 1) * 0.01;
        return currentPrice * (1 + changePercent);
    }
}
