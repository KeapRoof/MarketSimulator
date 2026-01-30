package com.market.db.dao;

import com.market.db.connection.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Db {

    /**
     * Exécute une requête SQL avec gestion automatique des ressources
     */
    public static <T> T query(SQLFunction<T> function) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return function.apply(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur Database : " + e.getMessage(), e);
        }
    }

    /**
     * Exécute une requête UPDATE/INSERT/DELETE
     * Usage: Db.update("INSERT INTO users(name) VALUES(?)", "Alice");
     */
    public static int update(String sql, Object... params) {
        return query(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                fillStatement(stmt, params);
                return stmt.executeUpdate();
            }
        });
    }

    /**
     * Exécute un INSERT et retourne l'ID généré
     * Usage: Long id = Db.insert("INSERT INTO users(name) VALUES(?)", "Alice");
     */
    public static Long insert(String sql, Object... params) {
        return query(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                fillStatement(stmt, params);
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                    throw new SQLException("Échec de l'insertion, aucun ID généré");
                }
            }
        });
    }

    /**
     * Récupère un seul objet (retourne null si non trouvé)
     * Usage: User u = Db.get("SELECT * FROM users WHERE id = ?", rs -> new User(rs), 1);
     */
    public static <T> T get(String sql, ResultSetMapper<T> mapper, Object... params) {
        return query(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                fillStatement(stmt, params);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return mapper.map(rs);
                    return null;
                }
            }
        });
    }

    /**
     * Récupère un seul objet dans un Optional
     * Usage: Optional<User> u = Db.find("SELECT * FROM users WHERE id = ?", rs -> new User(rs), 1);
     */
    public static <T> Optional<T> find(String sql, ResultSetMapper<T> mapper, Object... params) {
        return Optional.ofNullable(get(sql, mapper, params));
    }

    /**
     * Récupère une liste d'objets
     * Usage: List<User> users = Db.list("SELECT * FROM users WHERE active = ?", rs -> new User(rs), true);
     */
    public static <T> List<T> list(String sql, ResultSetMapper<T> mapper, Object... params) {
        return query(conn -> {
            List<T> results = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                fillStatement(stmt, params);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapper.map(rs));
                    }
                }
            }
            return results;
        });
    }

    /**
     * Compte le nombre de résultats
     * Usage: long count = Db.count("SELECT COUNT(*) FROM users WHERE active = ?", true);
     */
    public static long count(String sql, Object... params) {
        return query(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                fillStatement(stmt, params);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                    return 0L;
                }
            }
        });
    }

    /**
     * Vérifie si un résultat existe
     * Usage: boolean exists = Db.exists("SELECT 1 FROM users WHERE email = ?", "test@test.com");
     */
    public static boolean exists(String sql, Object... params) {
        return query(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                fillStatement(stmt, params);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    /**
     * Exécute plusieurs requêtes dans une transaction
     * Usage:
     * Db.transaction(conn -> {
     * // Plusieurs opérations ici
     * return true;
     * });
     */
    public static <T> T transaction(SQLFunction<T> function) {
        return query(conn -> {
            boolean autoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                T result = function.apply(conn);
                conn.commit();
                return result;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        });
    }

    /**
     * Remplit un PreparedStatement avec les paramètres
     */
    private static void fillStatement(PreparedStatement stmt, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }

    @FunctionalInterface
    public interface SQLFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}