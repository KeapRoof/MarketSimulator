package com.market.transactions;

import com.market.assets.Asset;
import com.market.assets.Stock;
import com.market.exceptions.InsufficientBalanceException;
import com.market.exceptions.MarketClosedException;
import com.market.users.User;
import com.market.wallet.Wallet;

import java.math.BigDecimal;
import java.time.LocalTime;

public class Transaction {

    /**
     * Valide que le type de wallet correspond au type d'asset
     */
    private static void validateWalletType(Asset asset, Wallet wallet) {
        if (wallet.getAssetType() != asset.getType()) {
            throw new IllegalArgumentException(
                    "Impossible d'acheter un asset de type " + asset.getType() +
                            " avec un Wallet de type " + wallet.getAssetType()
            );
        }
    }

    /**
     * Vérifie que le marché est ouvert (uniquement pour les stocks)
     */
    private static void validateMarketOpen(Asset asset) throws MarketClosedException {
        if (asset instanceof Stock) {
            Stock stock = (Stock) asset;
            LocalTime now = LocalTime.now();

            if (stock.getMarketOpenTime() != null && stock.getMarketCloseTime() != null) {
                if (now.isBefore(stock.getMarketOpenTime()) || now.isAfter(stock.getMarketCloseTime())) {
                    throw new MarketClosedException(
                            "Le marché est actuellement fermé pour " + asset.getName() +
                                    ". Heures d'ouverture: " + stock.getMarketOpenTime() + " - " + stock.getMarketCloseTime()
                    );
                }
            }
        }
    }

    /**
     * Achète un asset et l'ajoute au wallet
     *
     * @param user         L'utilisateur qui effectue l'achat
     * @param asset        L'asset à acheter
     * @param wallet       Le wallet dans lequel ajouter l'asset
     * @param quantity     La quantité à acheter
     * @param currentPrice Le prix actuel de l'asset (prix d'achat)
     * @throws InsufficientBalanceException Si l'utilisateur n'a pas assez d'argent
     * @throws MarketClosedException        Si le marché est fermé (pour les stocks)
     */
    public void buy(User user, Asset asset, Wallet wallet, double quantity, double currentPrice)
            throws InsufficientBalanceException, MarketClosedException {

        // 1. Vérifier que le marché est ouvert (stocks uniquement)
        validateMarketOpen(asset);

        // 2. Vérifier la correspondance entre le type de wallet et le type d'asset
        validateWalletType(asset, wallet);

        // 3. Calculer le montant total de l'achat
        double totalPrice = currentPrice * quantity;

        // 4. Vérifier si l'utilisateur a assez de balance
        if (user.getBalance().doubleValue() < totalPrice) {
            throw new InsufficientBalanceException(
                    "Balance insuffisante. Requis: " + totalPrice + ", Disponible: " + user.getBalance()
            );
        }

        // 5. Déduire le montant de la balance de l'utilisateur
        user.setBalance(user.getBalance().subtract(BigDecimal.valueOf(totalPrice)));

        // 6. Ajouter la position au wallet (avec quantité et prix d'achat)
        wallet.addPosition(asset, quantity, currentPrice);
    }

    /**
     * Vend un asset depuis le wallet
     *
     * @param user         L'utilisateur qui effectue la vente
     * @param asset        L'asset à vendre
     * @param wallet       Le wallet depuis lequel vendre l'asset
     * @param quantity     La quantité à vendre
     * @param currentPrice Le prix actuel de l'asset (prix de vente)
     * @throws MarketClosedException    Si le marché est fermé (pour les stocks)
     * @throws IllegalArgumentException Si la quantité est insuffisante dans le wallet
     */
    public void sell(User user, Asset asset, Wallet wallet, double quantity, double currentPrice)
            throws MarketClosedException {

        // 1. Vérifier que le marché est ouvert (stocks uniquement)
        validateMarketOpen(asset);

        // 2. Vérifier la correspondance entre le type de wallet et le type d'asset
        validateWalletType(asset, wallet);

        // 3. Vérifier que l'utilisateur possède cet asset
        if (!wallet.hasAsset(asset)) {
            throw new IllegalArgumentException(
                    "Vous ne possédez pas l'asset " + asset.getTicker() + " dans ce wallet"
            );
        }

        // 4. Vérifier que la quantité est suffisante
        var position = wallet.findPositionByAsset(asset);
        if (position.isEmpty() || position.get().getQuantity() < quantity) {
            double available = position.map(p -> p.getQuantity()).orElse(0.0);
            throw new IllegalArgumentException(
                    "Quantité insuffisante. Demandé: " + quantity + ", Disponible: " + available
            );
        }

        // 5. Calculer le montant de la vente
        double totalAmount = currentPrice * quantity;

        // 6. Ajouter le montant à la balance de l'utilisateur
        user.setBalance(user.getBalance().add(BigDecimal.valueOf(totalAmount)));

        // 7. Retirer la quantité du wallet
        wallet.removeFromPosition(asset, quantity);
    }

    /**
     * Surcharge pour acheter avec le prix actuel de l'asset
     */
    public void buy(User user, Asset asset, Wallet wallet, double quantity)
            throws InsufficientBalanceException, MarketClosedException {
        buy(user, asset, wallet, quantity, asset.getPrice());
    }

    /**
     * Surcharge pour vendre avec le prix actuel de l'asset
     */
    public void sell(User user, Asset asset, Wallet wallet, double quantity)
            throws MarketClosedException {
        sell(user, asset, wallet, quantity, asset.getPrice());
    }
}