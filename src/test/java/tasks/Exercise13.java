package tasks;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Exercise13 {

    @ParameterizedTest
    @CsvFileSource(resources = "/data.txt", delimiter = '!')
    public void userAgentTest(String userAgent, String platform, String browser, String device) {
        String url = "https://playground.learnqa.ru/ajax/api/user_agent_check";

        Map<String, String> parameters = new HashMap<>();
        parameters.put("user-agent", userAgent);
        parameters.put("platform", platform);
        parameters.put("browser", browser);
        parameters.put("device", device);

        JsonPath response = RestAssured
                .given()
                .header("User-Agent", parameters.get("user-agent"))
                .get(url)
                .jsonPath();

        assertEquals(parameters.get("platform"), response.getJsonObject("platform"), "Incorrect platform");
        assertEquals(parameters.get("browser"), response.getJsonObject("browser"), "Incorrect browser");
        assertEquals(parameters.get("device"), response.getJsonObject("device"), "Incorrect device");
    }
}
