package tasks;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Exercise10 {

    @ParameterizedTest
    @ValueSource(strings = {"","short string","not that short string"})
    public void stringLengthTest(String string) {
        assertTrue(string.length() > 15, "String length "
                + string.length() + " is less than 15 characters");
    }
}
