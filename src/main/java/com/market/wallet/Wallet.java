package com.market.wallet;

import com.market.assets.Asset;
import com.market.commons.enums.AssetType;
import com.market.exceptions.NegativeQuantityException;
import com.market.exceptions.NullAssetException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Wallet {
    private Long id;
    private Long userId;
    private AssetType assetType;
    private List<Position> positions = new ArrayList<>();

    // Constructeur pour création depuis la DB
    public Wallet(Long id, Long userId, AssetType assetType, List<Position> positions) {
        this.id = id;
        this.userId = userId;
        this.assetType = assetType;
        this.positions = (positions != null) ? new ArrayList<>(positions) : new ArrayList<>();
    }


    public void addPosition(Asset asset, double quantity, double purchasePrice) {
        if (asset == null) {
            throw new NullAssetException("Impossible d'ajouter un actif nul.");
        }
        if (quantity <= 0) {
            throw new NegativeQuantityException("La quantité doit être positive.");
        }

        Optional<Position> existing = findPositionByAsset(asset);

        if (existing.isPresent()) {
            // Mettre à jour la position existante
            Position pos = existing.get();
            double totalCost = (pos.getQuantity() * pos.getAveragePurchasePrice()) +
                    (quantity * purchasePrice);
            double newQuantity = pos.getQuantity() + quantity;
            double newAvgPrice = totalCost / newQuantity;

            pos.setQuantity(newQuantity);
            pos.setAveragePurchasePrice(newAvgPrice);
        } else {
            // Créer une nouvelle position
            Position newPosition = new Position(asset, quantity, purchasePrice);
            positions.add(newPosition);
        }
    }

    public boolean removeFromPosition(Asset asset, double quantity) {
        Optional<Position> existing = findPositionByAsset(asset);

        if (existing.isEmpty()) {
            return false;
        }

        Position pos = existing.get();
        double newQuantity = pos.getQuantity() - quantity;

        if (newQuantity <= 0) {
            positions.remove(pos);
        } else {
            pos.setQuantity(newQuantity);
        }

        return true;
    }

    public Optional<Position> findPositionByAsset(Asset asset) {
        return positions.stream()
                .filter(p -> p.getAsset().equals(asset))
                .findFirst();
    }

    public double getTotalValue() {
        return positions.stream()
                .mapToDouble(Position::getCurrentValue)
                .sum();
    }

    public double getTotalCost() {
        return positions.stream()
                .mapToDouble(Position::getTotalCost)
                .sum();
    }

    public double getTotalProfitLoss() {
        return positions.stream()
                .mapToDouble(Position::getProfitLoss)
                .sum();
    }

    public double getTotalProfitLossPercent() {
        double totalCost = getTotalCost();
        if (totalCost == 0) return 0.0;
        return (getTotalProfitLoss() / totalCost) * 100;
    }

    public int getPositionCount() {
        return positions.size();
    }

    public boolean isEmpty() {
        return positions.isEmpty();
    }

    public boolean hasAsset(Asset asset) {
        return findPositionByAsset(asset).isPresent();
    }

    public List<Position> getPositions() {
        return new ArrayList<>(positions);
    }

    // Getters et Setters

    public void setPositions(List<Position> positions) {
        this.positions = (positions != null) ? new ArrayList<>(positions) : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(AssetType assetType) {
        this.assetType = assetType;
    }

    @Override
    public String toString() {
        return String.format("Wallet{id=%d, userId=%d, type=%s, positions=%d, value=%.2f, P/L=%.2f%%}",
                id, userId, assetType, getPositionCount(), getTotalValue(), getTotalProfitLossPercent());
    }
}