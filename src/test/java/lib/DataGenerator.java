package lib;

import java.text.SimpleDateFormat;
import java.util.*;

public class DataGenerator {

    public static String getRandomEmail() {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Random rd = new Random();
        String randomInt = Integer.toString(rd.nextInt(999));
        return randomInt + "igorTest" + timestamp + "@example.com";
    }

    public static Map<String, String> getRegistrationData() {
        Map<String, String> data = new HashMap<>();

        data.put("email", getRandomEmail());
        data.put("password", "123");
        data.put("username", "learnqa");
        data.put("firstName", "learnqa");
        data.put("lastName", "learnqa");

        return data;
    }

    public static Map<String, String> getRegistrationData(Map<String, String> nonDefaultValues) {
        Map<String, String> defaultValues = getRegistrationData();

        Map<String, String> userData = new HashMap<>();
        String[] keys = {"email", "username", "password", "firstName", "lastName"};

        for (String key : keys) {
            if (nonDefaultValues.containsKey(key)) {
                userData.put(key, nonDefaultValues.get(key));
            }
            else {
                userData.put(key, defaultValues.get(key));
            }
        }
        return userData;
    }

    public static String generateRandomString(int length) {

        int begin = 97;
        int end = 122;

        Random rnd = new Random();

        return rnd.ints(begin, end + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }
}
