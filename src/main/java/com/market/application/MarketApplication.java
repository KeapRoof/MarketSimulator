package com.market.application;

import com.market.factory.MenuFactory;
import com.market.menu.Menu;
import com.market.users.auth.AuthService;

import java.util.Scanner;

public class MarketApplication {
    private final Scanner scanner = new Scanner(System.in);
    private final AuthService authService = new AuthService();

    public void run() {
        Menu menu = MenuFactory.createMenu();

        while (!login()) {
            System.out.println("❌ Invalid username or password. Please try again.\n");
        }

        System.out.println("\n✅ Authentication successful!");
        menu.showMenu(scanner, authService.getCurrentUser());

        scanner.close();
    }

    private boolean login() {
        System.out.println("--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        return authService.authenticate(username, password);
    }
}