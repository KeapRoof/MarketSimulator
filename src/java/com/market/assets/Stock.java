package com.market.assets;

import com.market.commons.enums.AssetType;

import java.time.LocalTime;

public class Stock extends Asset{

    private float dividendYield;
    private LocalTime marketOpenTime;
    private LocalTime marketCloseTime;
    private Thread priceUpdater;

    public Stock(String name, String ticker, double price, float yield, LocalTime openTime, LocalTime closeTime) {
        super(name, ticker, price, AssetType.STOCK);
        this.dividendYield = yield;
        this.marketOpenTime = openTime;
        this.marketCloseTime = closeTime;
        this.startPriceUpdater();
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
