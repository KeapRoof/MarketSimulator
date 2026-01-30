package com.market.transactions;

import com.market.assets.Asset;
import com.market.assets.Stock;
import com.market.commons.enums.AssetType;
import com.market.exceptions.InsufficientBalanceException;
import com.market.exceptions.MarketClosedException;
import com.market.exceptions.WalletTypeMismatchException;
import com.market.users.User;
import com.market.wallet.Wallet;

import java.time.LocalTime;

public class Transaction {

    public static <T extends Asset> void achat(User user, T asset, Wallet<T> wallet, int quantity)
            throws InsufficientBalanceException, WalletTypeMismatchException, MarketClosedException {

        // Vérifier que le marché est ouvert (uniquement pour les stocks)
        if (asset instanceof Stock) {
            Stock stock = (Stock) asset;
            LocalTime now = LocalTime.now();
            if (now.isBefore(stock.getMarketOpenTime()) || now.isAfter(stock.getMarketCloseTime())) {
                throw new MarketClosedException(
                        "Le marché est actuellement fermé pour " + asset.getName() +
                                ". Heures d'ouverture: " + stock.getMarketOpenTime() + " - " + stock.getMarketCloseTime()
                );
            }
        }

        // Vérifier la correspondance entre le type de wallet et le type d'asset
        validateWalletType(asset, wallet);

        // Calculer le montant total de l'achat
        double totalPrice = asset.getPrice() * quantity;

        // Vérifier si l'utilisateur a assez de balance
        if (user.getBalance().doubleValue() < totalPrice) {
            throw new InsufficientBalanceException(
                    "Balance insuffisante. Requis: " + totalPrice + ", Disponible: " + user.getBalance()
            );
        }

        // Déduire le montant de la balance de l'utilisateur
        user.setBalance(user.getBalance().subtract(java.math.BigDecimal.valueOf(totalPrice)));

        // Ajouter l'asset au wallet (quantity fois)
        for (int i = 0; i < quantity; i++) {
            wallet.add(asset);
        }
    }

    /**
     * Valide que le type de wallet correspond au type d'asset
     */
    private static <T extends Asset> void validateWalletType(T asset, Wallet<T> wallet)
            throws WalletTypeMismatchException {

        // Vérifier si le wallet contient déjà des items
        if (!wallet.getItems().isEmpty()) {
            // Comparer le type de l'asset avec le type du premier item du wallet
            AssetType walletType = wallet.getItems().get(0).getType();
            AssetType assetType = asset.getType();

            if (!walletType.equals(assetType)) {
                throw new WalletTypeMismatchException(
                        "Impossible d'acheter un asset de type " + assetType +
                                " avec un Wallet de type " + walletType
                );
            }
        }
    }
}