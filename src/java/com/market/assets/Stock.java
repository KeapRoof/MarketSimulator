package com.market.assets;

import com.market.commons.enums.AssetType;

import java.time.LocalTime;

public class Stock extends Asset{

    private float dividendYield;
    private LocalTime marketOpenTime;
    private LocalTime marketCloseTime;

    public Stock(String name, String ticker, double price, float yield, LocalTime openTime, LocalTime closeTime) {
        super(name, ticker, price, AssetType.STOCK);
        this.dividendYield = yield;
        this.marketOpenTime = openTime;
        this.marketCloseTime = closeTime;
    }

}
