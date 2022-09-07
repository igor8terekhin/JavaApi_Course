import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

public class Task1ex4 {

    @Test
    public void sendGetRequest() {
        Response response = RestAssured
                .get("https://playground.learnqa.ru/api/get_text")
                .andReturn();

        System.out.println(response.asString());
    }

}
