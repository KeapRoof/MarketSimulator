package com.market.assets;

import com.market.commons.enums.AssetType;

public class Stock extends Asset{


    public Stock(String name, String ticker, double price) {
        super(name, ticker, price, AssetType.STOCK);
    }

}
