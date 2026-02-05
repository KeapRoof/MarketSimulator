package com.market.db.dao;

import com.market.users.User;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAO {

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getBigDecimal("balance")
        );
    }

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

    public Optional<User> findByUsername(String username) {
        return Db.find("SELECT * FROM users WHERE username = ?", this::mapUser, username);
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, balance = ? WHERE id = ?";
        return Db.update(sql, user.getUsername(), user.getPassword(), user.getBalance(), user.getId()) > 0;
    }

    public boolean updateBalance(Long userId, BigDecimal newBalance) {
        return Db.update("UPDATE users SET balance = ? WHERE id = ?", newBalance, userId) > 0;
    }

    public boolean addBalance(Long userId, BigDecimal amount) {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        return Db.update(sql, amount, userId) > 0;
    }

    public boolean withdrawBalance(Long userId, BigDecimal amount) {
        String sql = "UPDATE users SET balance = balance - ? WHERE id = ? AND balance >= ?";
        return Db.update(sql, amount, userId, amount) > 0;
    }

    public boolean usernameExists(String username) {
        return Db.exists("SELECT 1 FROM users WHERE username = ?", username);
    }
}