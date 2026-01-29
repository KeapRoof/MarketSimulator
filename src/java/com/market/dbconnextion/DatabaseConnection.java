package com.market.dbconnextion;

import java.sql.*;

public class DatabaseConnection {

    private static final String DB_URL =
            "jdbc:mariadb://localhost:3306/market";
    private static final String DB_USER = "market_user";
    private static final String DB_PASS = "market_pass";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private static boolean authenticate(String username, String password) {

        String sql = """
        SELECT password
        FROM users
        WHERE username = ?
    """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                return false;
            }

            String storedPassword = rs.getString("password");
            return storedPassword.equals(password);

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}