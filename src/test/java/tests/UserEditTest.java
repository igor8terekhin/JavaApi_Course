package tests;

import io.qameta.allure.Description;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserEditTest extends BaseTestCase {
    final String getUserUrl = "https://playground.learnqa.ru/api/user/";
    final String loginUrl = "https://playground.learnqa.ru/api/user/login";

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
    @Description("Test change user's first name to the provided")
    @DisplayName("Test change first name")
    public void editJustCreatedTest() {
        //Generate user
        Map<String, String> userData = DataGenerator.getRegistrationData();
        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(getUserUrl)
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        //Login

        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        //Edit
        String newName = "Changed name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put(getUserUrl + userId)
                .andReturn();

        //Get user and further checks
        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .get(getUserUrl + userId)
                .andReturn();

        Assertions.assertJsonByName(responseUserData, "firstName", newName);
    }

    @Test
    @Description("Test tires to change user's first name without authentication")
    @DisplayName("Test change first name without auth")
    public void editUserWithoutAuth() {
        //Attempt to change user's data
        String newName = "Changed name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(getUserUrl + 2, editData);

        Assertions.assertResponseTextEquals(responseEditUser, "Auth token not supplied");

        //Login as user with changed data
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //Get changed data
        Response responseUserData = apiCoreRequests.makeGetRequest(getUserUrl + 2, header, cookie);

        Assertions.assertJsonByName(responseUserData, "firstName", "Vitalii");
    }

    @Test
    @Description("Test tries to change first name while being authed as another user")
    @DisplayName("Test change first name with another auth")
    public void editUserDataAsAnotherUser() {
        //Generate user
        Map<String, String> userData = DataGenerator.getRegistrationData();
        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(getUserUrl)
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        //Login
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        //Edit user
        String newName = "Changed name";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        Response responseEditUser = apiCoreRequests.makePutRequest(getUserUrl + 2, editData);

        Assertions.assertResponseTextEquals(responseEditUser, "Auth token not supplied");

        //Login as user with changed data
        Map<String, String> authDataUpd = new HashMap<>();
        authDataUpd.put("email", "vinkotov@example.com");
        authDataUpd.put("password", "1234");

        Response responseGetAuthUpd = apiCoreRequests.makePostRequest(loginUrl, authDataUpd);

        String header = this.getHeader(responseGetAuthUpd, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuthUpd, "auth_sid");

        //Get changed data
        Response responseUserData = apiCoreRequests.makeGetRequest(getUserUrl + 2, header, cookie);

        Assertions.assertJsonByName(responseUserData, "firstName", "Vitalii");
    }

    @Test
    @Description("Test tries to change user's email to the wrong format email (without @)")
    @DisplayName("Test change email with wrong one")
    public void editWrongEmail() {
        //Generate user
        Map<String, String> userData = DataGenerator.getRegistrationData();
        JsonPath responseCreateAuth = RestAssured
                .given()
                .body(userData)
                .post(getUserUrl)
                .jsonPath();

        String userId = responseCreateAuth.getString("id");

        //Login

        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        //Edit
        String newEmail = "wrong.email.com";
        Map<String, String> editData = new HashMap<>();
        editData.put("email", newEmail);

        Response responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put(getUserUrl + userId)
                .andReturn();

        //Get user and further checks
        Response responseUserData = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .get(getUserUrl + userId)
                .andReturn();

        Assertions.assertJsonNotEqualsByName(responseUserData, "email", newEmail);
    }

    @Test
    @Description("Test tries change user's first name to the one character first name")
    @DisplayName("Test first name to the short one")
    public void editFirstNameShortName() {
        //Generate user
        Map<String, String> userData = DataGenerator.getRegistrationData();
        JsonPath responseCreateAuth = apiCoreRequests.makePostRequestAsJson(getUserUrl, userData);

        String userId = responseCreateAuth.getString("id");

        //Login
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        //Attempt to edit
        String newName = "s";
        Map<String, String> editData = new HashMap<>();
        editData.put("firstName", newName);

        String token = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        Response responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", token)
                .cookie("auth_sid", cookie)
                .body(editData)
                .put(getUserUrl + userId)
                .andReturn();

        //put request is done with correct token:
        System.out.println(responseEditUser.asString());
        Assertions.assertJsonByName(responseEditUser, "error", "Too short value for field firstName");

        responseEditUser = apiCoreRequests.makePutRequestWithTokenAndCookie(getUserUrl + userId, editData, token, cookie);

        //put response is done with wrong token:
        System.out.println(responseEditUser.asString());

        //Get user and further checks
        Response responseUserData = apiCoreRequests.makeGetRequest(getUserUrl + userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));

        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }
}
