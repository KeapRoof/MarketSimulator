package com.market.users;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User {
    private Long id;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private BigDecimal balance;

    // Constructeur par défaut
    public User() {
        this.balance = new BigDecimal("0.00");
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur avec paramètres (sans id car auto-généré)
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.balance = new BigDecimal("0.00");
        this.createdAt = LocalDateTime.now();
    }

    // Constructeur complet (pour récupération depuis la base)
    public User(Long id, String username, String password, LocalDateTime createdAt, BigDecimal balance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
        this.balance = balance;
    }

    // Getters
    public Long getId() {
        return id;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", createdAt=" + createdAt +
                ", balance=" + balance +
                '}';
    }
}