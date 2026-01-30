package com.market.db.dao;

import com.market.assets.Asset;
import com.market.assets.Crypto;
import com.market.assets.Stock;
import com.market.commons.enums.AssetType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class AssetDAO {

    /**
     * Mapper pour convertir un ResultSet en Asset (Stock ou Crypto)
     */
    private Asset mapAsset(ResultSet rs) throws SQLException {
        AssetType type = AssetType.valueOf(rs.getString("asset_type"));

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
     * Sauvegarde un Stock (insert ou update)
     */
    public boolean saveStock(Stock stock) {
        String sql = """
                    INSERT INTO assets (name, ticker, price, volatility_rate, asset_type, 
                                       dividend_yield, market_open_time, market_close_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name),
                        price = VALUES(price),
                        volatility_rate = VALUES(volatility_rate),
                        dividend_yield = VALUES(dividend_yield),
                        market_open_time = VALUES(market_open_time),
                        market_close_time = VALUES(market_close_time)
                """;

        return Db.update(sql,
                stock.getName(),
                stock.getTicker(),
                stock.getPrice(),
                stock.getVolatilityRate(),
                stock.getType().name(),
                stock.getDividendYield(),
                stock.getMarketOpenTime() != null ? java.sql.Time.valueOf(stock.getMarketOpenTime()) : null,
                stock.getMarketCloseTime() != null ? java.sql.Time.valueOf(stock.getMarketCloseTime()) : null
        ) > 0;
    }

    /**
     * Sauvegarde une Crypto (insert ou update)
     */
    public boolean saveCrypto(Crypto crypto) {
        String sql = """
                    INSERT INTO assets (name, ticker, price, volatility_rate, asset_type, blockchain)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name),
                        price = VALUES(price),
                        volatility_rate = VALUES(volatility_rate),
                        blockchain = VALUES(blockchain)
                """;

        return Db.update(sql,
                crypto.getName(),
                crypto.getTicker(),
                crypto.getPrice(),
                crypto.getVolatilityRate(),
                crypto.getType().name(),
                crypto.getBlockchain()
        ) > 0;
    }

    /**
     * Sauvegarde un Asset générique (détecte automatiquement le type)
     */
    public boolean saveAsset(Asset asset) {
        if (asset instanceof Stock) {
            return saveStock((Stock) asset);
        } else if (asset instanceof Crypto) {
            return saveCrypto((Crypto) asset);
        }
        throw new IllegalArgumentException("Type d'asset non supporté: " + asset.getClass().getName());
    }

    /**
     * Sauvegarde plusieurs assets en batch
     */
    public boolean saveAllAssets(List<Asset> assets) {
        if (assets == null || assets.isEmpty()) {
            return true;
        }

        // On peut faire un batch, mais il faut séparer Stocks et Cryptos
        // car ils n'ont pas les mêmes colonnes
        boolean allSuccess = true;

        for (Asset asset : assets) {
            if (!saveAsset(asset)) {
                allSuccess = false;
            }
        }

        return allSuccess;
    }

    /**
     * Version optimisée avec batch séparé pour Stocks et Cryptos
     */
    public boolean saveAllAssetsOptimized(List<Asset> assets) {
        if (assets == null || assets.isEmpty()) {
            return true;
        }

        // Séparer les stocks et les cryptos
        List<Stock> stocks = assets.stream()
                .filter(a -> a instanceof Stock)
                .map(a -> (Stock) a)
                .toList();

        List<Crypto> cryptos = assets.stream()
                .filter(a -> a instanceof Crypto)
                .map(a -> (Crypto) a)
                .toList();

        boolean stocksSuccess = saveAllStocks(stocks);
        boolean cryptosSuccess = saveAllCryptos(cryptos);

        return stocksSuccess && cryptosSuccess;
    }

    /**
     * Batch insert/update pour Stocks uniquement
     */
    private boolean saveAllStocks(List<Stock> stocks) {
        if (stocks.isEmpty()) return true;

        String sql = """
                    INSERT INTO assets (name, ticker, price, volatility_rate, asset_type, 
                                       dividend_yield, market_open_time, market_close_time)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name),
                        price = VALUES(price),
                        volatility_rate = VALUES(volatility_rate),
                        dividend_yield = VALUES(dividend_yield),
                        market_open_time = VALUES(market_open_time),
                        market_close_time = VALUES(market_close_time)
                """;

        return Db.query(conn -> {
            try (var stmt = conn.prepareStatement(sql)) {
                for (Stock stock : stocks) {
                    stmt.setString(1, stock.getName());
                    stmt.setString(2, stock.getTicker());
                    stmt.setDouble(3, stock.getPrice());
                    stmt.setDouble(4, stock.getVolatilityRate());
                    stmt.setString(5, stock.getType().name());
                    stmt.setFloat(6, stock.getDividendYield());
                    stmt.setTime(7, stock.getMarketOpenTime() != null ?
                            java.sql.Time.valueOf(stock.getMarketOpenTime()) : null);
                    stmt.setTime(8, stock.getMarketCloseTime() != null ?
                            java.sql.Time.valueOf(stock.getMarketCloseTime()) : null);
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                for (int result : results) {
                    if (result == Statement.EXECUTE_FAILED) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Batch insert/update pour Cryptos uniquement
     */
    private boolean saveAllCryptos(List<Crypto> cryptos) {
        if (cryptos.isEmpty()) return true;

        String sql = """
                    INSERT INTO assets (name, ticker, price, volatility_rate, asset_type, blockchain)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                        name = VALUES(name),
                        price = VALUES(price),
                        volatility_rate = VALUES(volatility_rate),
                        blockchain = VALUES(blockchain)
                """;

        return Db.query(conn -> {
            try (var stmt = conn.prepareStatement(sql)) {
                for (Crypto crypto : cryptos) {
                    stmt.setString(1, crypto.getName());
                    stmt.setString(2, crypto.getTicker());
                    stmt.setDouble(3, crypto.getPrice());
                    stmt.setDouble(4, crypto.getVolatilityRate());
                    stmt.setString(5, crypto.getType().name());
                    stmt.setString(6, crypto.getBlockchain());
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                for (int result : results) {
                    if (result == Statement.EXECUTE_FAILED) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Trouve un actif par son ticker
     */
    public Optional<Asset> findByTicker(String ticker) {
        return Db.find("SELECT * FROM assets WHERE ticker = ?", this::mapAsset, ticker);
    }

    /**
     * Récupère tous les actifs
     */
    public List<Asset> getAll() {
        return Db.list("SELECT * FROM assets", this::mapAsset);
    }

    /**
     * Récupère tous les stocks
     */
    public List<Asset> getAllStocks() {
        return Db.list("SELECT * FROM assets WHERE asset_type = ?",
                this::mapAsset, AssetType.Stock.name());
    }

    /**
     * Récupère toutes les cryptos
     */
    public List<Asset> getAllCryptos() {
        return Db.list("SELECT * FROM assets WHERE asset_type = ?",
                this::mapAsset, AssetType.Crypto.name());
    }

    /**
     * Met à jour le prix d'un actif
     */
    public boolean updatePrice(String ticker, double newPrice) {
        return Db.update("UPDATE assets SET price = ? WHERE ticker = ?", newPrice, ticker) > 0;
    }

    /**
     * Supprime un actif
     */
    public boolean delete(String ticker) {
        return Db.update("DELETE FROM assets WHERE ticker = ?", ticker) > 0;
    }

    /**
     * Vérifie si un ticker existe
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
        return Db.count("SELECT COUNT(*) FROM assets WHERE asset_type = ?", type.name());
    }
}