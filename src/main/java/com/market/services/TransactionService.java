package com.market.services;

import com.market.assets.Asset;
import com.market.db.dao.UserDAO;
import com.market.db.dao.WalletDAO;
import com.market.exceptions.InsufficientBalanceException;
import com.market.exceptions.MarketClosedException;
import com.market.transactions.Transaction;
import com.market.users.User;
import com.market.wallet.Wallet;

import java.util.logging.Logger;

public class TransactionService {
    private static final Logger LOGGER = Logger.getLogger(TransactionService.class.getName());

    private final Transaction transaction;
    private final UserDAO userDAO;
    private final WalletDAO walletDAO;

    public TransactionService(Transaction transaction, UserDAO userDAO, WalletDAO walletDAO) {
        this.transaction = transaction;
        this.userDAO = userDAO;
        this.walletDAO = walletDAO;
    }

    public void executeBuyOrder(User user, Asset asset, double quantity)
            throws InsufficientBalanceException, MarketClosedException {

        try {
            LOGGER.info(String.format("[BUY] User=%d, Asset=%s, Quantity=%.4f, Price=%.2f",
                    user.getId(), asset.getTicker(), quantity, asset.getPrice()));

            // 1. Récupérer ou créer le wallet approprié
            Wallet wallet = walletDAO.getOrCreateWallet(user.getId(), asset.getType());

            // 2. Exécuter la transaction (modification en mémoire)
            transaction.buy(user, asset, wallet, quantity);

            // 3. Sauvegarder en base de données (transaction DB)
            boolean success = saveTransaction(user, wallet);

            if (success) {
                LOGGER.info("[SUCCESS] Transaction d'achat sauvegardée avec succès.");
            } else {
                LOGGER.warning("[WARNING] Échec de la sauvegarde de la transaction.");
                throw new RuntimeException("Échec de la sauvegarde de la transaction");
            }

        } catch (InsufficientBalanceException | MarketClosedException e) {
            LOGGER.warning(String.format("[FAILED] Transaction échouée: %s", e.getMessage()));
            throw e;
        } catch (Exception e) {
            LOGGER.severe(String.format("[ERROR] Erreur technique lors de la transaction: %s", e.getMessage()));
            throw new RuntimeException("Erreur technique lors de la transaction", e);
        }
    }

    public void executeSellOrder(User user, Asset asset, double quantity)
            throws MarketClosedException {

        try {
            LOGGER.info(String.format("[SELL] User=%d, Asset=%s, Quantity=%.4f, Price=%.2f",
                    user.getId(), asset.getTicker(), quantity, asset.getPrice()));

            // 1. Récupérer le wallet approprié
            Wallet wallet = walletDAO.findWallet(user.getId(), asset.getType())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Vous ne possédez pas de wallet de type " + asset.getType()));

            // 2. Exécuter la transaction (modification en mémoire)
            transaction.sell(user, asset, wallet, quantity);

            // 3. Sauvegarder en base de données
            boolean success = saveTransaction(user, wallet);

            if (success) {
                LOGGER.info("[SUCCESS] Transaction de vente sauvegardée avec succès.");
            } else {
                LOGGER.warning("[WARNING] Échec de la sauvegarde de la transaction.");
                throw new RuntimeException("Échec de la sauvegarde de la transaction");
            }

        } catch (MarketClosedException | IllegalArgumentException e) {
            LOGGER.warning(String.format("[FAILED] Transaction échouée: %s", e.getMessage()));
            throw e;
        } catch (Exception e) {
            LOGGER.severe(String.format("[ERROR] Erreur technique lors de la transaction: %s", e.getMessage()));
            throw new RuntimeException("Erreur technique lors de la transaction", e);
        }
    }

    private boolean saveTransaction(User user, Wallet wallet) {
        try {
            boolean userUpdated = userDAO.updateBalance(user.getId(), user.getBalance());
            Wallet savedWallet = walletDAO.save(wallet);
            return userUpdated && savedWallet != null;
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la sauvegarde en DB: " + e.getMessage());
            return false;
        }
    }

    public Wallet getUserWallet(Long userId, Asset asset) {
        return walletDAO.findWallet(userId, asset.getType())
                .orElse(null);
    }

    public boolean canAfford(User user, Asset asset, double quantity) {
        double totalCost = asset.getPrice() * quantity;
        return user.getBalance().doubleValue() >= totalCost;
    }

    public boolean hasEnoughAsset(User user, Asset asset, double quantity) {
        return walletDAO.findWallet(user.getId(), asset.getType())
                .flatMap(wallet -> wallet.findPositionByAsset(asset))
                .map(position -> position.getQuantity() >= quantity)
                .orElse(false);
    }

    public double calculatePurchaseAmount(Asset asset, double quantity) {
        return asset.getPrice() * quantity;
    }

    public double calculateSaleAmount(Asset asset, double quantity) {
        return asset.getPrice() * quantity;
    }
}