package tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

@Feature("Authorisation")
public class UserAuthTest extends BaseTestCase {

    String cookie;
    String header;
    int userIdOnAuth;

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @BeforeEach
    public void loginUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        String url = "https://playground.learnqa.ru/api/user/login";

        Response responseGetAuth = apiCoreRequests.makePostRequest(url, authData);

        this.cookie = this.getCookie(responseGetAuth, "auth_sid");
        this.header = this.getHeader(responseGetAuth, "x-csrf-token");
        this.userIdOnAuth = this.getIntFromJson(responseGetAuth, "user_id");
    }

    @Test
    @Description("Test successfully authorizes user using email and password")
    @DisplayName("Test user with positive auth")
    public void testAuthUser() {
        String url = "https://playground.learnqa.ru/api/user/auth";

        Response responseCheckAuth = apiCoreRequests.makeGetRequest(url, this.header, this.cookie);

        Assertions.assertJsonByName(responseCheckAuth, "user_id", this.userIdOnAuth);

    }

    @Severity(SeverityLevel.CRITICAL)
    @Description("Test checks authorization status without sending auth cookie or token")
    @DisplayName("Negative auth user")
    @ParameterizedTest
    @ValueSource(strings = {"cookie", "headers"})
    public void testNegativeAuthUser(String condition) {

        String url = "https://playground.learnqa.ru/api/user/auth";

        if (condition.equals("cookie")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithCookie(url, this.cookie);
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else if (condition.equals("headers")) {
            Response responseForCheck = apiCoreRequests.makeGetRequestWithToken(url, this.header);
            Assertions.assertJsonByName(responseForCheck, "user_id", 0);
        } else {
            throw new IllegalArgumentException("Condition isn't known " + condition);
        }
    }
}
