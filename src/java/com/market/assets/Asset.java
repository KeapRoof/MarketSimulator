package com.market.assets;

public class Asset {
    private String name;
    private String ticker;
    private double price;

    public Asset(String name, String ticker, double price) {
        this.name = name;
        this.ticker = ticker;
        this.price = price;
    }
}
