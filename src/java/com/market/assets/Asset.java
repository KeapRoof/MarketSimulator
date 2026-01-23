package com.market.assets;

import com.market.commons.enums.AssetType;

public abstract class Asset {
    private String name;
    private String ticker;
    private double price;
    private AssetType type;

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
}
