package com.market.db.dao;

import com.market.assets.Asset;
import com.market.commons.enums.AssetType;
import com.market.wallet.Position;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PositionDAO {

    /**
     * Sauvegarde ou met à jour une position
     * Retourne la position avec son ID
     */
    public Position save(Position position) {
        // 1. D'abord sauvegarder l'asset si nécessaire
        AssetDAO assetDAO = new AssetDAO();
        assetDAO.saveAsset(position.getAsset());

        // 2. Si l'ID est null, on vérifie quand même en DB si cette position existe déjà
        // pour cet utilisateur et cet actif (sécurité contre les doublons)
        if (position.getId() == null) {
            Optional<Position> existing = findByWalletAndAsset(
                    position.getWalletId(),
                    position.getAsset().getTicker()
            );

            if (existing.isPresent()) {
                // Si elle existe, on récupère l'ID et on fait un update
                position.setId(existing.get().getId());
                update(position);
                return position;
            } else {
                // Si elle n'existe vraiment pas, on insère
                return insert(position);
            }
        } else {
            // L'ID n'est pas null, c'est une mise à jour classique
            update(position);
            return position;
        }
    }

    /**
     * Insère une nouvelle position
     */
    private Position insert(Position position) {
        String sql = """
                    INSERT INTO wallet_positions (wallet_id, asset_id, quantity, average_purchase_price)
                    VALUES (?, ?, ?, ?)
                """;

        Long assetId = getAssetId(position.getAsset().getTicker());

        Long positionId = Db.insert(sql,
                position.getWalletId(),
                assetId,
                position.getQuantity(),
                position.getAveragePurchasePrice()
        );

        position.setId(positionId);
        return position;
    }

    /**
     * Met à jour une position existante
     */
    private boolean update(Position position) {
        String sql = """
                    UPDATE wallet_positions 
                    SET quantity = ?, 
                        average_purchase_price = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                """;

        return Db.update(sql,
                position.getQuantity(),
                position.getAveragePurchasePrice(),
                position.getId()
        ) > 0;
    }

    /**
     * Trouve une position par son ID
     */
    public Optional<Position> findById(Long positionId) {
        String sql = """
                    SELECT wp.*, a.*
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.id = ?
                """;

        Position position = Db.get(sql, this::mapPosition, positionId);
        return Optional.ofNullable(position);
    }

    /**
     * Trouve toutes les positions d'un wallet
     */
    public List<Position> findByWalletId(Long walletId) {
        String sql = """
                    SELECT wp.*, a.*
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ?
                    ORDER BY wp.updated_at DESC
                """;

        return Db.list(sql, this::mapPosition, walletId).stream()
                .filter(p -> p != null) // Filtrer les positions avec assets manquants
                .toList();
    }

    /**
     * Trouve une position spécifique par wallet et asset
     */
    public Optional<Position> findByWalletAndAsset(Long walletId, String assetTicker) {
        String sql = """
                    SELECT wp.*, a.*
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ? AND a.ticker = ?
                """;

        Position position = Db.get(sql, this::mapPosition, walletId, assetTicker);
        return Optional.ofNullable(position);
    }

    /**
     * Trouve toutes les positions d'un asset spécifique (tous wallets confondus)
     */
    public List<Position> findByAsset(String assetTicker) {
        String sql = """
                    SELECT wp.*, a.*
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE a.ticker = ?
                """;

        return Db.list(sql, this::mapPosition, assetTicker).stream()
                .filter(p -> p != null)
                .toList();
    }

    /**
     * Ajoute ou met à jour une position (upsert)
     * Si la position existe, met à jour quantité et prix moyen
     */
    public Position addOrUpdatePosition(Long walletId, Asset asset, double quantity, double purchasePrice) {
        Optional<Position> existing = findByWalletAndAsset(walletId, asset.getTicker());

        if (existing.isPresent()) {
            // Mettre à jour avec calcul du prix moyen pondéré
            Position pos = existing.get();
            double totalCost = (pos.getQuantity() * pos.getAveragePurchasePrice()) +
                    (quantity * purchasePrice);
            double newQuantity = pos.getQuantity() + quantity;
            double newAvgPrice = totalCost / newQuantity;

            pos.setQuantity(newQuantity);
            pos.setAveragePurchasePrice(newAvgPrice);
            update(pos);
            return pos;
        } else {
            // Créer une nouvelle position
            Position newPos = new Position(null, walletId, asset, quantity, purchasePrice);
            return insert(newPos);
        }
    }

    /**
     * Retire une quantité d'une position
     * Si la quantité devient <= 0, supprime la position
     */
    public boolean removeQuantity(Long positionId, double quantity) {
        Optional<Position> positionOpt = findById(positionId);

        if (positionOpt.isEmpty()) {
            return false;
        }

        Position position = positionOpt.get();
        double newQuantity = position.getQuantity() - quantity;

        if (newQuantity <= 0) {
            return delete(positionId);
        } else {
            position.setQuantity(newQuantity);
            return update(position);
        }
    }

    /**
     * Met à jour uniquement la quantité d'une position
     */
    public boolean updateQuantity(Long positionId, double newQuantity) {
        if (newQuantity <= 0) {
            return delete(positionId);
        }

        String sql = """
                    UPDATE wallet_positions 
                    SET quantity = ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                """;

        return Db.update(sql, newQuantity, positionId) > 0;
    }

    /**
     * Supprime une position par son ID
     */
    public boolean delete(Long positionId) {
        return Db.update("DELETE FROM wallet_positions WHERE id = ?", positionId) > 0;
    }

    /**
     * Supprime une position par wallet et asset
     */
    public boolean deleteByWalletAndAsset(Long walletId, String assetTicker) {
        String sql = """
                    DELETE FROM wallet_positions 
                    WHERE wallet_id = ? AND asset_id = (
                        SELECT id FROM assets WHERE ticker = ?
                    )
                """;

        return Db.update(sql, walletId, assetTicker) > 0;
    }

    /**
     * Supprime toutes les positions d'un wallet
     */
    public boolean deleteAllByWallet(Long walletId) {
        return Db.update("DELETE FROM wallet_positions WHERE wallet_id = ?", walletId) > 0;
    }

    /**
     * Compte le nombre de positions dans un wallet
     */
    public long countByWallet(Long walletId) {
        return Db.count("SELECT COUNT(*) FROM wallet_positions WHERE wallet_id = ?", walletId);
    }

    /**
     * Vérifie si une position existe
     */
    public boolean exists(Long positionId) {
        return Db.exists("SELECT 1 FROM wallet_positions WHERE id = ?", positionId);
    }

    /**
     * Vérifie si une position existe pour un wallet et un asset
     */
    public boolean exists(Long walletId, String assetTicker) {
        String sql = """
                    SELECT 1 FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ? AND a.ticker = ?
                """;
        return Db.exists(sql, walletId, assetTicker);
    }

    /**
     * Récupère les positions les plus performantes (par % de gain)
     */
    public List<Position> getTopPerformers(Long walletId, int limit) {
        String sql = """
                    SELECT wp.*, a.*,
                           (a.price - wp.average_purchase_price) / wp.average_purchase_price * 100 as profit_percent
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ?
                    ORDER BY profit_percent DESC
                    LIMIT ?
                """;

        return Db.list(sql, this::mapPosition, walletId, limit).stream()
                .filter(p -> p != null)
                .toList();
    }

    /**
     * Récupère les positions les moins performantes (par % de perte)
     */
    public List<Position> getWorstPerformers(Long walletId, int limit) {
        String sql = """
                    SELECT wp.*, a.*,
                           (a.price - wp.average_purchase_price) / wp.average_purchase_price * 100 as profit_percent
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ?
                    ORDER BY profit_percent ASC
                    LIMIT ?
                """;

        return Db.list(sql, this::mapPosition, walletId, limit).stream()
                .filter(p -> p != null)
                .toList();
    }

    /**
     * Récupère les positions profitables d'un wallet
     */
    public List<Position> getProfitablePositions(Long walletId) {
        String sql = """
                    SELECT wp.*, a.*
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ? AND a.price > wp.average_purchase_price
                """;

        return Db.list(sql, this::mapPosition, walletId).stream()
                .filter(p -> p != null)
                .toList();
    }

    /**
     * Récupère les positions en perte d'un wallet
     */
    public List<Position> getLosingPositions(Long walletId) {
        String sql = """
                    SELECT wp.*, a.*
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ? AND a.price < wp.average_purchase_price
                """;

        return Db.list(sql, this::mapPosition, walletId).stream()
                .filter(p -> p != null)
                .toList();
    }

    /**
     * Calcule la valeur totale de toutes les positions d'un wallet
     */
    public double calculateTotalValue(Long walletId) {
        String sql = """
                    SELECT COALESCE(SUM(wp.quantity * a.price), 0) as total_value
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ?
                """;

        Double value = Db.get(sql, rs -> rs.getDouble("total_value"), walletId);
        return value != null ? value : 0.0;
    }

    /**
     * Calcule le profit/perte total d'un wallet
     */
    public double calculateTotalProfitLoss(Long walletId) {
        String sql = """
                    SELECT COALESCE(
                        SUM(wp.quantity * (a.price - wp.average_purchase_price)), 
                        0
                    ) as profit_loss
                    FROM wallet_positions wp
                    JOIN assets a ON wp.asset_id = a.id
                    WHERE wp.wallet_id = ?
                """;

        Double profitLoss = Db.get(sql, rs -> rs.getDouble("profit_loss"), walletId);
        return profitLoss != null ? profitLoss : 0.0;
    }

    // ==================== Méthodes privées ====================

    private Position mapPosition(ResultSet rs) throws SQLException {
        try {
            // Mapper l'asset directement depuis le ResultSet (évite une query supplémentaire)
            AssetType type = AssetType.valueOf(rs.getString("asset_type"));
            Asset asset;

            if (type == AssetType.Stock) {
                asset = new com.market.assets.Stock(
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
                asset = new com.market.assets.Crypto(
                        rs.getString("name"),
                        rs.getString("ticker"),
                        rs.getDouble("price"),
                        rs.getString("blockchain")
                );
            } else {
                System.err.println("ATTENTION: Type d'asset inconnu: " + type);
                return null;
            }

            return new Position(
                    rs.getLong("id"),
                    rs.getLong("wallet_id"),
                    asset,
                    rs.getDouble("quantity"),
                    rs.getDouble("average_purchase_price")
            );
        } catch (Exception e) {
            System.err.println("ERREUR lors du mapping de position: " + e.getMessage());
            return null;
        }
    }

    private Long getAssetId(String ticker) {
        String sql = "SELECT id FROM assets WHERE ticker = ?";
        Long id = Db.get(sql, rs -> rs.getLong("id"), ticker);
        if (id == null) {
            throw new RuntimeException("Asset non trouvé: " + ticker);
        }
        return id;
    }
}