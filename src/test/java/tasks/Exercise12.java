package tasks;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Exercise12 extends BaseTestCase {
    @Test
    public void headersTest() {
        String url = "https://playground.learnqa.ru/api/homework_header";

        Response response = RestAssured
                .get(url)
                .andReturn();

        String headerValue = response.getHeader("x-secret-homework-header");
        String expectedHeaderValue = "Some secret value";

        assertEquals(expectedHeaderValue, headerValue, "Such header value is unexpected!");

    }
}
