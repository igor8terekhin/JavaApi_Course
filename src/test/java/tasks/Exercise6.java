package tasks;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class Exercise6 {

    @Test
    public void redirect() {
        Response response = RestAssured
                .given()
                .redirects()
                .follow(false)
                .when()
                .get("https://playground.learnqa.ru/api/long_redirect")
                .andReturn();

        String redirectLocationHeader = response.getHeader("Location");
        System.out.println(redirectLocationHeader);
    }
}
