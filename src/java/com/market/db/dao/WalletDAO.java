package com.market.db.dao;

import com.market.commons.enums.AssetType;
import com.market.wallet.Position;
import com.market.wallet.Wallet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class WalletDAO {

    private final PositionDAO positionDAO = new PositionDAO();

    /**
     * Crée un wallet pour un utilisateur et un type d'asset
     * Retourne le wallet créé avec son ID
     */
    public Wallet createWallet(Long userId, AssetType assetType) {
        String sql = """
                    INSERT INTO wallets (user_id, asset_type)
                    VALUES (?, ?)
                """;

        Long walletId = Db.insert(sql, userId, assetType.name());
        // On passe une ArrayList vide et modifiable
        return new Wallet(walletId, userId, assetType, new java.util.ArrayList<>());
    }

    /**
     * Récupère ou crée un wallet pour un utilisateur et un type d'asset
     */
    public Wallet getOrCreateWallet(Long userId, AssetType assetType) {
        Optional<Wallet> existing = findWallet(userId, assetType);
        return existing.orElseGet(() -> createWallet(userId, assetType));
    }

    /**
     * Trouve un wallet par user_id et asset_type
     */
    public Optional<Wallet> findWallet(Long userId, AssetType assetType) {
        String sql = "SELECT * FROM wallets WHERE user_id = ? AND asset_type = ?";
        Wallet wallet = Db.get(sql, this::mapWallet, userId, assetType.name());

        if (wallet != null) {
            // Charger les positions
            List<Position> positions = positionDAO.findByWalletId(wallet.getId());
            wallet.setPositions(positions);
            return Optional.of(wallet);
        }

        return Optional.empty();
    }

    /**
     * Trouve un wallet par son ID
     */
    public Optional<Wallet> findById(Long walletId) {
        String sql = "SELECT * FROM wallets WHERE id = ?";
        Wallet wallet = Db.get(sql, this::mapWallet, walletId);

        if (wallet != null) {
            List<Position> positions = positionDAO.findByWalletId(walletId);
            wallet.setPositions(positions);
            return Optional.of(wallet);
        }

        return Optional.empty();
    }

    /**
     * Récupère tous les wallets d'un utilisateur
     */
    public List<Wallet> findAllByUser(Long userId) {
        String sql = "SELECT * FROM wallets WHERE user_id = ? ORDER BY asset_type";
        List<Wallet> wallets = Db.list(sql, this::mapWallet, userId);

        // Charger les positions pour chaque wallet
        for (Wallet wallet : wallets) {
            List<Position> positions = positionDAO.findByWalletId(wallet.getId());
            wallet.setPositions(positions);
        }

        return wallets;
    }

    /**
     * Sauvegarde un wallet complet (wallet + positions)
     * Met à jour si existe, crée sinon
     */
    public Wallet save(Wallet wallet) {
        return Db.transaction(conn -> {
            Long walletId = wallet.getId();

            if (walletId == null) {
                String insertSql = "INSERT INTO wallets (user_id, asset_type) VALUES (?, ?)";
                walletId = Db.insert(insertSql, wallet.getUserId(), wallet.getAssetType().name());
                wallet.setId(walletId);
            } else {
                if (!exists(walletId)) {
                    throw new SQLException("Wallet introuvable ID: " + walletId);
                }
            }
            String selectTickersSql = """
                        SELECT a.ticker 
                        FROM wallet_positions wp 
                        JOIN assets a ON wp.asset_id = a.id 
                        WHERE wp.wallet_id = ?
                    """;

            List<String> dbTickers = Db.list(
                    selectTickersSql,
                    rs -> rs.getString("ticker"),
                    walletId
            );

            // Liste des tickers actuellement en mémoire (Java)
            List<String> javaTickers = wallet.getPositions().stream()
                    .map(p -> p.getAsset().getTicker())
                    .toList();

            for (String dbTicker : dbTickers) {
                // Si le ticker présent en BDD n'est plus dans la liste Java...
                boolean stillExists = javaTickers.stream()
                        .anyMatch(t -> t.equalsIgnoreCase(dbTicker));

                if (!stillExists) {
                    // On utilise une sous-requête pour trouver l'asset_id à partir du ticker
                    String deleteSql = """
                                DELETE FROM wallet_positions 
                                WHERE wallet_id = ? 
                                AND asset_id = (SELECT id FROM assets WHERE ticker = ?)
                            """;
                    Db.update(deleteSql, walletId, dbTicker);
                }
            }

            // 3. Sauvegarde des positions restantes
            for (Position position : wallet.getPositions()) {
                position.setWalletId(walletId);
                positionDAO.save(position);
            }

            return wallet;
        });
    }

    /**
     * Vérifie si un wallet existe
     */
    public boolean exists(Long walletId) {
        return Db.exists("SELECT 1 FROM wallets WHERE id = ?", walletId);
    }

    // ==================== Méthodes privées ====================

    private Wallet mapWallet(ResultSet rs) throws SQLException {
        return new Wallet(
                rs.getLong("id"),
                rs.getLong("user_id"),
                AssetType.valueOf(rs.getString("asset_type")),
                null // Les positions seront chargées séparément
        );
    }
}