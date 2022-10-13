package tests;

import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class GetUserTest extends BaseTestCase {
    final String getUserUrl = "https://playground.learnqa.ru/api/user/";
    final String loginUrl = "https://playground.learnqa.ru/api/user/login";
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Description("Test tries to get user's data while being not authorised")
    @DisplayName("Test get user's data while not authorized")
    @Test
    public void getNotAuthUserDataTest() {
        Response responseUserData = RestAssured
                .get(getUserUrl + 2)
                .andReturn();

        Assertions.assertJsonHasField(responseUserData, "username");
        Assertions.assertJsonHasNotField(responseUserData, "firstName");
        Assertions.assertJsonHasNotField(responseUserData, "lastName");
        Assertions.assertJsonHasNotField(responseUserData, "email");
    }

    @Description("Test authorizes and gets data for authed user")
    @DisplayName("Test get current authed user data")
    @Test
    public void getUserDetailAuthAsSameUserTest() {

        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = apiCoreRequests.makeGetRequest(getUserUrl + 2, header, cookie);

        String[] expectedFields = {"username", "firstName", "lastName", "email"};
        Assertions.assertJsonHasFields(responseUserData, expectedFields);

        Assertions.assertJsonHasField(responseUserData, "username");
    }

    @Description("Test tries to get one user data while authorized as another user")
    @DisplayName("Test get another user data")
    @Test
    public void getAnotherUserDataTest(){
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseUserData = apiCoreRequests.makeGetRequest(getUserUrl + 1, header, cookie);

        String[] unexpectedFields = {"id", "email", "firstName", "lastName"};

        Assertions.assertJsonHasNotFields(responseUserData, unexpectedFields);
    }
}
