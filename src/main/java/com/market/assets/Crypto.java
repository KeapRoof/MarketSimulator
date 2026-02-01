package com.market.assets;

import com.market.commons.enums.AssetType;

public class Crypto extends Asset {
    private String blockchain;

    public Crypto(String name, String ticker, double price, String blockchain) {
        super(name, ticker, price, 0.05, AssetType.Crypto);
        this.blockchain = blockchain;
    }

    public String getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(String blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public boolean isMarketOpen() {
        return true; // Les cryptos sont ouvertes 24/7
    }
}