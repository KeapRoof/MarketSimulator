import com.market.db.dao.UserDAO;
import com.market.db.dao.WalletDAO;
import com.market.handlers.TransactionHandler;
import com.market.menu.Menu;
import com.market.services.TransactionService;
import com.market.transactions.Transaction;
import com.market.users.auth.AuthService;
import com.market.views.WalletView;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialisation des DAOs
        UserDAO userDAO = new UserDAO();
        WalletDAO walletDAO = new WalletDAO();

        // Initialisation de la vue Wallet
        WalletView walletView = new WalletView(walletDAO);

        // Initialisation de la couche Transaction
        Transaction transaction = new Transaction();
        TransactionService transactionService = new TransactionService(transaction, userDAO, walletDAO);
        TransactionHandler transactionHandler = new TransactionHandler(transactionService, walletView);

        // Initialisation du Menu
        Menu menu = new Menu(transactionHandler, walletView);

        // Authentification
        AuthService authService = new AuthService();
        boolean exit = false;

        while (!exit) {
            System.out.print("Username (or type 'exit' to quit): ");
            String username = scanner.nextLine();

            if (username.equalsIgnoreCase("exit")) {
                System.out.println("Exiting application. Goodbye!");
                break;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (authService.authenticate(username, password)) {
                System.out.println("Authentication successful!");
                menu.showMenu(scanner, authService.getCurrentUser());
                exit = true;
            } else {
                System.out.println("Authentication failed. Invalid username or password.");
                System.out.println("Please try again or type 'exit' to quit.\n");
            }
        }

        scanner.close();
    }
}