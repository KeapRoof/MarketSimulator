package com.market.db.dao;

import com.market.assets.Asset;
import com.market.assets.Crypto;
import com.market.assets.Stock;
import com.market.commons.enums.AssetType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AssetDAO {

    /**
     * Mapper pour convertir un ResultSet en Asset (Stock ou Crypto)
     */
    private Asset mapAsset(ResultSet rs) throws SQLException {
        AssetType type = AssetType.valueOf(rs.getString("type"));

        if (type == AssetType.Stock) {
            return new Stock(
                    rs.getString("name"),
                    rs.getString("ticker"),
                    rs.getDouble("price"),
                    rs.getFloat("dividend_yield"),
                    rs.getTime("market_open_time") != null ?
                            rs.getTime("market_open_time").toLocalTime() : null,
                    rs.getTime("market_close_time") != null ?
                            rs.getTime("market_close_time").toLocalTime() : null
            );
        } else if (type == AssetType.Crypto) {
            return new Crypto(
                    rs.getString("name"),
                    rs.getString("ticker"),
                    rs.getDouble("price"),
                    rs.getString("blockchain")
            );
        }

        throw new IllegalStateException("Type d'actif inconnu: " + type);
    }

    /**
     * Crée un nouvel actif (Stock)
     */
    public Long createStock(Stock stock) {
        String sql = "INSERT INTO assets (name, ticker, price, volatility_rate, type, dividend_yield, market_open_time, market_close_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        return Db.insert(sql,
                stock.getName(),
                stock.getTicker(),
                stock.getPrice(),
                stock.getVolatilityRate(),
                stock.getType().name(),
                stock.getDividendYield(),
                stock.getMarketOpenTime() != null ? java.sql.Time.valueOf(stock.getMarketOpenTime()) : null,
                stock.getMarketCloseTime() != null ? java.sql.Time.valueOf(stock.getMarketCloseTime()) : null
        );
    }

    /**
     * Crée un nouvel actif (Crypto)
     */
    public Long createCrypto(Crypto crypto) {
        String sql = "INSERT INTO assets (name, ticker, price, volatility_rate, type, blockchain) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        return Db.insert(sql,
                crypto.getName(),
                crypto.getTicker(),
                crypto.getPrice(),
                crypto.getVolatilityRate(),
                crypto.getType().name(),
                crypto.getBlockchain()
        );
    }

    /**
     * Trouve un actif par son ticker
     */
    public Optional<Asset> findByTicker(String ticker) {
        return Db.find("SELECT * FROM assets WHERE ticker = ?", this::mapAsset, ticker);
    }

    /**
     * Trouve un actif par son nom
     */
    public Optional<Asset> findByName(String name) {
        return Db.find("SELECT * FROM assets WHERE name = ?", this::mapAsset, name);
    }

    /**
     * Récupère tous les actifs
     */
    public List<Asset> findAll() {
        return Db.list("SELECT * FROM assets", this::mapAsset);
    }

    /**
     * Récupère tous les stocks
     */
    public List<Asset> findAllStocks() {
        return Db.list("SELECT * FROM assets WHERE type = ?", this::mapAsset, AssetType.Stock.name());
    }

    /**
     * Récupère toutes les cryptos
     */
    public List<Asset> findAllCryptos() {
        return Db.list("SELECT * FROM assets WHERE type = ?", this::mapAsset, AssetType.Crypto.name());
    }

    /**
     * Met à jour le prix d'un actif
     */
    public boolean updatePrice(String ticker, double newPrice) {
        return Db.update("UPDATE assets SET price = ? WHERE ticker = ?", newPrice, ticker) > 0;
    }

    /**
     * Met à jour un stock
     */
    public boolean updateStock(Stock stock) {
        String sql = "UPDATE assets SET name = ?, price = ?, volatility_rate = ?, " +
                "dividend_yield = ?, market_open_time = ?, market_close_time = ? " +
                "WHERE ticker = ?";

        return Db.update(sql,
                stock.getName(),
                stock.getPrice(),
                stock.getVolatilityRate(),
                stock.getDividendYield(),
                stock.getMarketOpenTime() != null ? java.sql.Time.valueOf(stock.getMarketOpenTime()) : null,
                stock.getMarketCloseTime() != null ? java.sql.Time.valueOf(stock.getMarketCloseTime()) : null,
                stock.getTicker()
        ) > 0;
    }

    /**
     * Met à jour une crypto
     */
    public boolean updateCrypto(Crypto crypto) {
        String sql = "UPDATE assets SET name = ?, price = ?, volatility_rate = ?, blockchain = ? " +
                "WHERE ticker = ?";

        return Db.update(sql,
                crypto.getName(),
                crypto.getPrice(),
                crypto.getVolatilityRate(),
                crypto.getBlockchain(),
                crypto.getTicker()
        ) > 0;
    }

    /**
     * Supprime un actif par son ticker
     */
    public boolean delete(String ticker) {
        return Db.update("DELETE FROM assets WHERE ticker = ?", ticker) > 0;
    }

    /**
     * Vérifie si un ticker existe déjà
     */
    public boolean tickerExists(String ticker) {
        return Db.exists("SELECT 1 FROM assets WHERE ticker = ?", ticker);
    }

    /**
     * Compte le nombre total d'actifs
     */
    public long countAll() {
        return Db.count("SELECT COUNT(*) FROM assets");
    }

    /**
     * Compte le nombre d'actifs par type
     */
    public long countByType(AssetType type) {
        return Db.count("SELECT COUNT(*) FROM assets WHERE type = ?", type.name());
    }
}