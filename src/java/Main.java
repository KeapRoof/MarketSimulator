import com.market.assets.Stock;

import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        System.out.println("Market sim");
        Stock asset = new Stock("Gold", "GLD", 1800.50, 1.5f, LocalTime.of(9,0), LocalTime.of(17,30));
    }
}