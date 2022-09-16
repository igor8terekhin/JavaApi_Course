import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashMap;
import java.util.Map;

public class Task2ex8 {
    @Test
    public void checkToken() throws InterruptedException {
        String url = "https://playground.learnqa.ru/ajax/api/longtime_job";

        JsonPath createTask = RestAssured
                .given()
                .get(url)
                .jsonPath();

        Map<String, String> params = new HashMap<>();
        params.put("token", createTask.getJsonObject("token"));
        int secondsToWait = createTask.getJsonObject("seconds");

        Assertions.assertEquals("Job is NOT ready", checkTaskStatus(url, params));

        Thread.sleep(secondsToWait * 1000L);

        Assertions.assertEquals("Job is ready", checkTaskStatus(url, params));
    }

    public String checkTaskStatus(String url, Map<String, String> params) {
        JsonPath response = RestAssured
                .given()
                .queryParams(params)
                .get(url)
                .jsonPath();
        return response.getJsonObject("status");
    }
}
