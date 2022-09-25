package tasks;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.BaseTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Exercise11 extends BaseTestCase {

    @Test
    public void cookieTest() {
        String url = "https://playground.learnqa.ru/api/homework_cookie";

        Response response = RestAssured
                .get(url)
                .andReturn();

        String cookie = this.getCookie(response, "HomeWork");
        String expectedCookie = "hw_value";

        assertEquals(expectedCookie, cookie, "Such cookie value isn't expected!");
    }
}
