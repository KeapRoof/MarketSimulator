package com.market.users.auth;

import com.market.db.dao.UserDAO;
import com.market.users.User;

import java.util.Optional;

/**
 * Service d'authentification et de gestion des sessions
 */
public class AuthService {
    private final UserDAO userDAO;
    private User currentUser;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.currentUser = null;
    }

    /**
     * Authentifie un utilisateur avec username et password
     *
     * @return true si l'authentification réussit
     */
    public boolean authenticate(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }

        Optional<User> userOpt = userDAO.findByUsername(username);

        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();

        if (user.getPassword().equals(password)) {
            this.currentUser = user;
            return true;
        }

        return false;
    }

    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        this.currentUser = null;
    }

    /**
     * Vérifie si un utilisateur est connecté
     */
    public boolean isAuthenticated() {
        return this.currentUser != null;
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     *
     * @return l'utilisateur connecté ou null
     */
    public User getCurrentUser() {
        return this.currentUser;
    }

    /**
     * Récupère l'ID de l'utilisateur connecté
     *
     * @throws IllegalStateException si aucun utilisateur n'est connecté
     */
    public Long getCurrentUserId() {
        if (currentUser == null) {
            throw new IllegalStateException("Aucun utilisateur connecté");
        }
        return currentUser.getId();
    }

    /**
     * Inscrit un nouvel utilisateur
     *
     * @return true si l'inscription réussit
     */
    public boolean register(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }
        if (userDAO.usernameExists(username)) {
            return false;
        }
        User newUser = new User(username, password);
        User createdUser = userDAO.create(newUser);
        return createdUser != null && createdUser.getId() != null;
    }

    /**
     * Change le mot de passe de l'utilisateur connecté
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (!isAuthenticated()) {
            return false;
        }

        if (!currentUser.getPassword().equals(oldPassword)) {
            return false;
        }

        currentUser.setPassword(newPassword);
        return userDAO.update(currentUser);
    }
}