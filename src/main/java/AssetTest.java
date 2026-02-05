
import com.market.assets.Asset;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

class AssetTest {

    @Test
    void assetPriceFieldShouldBePrivate() throws Exception {
        Field field = Asset.class.getDeclaredField("price");
        assertTrue(Modifier.isPrivate(field.getModifiers()),
                "price should be private");
    }

    @Test
    void assetPriceFieldShouldBeOfTypeDouble() throws Exception {
        Field field = Asset.class.getDeclaredField("price");
        assertEquals(double.class, field.getType(),
                "price should be of type double");
    }

    @Test
    void assetClassShouldBeAbstract() {
        assertTrue(Modifier.isAbstract(Asset.class.getModifiers()),
                "Asset should be abstract");
    }

    @Test
    void assetShouldHaveGetPriceMethod() throws Exception {
        Method method = Asset.class.getDeclaredMethod("getPrice");
        assertTrue(Modifier.isPublic(method.getModifiers()),
                "getPrice should be public");
        assertEquals(double.class, method.getReturnType(),
                "getPrice should return double");
    }


    @Test
    void assetFieldsShouldAllBePrivate() {
        Field[] fields = Asset.class.getDeclaredFields();

        for (Field field : fields) {
            assertTrue(Modifier.isPrivate(field.getModifiers()),
                    "Field " + field.getName() + " should be private");
        }
    }
}