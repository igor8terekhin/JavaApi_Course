import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class Task2ex7 {
    @Test
    public void getAllRedirectLocations() {
        int statusCode = 0;
        String redirectLocation = "https://playground.learnqa.ru/api/long_redirect";
        int counter = 0;
        while (statusCode != 200) {
            counter++;
            Response response = RestAssured
                    .given()
                    .redirects()
                    .follow(false)
                    .when()
                    .get(redirectLocation)
                    .andReturn();

            redirectLocation = response.getHeader("Location");
            statusCode = response.getStatusCode();

            if (redirectLocation == null)
                break;
            System.out.println("Redirect " + counter + ": " + redirectLocation);
        }
    }
}
