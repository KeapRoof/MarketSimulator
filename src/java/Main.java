import com.market.assets.Crypto;
import com.market.assets.Stock;
import com.market.wallet.Wallet;

import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("========== DEBUT DE LA SIMULATION ==========");

        // 1. Création des actifs (Polymorphisme)
        Stock apple = new Stock("Apple Inc.", "AAPL", 175.0, 1.5f, LocalTime.of(9, 0), LocalTime.of(17, 30));
        Crypto bitcoin = new Crypto("Bitcoin", "BTC", 63000.0, "Blockchain Bitcoin");

        // 2. Test du Wallet Générique (Validation Point 2 du cours)
        // On crée un Wallet qui n'accepte QUE des Cryptos
        Wallet<Crypto> cryptoWallet = new Wallet<>();
        cryptoWallet.add(bitcoin);

        // Tentative d'ajouter une Action dans un Wallet Crypto :
        // cryptoWallet.add(apple); // <--- DECOMMENTE CETTE LIGNE : ça ne compilera pas !
        // C'est la preuve que ton générique <T extends Asset> fonctionne.


        // 4. Test du Multithreading (Observation des prix)
        System.out.println("\n=== Observation des prix pendant 10 secondes ===");
        System.out.println("(Les prix changent toutes les 3s en arrière-plan)");

        for (int i = 0; i < 3; i++) {
            try {
                Thread.sleep(3500); // On attend un peu plus que le thread de l'Asset

                System.out.println("\n--- Tick Marché n°" + (i + 1) + " ---");
                System.out.printf("Prix %s (%s) : %.2f€\n", apple.getName(), apple.getTicker(), apple.getPrice());
                System.out.printf("Prix %s (%s) : %.2f€\n", bitcoin.getName(), bitcoin.getTicker(), bitcoin.getPrice());

                // Test de la méthode fonctionnelle du Wallet (Stream)
                System.out.printf("Valeur totale du portefeuille Crypto : %.2f€\n", cryptoWallet.getTotalValue());

            } catch (InterruptedException e) {
                System.err.println("Erreur dans la boucle principale");
            }
        }

        System.out.println("\n========== FIN DU TEST ==========");
        // Note : Les threads daemon s'arrêteront automatiquement ici.
    }
}