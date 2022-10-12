package tests;

import io.qameta.allure.Description;
import io.restassured.response.Response;
import lib.ApiCoreRequests;
import lib.Assertions;
import lib.BaseTestCase;
import lib.DataGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;


public class UserRegisterTest extends BaseTestCase {
    final String url = "https://playground.learnqa.ru/api/user/";
    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Description("Test tries to create user with existing email")
    @DisplayName("Create user with existing email")
    @Test
    public void testCreateUserWithExistingEmail() {

        String email = "vinkotov@example.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(url, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 400);
        Assertions.assertResponseTextEquals(responseCreateAuth, "Users with email '" + email + "' already exists");
    }

    @Description("Test creates new user")
    @DisplayName("Create new user")
    @Test
    public void testCreateUserSuccessfully() {

        Map<String, String> userData = DataGenerator.getRegistrationData();

        Response responseCreateAuth = apiCoreRequests.makePostRequest(url, userData);

        Assertions.assertResponseCodeEquals(responseCreateAuth, 200);
        Assertions.assertJsonHasField(responseCreateAuth, "id");
    }

    @Description("Test tries to create user with incorrect (without @) email")
    @DisplayName("Create user with incorrect email")
    @Test
    public void testCreateUserWithIncorrectEmail() {

        String email = "incorrect.email.com";

        Map<String, String> userData = new HashMap<>();
        userData.put("email", email);
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(url, userData);

        Assertions.assertResponseTextEquals(responseCreateAuth, "Invalid email format");
    }

    @Description("Test tries to create user without any of required parameters")
    @DisplayName("Create user without any of parameters")
    @ParameterizedTest
    @ValueSource(strings = {"email", "username", "password", "firstName", "lastName"})
    public void testCreateUserWithoutParameter(String condition) {
        Map<String, String> userData = DataGenerator.getRegistrationData();
        userData.remove(condition);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(url, userData);

        Assertions.assertJsonHasNotField(responseCreateAuth, "id");
        Assertions.assertResponseTextEquals(responseCreateAuth,"The following required params are missed: " + condition);
    }

    @Description("Test tries to create user with 1 character first name")
    @DisplayName("Create user with 1 character firstname")
    @Test
    public void testCreateUserOneCharName() {
        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", "1");
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(url, userData);

        Assertions.assertJsonHasNotField(responseCreateAuth, "id");
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'firstName' field is too short");
    }

    @Description("Test tries to create new user with very long (251 characters) first name")
    @DisplayName("Create user with 251 characters first name")
    @Test
    public void testCreateUserWithVeryLongName() {
        Map<String, String> userData = new HashMap<>();
        userData.put("firstName", DataGenerator.generateRandomString(251));
        userData = DataGenerator.getRegistrationData(userData);

        Response responseCreateAuth = apiCoreRequests.makePostRequest(url, userData);

        Assertions.assertJsonHasNotField(responseCreateAuth, "id");
        Assertions.assertResponseTextEquals(responseCreateAuth, "The value of 'firstName' field is too long");
    }
}
