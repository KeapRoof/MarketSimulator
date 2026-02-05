package com.market.exceptions;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NegativeQuantityException extends IllegalArgumentException {

    private static final String LOG_FILE = "application.log";
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NegativeQuantityException(String message) {
        super(message);
        logException(message);
    }

    private void logException(String message) {
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("=====================================");
            pw.println("Date: " + LocalDateTime.now().format(DATE_FORMATTER));
            pw.println("Exception: " + this.getClass().getName());
            pw.println("Message: " + message);
            pw.print("Stack trace: ");
            this.printStackTrace(pw);
            pw.println("=====================================");
            pw.println();

        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du log: " + e.getMessage());
        }
    }
}