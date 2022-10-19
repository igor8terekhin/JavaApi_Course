package tests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class UserEditTest extends BaseTestCase {
    final String getUserUrl = "https://playground.learnqa.ru/api/user/";
    final String loginUrl = "https://playground.learnqa.ru/api/user/login";

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Test
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

        Response responseEditUser = RestAssured
                .given()
                .header("x-csrf-token", this.getHeader(responseGetAuth, "x-csrf-token"))
                .cookie("auth_sid", this.getCookie(responseGetAuth, "auth_sid"))
                .body(editData)
                .put(getUserUrl + userId)
                .andReturn();

        Assertions.assertJsonByName(responseEditUser, "error", "Too short value for field firstName");

        //Get user and further checks
        Response responseUserData = apiCoreRequests.makeGetRequest(getUserUrl + userId,
                this.getHeader(responseGetAuth, "x-csrf-token"),
                this.getCookie(responseGetAuth, "auth_sid"));

        Assertions.assertJsonByName(responseUserData, "firstName", userData.get("firstName"));
    }
}
