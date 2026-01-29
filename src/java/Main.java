import com.market.assets.Stock;

import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        Stock asset = new Stock("Gold", "GLD", 1800.50, 1.5f, LocalTime.of(9,0), LocalTime.of(17,30));
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}