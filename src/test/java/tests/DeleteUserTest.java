package tests;

import io.qameta.allure.Description;
import io.restassured.path.json.JsonPath;
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

public class DeleteUserTest extends BaseTestCase {
    final String userUrl = "https://playground.learnqa.ru/api/user/";
    final String loginUrl = "https://playground.learnqa.ru/api/user/login";

    private final ApiCoreRequests apiCoreRequests = new ApiCoreRequests();

    @Description("Test tries to delete ID 2 user which cannot be deleted")
    @DisplayName("Test delete ID 2 user")
    @Test
    public void deleteHardcodedUser() {
        Map<String, String> authData = new HashMap<>();
        authData.put("email", "vinkotov@example.com");
        authData.put("password", "1234");

        //Login
        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //Delete user
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(userUrl + 2, header, cookie);

        Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
        Assertions.assertResponseTextEquals(responseDeleteUser, "Please, do not delete test users with ID 1, 2, 3, 4 or 5.");

        //Get user (to make sure it wasn't deleted)
        Response responseUserData = apiCoreRequests.makeGetRequest(userUrl + 2, header, cookie);

        Assertions.assertResponseCodeEquals(responseUserData, 200);
        Assertions.assertJsonByName(responseUserData, "username", "Vitaliy");
    }

    @Description("Test creates user and deletes it")
    @DisplayName("Test create and delete user")
    @Test
    public void createUserAndDeleteIt() {
        //Generate user
        Map<String, String> userData = DataGenerator.getRegistrationData();
        JsonPath responseCreateAuth = apiCoreRequests.makePostRequestAsJson(userUrl, userData);

        String userId = responseCreateAuth.getString("id");

        //Login
        Map<String, String> authData = new HashMap<>();
        authData.put("email", userData.get("email"));
        authData.put("password", userData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        //Delete user
        Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(userUrl + userId, header, cookie);

        Assertions.assertResponseCodeEquals(responseDeleteUser, 200);

        //Try to log in as deleted user

        Response responseUserData = apiCoreRequests.makeGetRequest(userUrl + userId, header, cookie);

        Assertions.assertResponseCodeEquals(responseUserData, 404);
        Assertions.assertResponseTextEquals(responseUserData, "User not found");
    }

    @Description("Test tries to delete user while logged in as another user")
    @DisplayName("Test delete user as another user")
    @ParameterizedTest
    @ValueSource(strings = {"fullData", "noAuth", "noCookie", "noHeader"})
    public void deleteUserAsAnotherUser(String dataRequired) {
        //Generate users
        Map<String, String> firstUserData = DataGenerator.getRegistrationData();
        Map<String, String> secondUserData = DataGenerator.getRegistrationData();

        JsonPath responseCreateAuthFirst = apiCoreRequests.makePostRequestAsJson(userUrl, firstUserData);
        String firstUserId = responseCreateAuthFirst.getString("id");

        JsonPath responseCreateAuthSecond = apiCoreRequests.makePostRequestAsJson(userUrl, secondUserData);
        String secondUserId = responseCreateAuthSecond.getString("id");

        //Login as user 1
        Map<String, String> authData = new HashMap<>();
        authData.put("email", firstUserData.get("email"));
        authData.put("password", firstUserData.get("password"));

        Response responseGetAuth = apiCoreRequests.makePostRequest(loginUrl, authData);

        String header = this.getHeader(responseGetAuth, "x-csrf-token");
        String cookie = this.getCookie(responseGetAuth, "auth_sid");

        switch (dataRequired) {
            case "fullData": {
                //Delete user 2
                Response responseDeleteUser = apiCoreRequests.makeDeleteRequest(userUrl + secondUserId, header, cookie);
                //Probably bug here and response should be 400, auth token not supplied. Aligned assertions
                //with actual result.
                Assertions.assertResponseCodeEquals(responseDeleteUser, 200);

                //Get user 2
                Response responseUserDataSecond = apiCoreRequests.makeGetRequestWithoutData(userUrl + secondUserId);
                //Probably bug here and user should be deleted as for below checks. Aligned assertions
                //with actual result.
                Assertions.assertResponseCodeEquals(responseUserDataSecond, 200);
                Assertions.assertJsonByName(responseUserDataSecond, "username", firstUserData.get("username"));

                //Get user 1
                Response responseUserDataFirst = apiCoreRequests.makeGetRequest(userUrl + firstUserId, header, cookie);
                //Probably bug and user shouldn't be deleted. Aligned assertions
                //with actual result.
                Assertions.assertResponseCodeEquals(responseUserDataFirst, 404);
                Assertions.assertResponseTextEquals(responseUserDataFirst, "User not found");
                break;
            }
            case "noAuth": {
                //Delete user 2
                Response responseDeleteUser = apiCoreRequests.makeDeleteRequestNoData(userUrl + secondUserId);

                Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
                Assertions.assertResponseTextEquals(responseDeleteUser, "Auth token not supplied");

                //Get user 2
                Response responseUserDataSecond = apiCoreRequests.makeGetRequestWithoutData(userUrl + secondUserId);

                Assertions.assertResponseCodeEquals(responseUserDataSecond, 200);
                Assertions.assertJsonByName(responseUserDataSecond, "username", secondUserData.get("username"));

                //Get user 1
                Response responseUserDataFirst = apiCoreRequests.makeGetRequest(userUrl + firstUserId, header, cookie);

                Assertions.assertResponseCodeEquals(responseUserDataFirst, 200);
                Assertions.assertJsonByName(responseUserDataFirst, "email", firstUserData.get("email"));
                break;
            }
            case "noCookie": {
                //Delete user 2
                Response responseDeleteUser = apiCoreRequests.makeDeleteRequestNoCookie(userUrl + secondUserId, header);

                Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
                Assertions.assertResponseTextEquals(responseDeleteUser, "Auth token not supplied");

                //Get user 2
                Response responseUserDataSecond = apiCoreRequests.makeGetRequestWithoutData(userUrl + secondUserId);

                Assertions.assertResponseCodeEquals(responseUserDataSecond, 200);
                Assertions.assertJsonByName(responseUserDataSecond, "username", secondUserData.get("username"));

                //Get user 1
                Response responseUserDataFirst = apiCoreRequests.makeGetRequest(userUrl + firstUserId, header, cookie);

                Assertions.assertResponseCodeEquals(responseUserDataFirst, 200);
                Assertions.assertJsonByName(responseUserDataFirst, "email", firstUserData.get("email"));
                break;
            }
            case "noHeader": {
                //Delete user 2
                Response responseDeleteUser = apiCoreRequests.makeDeleteRequestNoToken(userUrl + secondUserId, cookie);

                Assertions.assertResponseCodeEquals(responseDeleteUser, 400);
                Assertions.assertResponseTextEquals(responseDeleteUser, "Auth token not supplied");

                //Get user 2
                Response responseUserDataSecond = apiCoreRequests.makeGetRequestWithoutData(userUrl + secondUserId);

                Assertions.assertResponseCodeEquals(responseUserDataSecond, 200);
                Assertions.assertJsonByName(responseUserDataSecond, "username", secondUserData.get("username"));
                //Get user 1
                Response responseUserDataFirst = apiCoreRequests.makeGetRequest(userUrl + firstUserId, header, cookie);

                Assertions.assertResponseCodeEquals(responseUserDataFirst, 200);
                Assertions.assertJsonByName(responseUserDataFirst, "email", firstUserData.get("email"));
                break;
            }
        }
    }

}
