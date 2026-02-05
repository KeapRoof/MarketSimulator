package com.market.exceptions;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MarketClosedException extends Exception {

    private static final String LOG_FILE = "application.log";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MarketClosedException(String message) {
        super(message);
        logException(message, null);
    }

    public MarketClosedException(String message, Throwable cause) {
        super(message, cause);
        logException(message, cause);
    }

    private void logException(String message, Throwable cause) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("=====================================");
            pw.println("Date: " + LocalDateTime.now().format(DATE_FORMATTER));
            pw.println("Exception: " + this.getClass().getName());
            pw.println("Message: " + message);

            if (cause != null) {
                pw.println("Cause: " + cause.getMessage());
                pw.print("Stack trace: ");
                cause.printStackTrace(pw);
            } else {
                pw.print("Stack trace: ");
                this.printStackTrace(pw);
            }

            pw.println("=====================================");
            pw.println();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du log: " + e.getMessage());
        }
    }
}