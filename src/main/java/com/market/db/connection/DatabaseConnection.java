package com.market.db.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:mariadb://localhost:3306/market";
    private static final String DB_USER = "market_user";
    private static final String DB_PASS = "market_pass";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            // Charger le driver MariaDB
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MariaDB non trouvé. Ajoutez la dépendance mariadb-java-client", e);
        }

        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }
}