package com.market.db.dao;

import com.market.users.User;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    /**
     * Mapper pour convertir un ResultSet en User
     */
    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getBigDecimal("balance")
        );
    }

    /**
     * Crée un nouvel utilisateur
     */
    public User create(User user) {
        String sql = "INSERT INTO users (username, password, balance, created_at) VALUES (?, ?, ?, ?)";

        Long id = Db.insert(sql,
                user.getUsername(),
                user.getPassword(),
                user.getBalance(),
                java.sql.Timestamp.valueOf(user.getCreatedAt())
        );

        user.setId(id);
        return user;
    }

    /**
     * Trouve un utilisateur par son ID
     */
    public Optional<User> findById(Long id) {
        return Db.find("SELECT * FROM users WHERE id = ?", this::mapUser, id);
    }

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     */
    public Optional<User> findByUsername(String username) {
        return Db.find("SELECT * FROM users WHERE username = ?", this::mapUser, username);
    }

    /**
     * Récupère tous les utilisateurs
     */
    public List<User> findAll() {
        return Db.list("SELECT * FROM users", this::mapUser);
    }

    /**
     * Met à jour un utilisateur
     */
    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, balance = ? WHERE id = ?";
        return Db.update(sql, user.getUsername(), user.getPassword(), user.getBalance(), user.getId()) > 0;
    }

    /**
     * Met à jour le solde d'un utilisateur
     */
    public boolean updateBalance(Long userId, BigDecimal newBalance) {
        return Db.update("UPDATE users SET balance = ? WHERE id = ?", newBalance, userId) > 0;
    }

    /**
     * Supprime un utilisateur
     */
    public boolean delete(Long id) {
        return Db.update("DELETE FROM users WHERE id = ?", id) > 0;
    }

    /**
     * Vérifie si un nom d'utilisateur existe déjà
     */
    public boolean usernameExists(String username) {
        return Db.exists("SELECT 1 FROM users WHERE username = ?", username);
    }

    /**
     * Compte le nombre total d'utilisateurs
     */
    public long countAll() {
        return Db.count("SELECT COUNT(*) FROM users");
    }
}