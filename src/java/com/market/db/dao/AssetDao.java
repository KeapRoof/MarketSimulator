package com.market.db.dao;

import com.market.db.connection.DatabaseConnection;
import com.market.assets.Asset;
import com.market.commons.enums.AssetType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AssetDao {


    public List<Asset> getAllAssets() {
        List<Asset> assets = new ArrayList<>();
        String sql = "SELECT * FROM assets";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                AssetType type = AssetType.valueOf(rs.getString("asset_type"));
                Asset asset = new Asset(
                        rs.getString("name"),
                        rs.getString("ticker"),
                        rs.getDouble("price"),
                        rs.getDouble("volatility_rate"),
                        type
                ) {};
                assets.add(asset);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return assets;
    }


    public Asset getAssetByTicker(String ticker) {
        String sql = "SELECT * FROM assets WHERE ticker = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ticker);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AssetType type = AssetType.valueOf(rs.getString("asset_type"));
                return new Asset(
                        rs.getString("name"),
                        rs.getString("ticker"),
                        rs.getDouble("price"),
                        rs.getDouble("volatility_rate"),
                        type
                ) {};
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    public boolean createAsset(Asset asset) {
        String sql = """
            INSERT INTO assets (name, ticker, price, volatility_rate, asset_type)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asset.getName());
            stmt.setString(2, asset.getTicker());
            stmt.setDouble(3, asset.getPrice());
            stmt.setDouble(4, asset.getVolatilityRate());
            stmt.setString(5, asset.getType().name());

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveAllAssets(List<Asset> assets) {
        String sql = """
            INSERT INTO assets (name, ticker, price, volatility_rate, asset_type)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                price = VALUES(price),
                volatility_rate = VALUES(volatility_rate),
                asset_type = VALUES(asset_type)
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (Asset asset : assets) {
                stmt.setString(1, asset.getName());
                stmt.setString(2, asset.getTicker());
                stmt.setDouble(3, asset.getPrice());
                stmt.setDouble(4, asset.getVolatilityRate());
                stmt.setString(5, asset.getType().name());
                stmt.addBatch();
            }

            int[] rowsAffected = stmt.executeBatch();
            for (int count : rowsAffected) {
                if (count == Statement.EXECUTE_FAILED) {
                    return false;
                }
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}