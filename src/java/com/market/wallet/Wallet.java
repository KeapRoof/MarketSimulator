package com.market.wallet;

import com.market.assets.Asset;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Wallet<T extends Asset> {
    private List<T> items = new ArrayList<>();

    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Impossible d'ajouter un actif nul.");
        }
        items.add(item);
    }

    public double getTotalValue() {
        return items.stream()
                .mapToDouble(Asset::getPrice)
                .sum();
    }

    public T getTopPerformer() {
        return items.stream()
                .max(Comparator.comparingDouble(Asset::getPrice))
                .orElse(null);
    }

    public List<T> getItems() {
        return new ArrayList<>(items);
    }
}