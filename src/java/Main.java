import com.market.users.auth.AuthService;
import menu.Menu;

import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        AuthService authService = new AuthService();
        if (authService.authenticate(username, password)) {
            System.out.println("Authentication successful!");
            Menu.showMenu(scanner);
        } else {
            System.out.println("Authentication failed. Invalid username or password.");
        }
    }
}