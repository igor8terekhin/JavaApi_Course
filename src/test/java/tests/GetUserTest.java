package tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class GetUserTest extends BaseTestCase {
    final String baseUrl = "https://playground.learnqa.ru/api/user/";
    final String url = "https://playground.learnqa.ru/api/user/login";

    @Test
    public void getNotAuthUserDataTest() {
        Response responseUserData = RestAssured
                .get(baseUrl + 2)
                .andReturn();

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasNotField(responseUserData, "firstName");
        Assertions.assertJsonHasNotField(responseUserData, "lastName");
        Assertions.assertJsonHasNotField(responseUserData, "email");
    }

    @Test
    public void getUserDetailAuthAsSameUserTest() {


        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = RestAssured
                .given()
                .body(authData)
                .post(url)
                .andReturn();

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", header)
                .cookie("auth_sid", cookie)
                .get(baseUrl + 2)
                .andReturn();

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);

        Assertions.assertJsonHasField(responseUserData, "username");
    }

}
