package com.market.assets;

import com.market.commons.enums.AssetType;

import java.time.LocalTime;

public class Stock extends Asset {
    private float dividendYield;
    private LocalTime marketOpenTime;
    private LocalTime marketCloseTime;

    public Stock(String name, String ticker, double price, float yield, LocalTime open, LocalTime close) {
        super(name, ticker, price, 0.01, AssetType.STOCK);
        this.dividendYield = yield;
        this.marketOpenTime = open;
        this.marketCloseTime = close;
    }

    public float getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(float dividendYield) {
        this.dividendYield = dividendYield;
    }

    public LocalTime getMarketOpenTime() {
        return marketOpenTime;
    }

    public void setMarketOpenTime(LocalTime marketOpenTime) {
        this.marketOpenTime = marketOpenTime;
    }

    public LocalTime getMarketCloseTime() {
        return marketCloseTime;
    }

    public void setMarketCloseTime(LocalTime marketCloseTime) {
        this.marketCloseTime = marketCloseTime;
    }
}