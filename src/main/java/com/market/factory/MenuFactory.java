package com.market.factory;

import com.market.db.dao.UserDAO;
import com.market.db.dao.WalletDAO;
import com.market.handlers.BalanceHandler;
import com.market.handlers.MarketHandler;
import com.market.handlers.TransactionHandler;
import com.market.menu.Menu;
import com.market.services.TransactionService;
import com.market.transactions.Transaction;
import com.market.views.WalletView;

public class MenuFactory {
    public static Menu createMenu() {
        UserDAO userDAO = new UserDAO();
        WalletDAO walletDAO = new WalletDAO();
        WalletView walletView = new WalletView(walletDAO);

        TransactionService transactionService = new TransactionService(new Transaction(), userDAO, walletDAO);
        TransactionHandler transactionHandler = new TransactionHandler(transactionService, walletView);

        return new Menu(
                transactionHandler,
                walletView,
                new BalanceHandler(userDAO),
                new MarketHandler()
        );
    }
}