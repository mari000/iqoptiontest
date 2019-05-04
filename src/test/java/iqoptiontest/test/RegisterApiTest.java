package iqoptiontest.test;

import com.google.common.collect.ImmutableMap;
import io.restassured.http.Cookie;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class RegisterApiTest {

    private Cookie cookie;
    private Map<String, Object> defaultParams;
    private URI REGISTER_URI;

    @BeforeClass
    @Parameters({"register_service_url", "cookie_lang", "valid_email", "valid_password", "valid_first_name", "valid_last_name", "valid_tz"})
    private void beforeClass(
            String registerUrl,
            String cookieLang,
            String email,
            String password,
            String firstName,
            String lastName,
            String tz)
    {
        this.REGISTER_URI = URI.create(registerUrl);
        this.cookie = new Cookie.Builder("lang", cookieLang).build();
        this.defaultParams = ImmutableMap.<String, Object> builder()
                .put("email", email)
                .put("password", password)
                .put("first_name", firstName)
                .put("last_name", lastName)
                .put("tz", lastName)
                .build();
    }

    @Test
    @Parameters({"existing_user_email", "user_already_registered_code"})
    public void RegisterUser_EmailAlreadyExisting_ReturnsError(String existingUserEmail, int errorCode) {
        given()
            .cookie(this.cookie)
            .params(this.defaultParams)
            .param("email", existingUserEmail).
        when()
            .post(this.REGISTER_URI).
        then()
            .statusCode(200)
            .body("isSuccessful", equalTo(false))
            .body("code", equalTo(errorCode));
    }

    @Test
    @Parameters({"not_valid_email", "not_valid_email_code"})
    public void RegisterUser_EmailIsNotValid_ReturnsError(String notValidEmail, int errorCode) {
        given()
            .cookie(this.cookie)
            .params(this.defaultParams)
            .param("email", notValidEmail).
        when()
            .post(this.REGISTER_URI).
        then()
            .statusCode(200)
            .body("isSuccessful", equalTo(false))
            .body("code", equalTo(errorCode));
    }

    @Test
    @Parameters({"pwd_less_than_6_char", "invalid_pwd_length_code"})
    public void RegisterUser_PasswordLessThan6Char_ReturnsError(String notValidPassword, int errorCode) {
        given()
            .cookie(this.cookie)
            .params(this.defaultParams)
            .param("password", notValidPassword).
        when()
            .post(this.REGISTER_URI).
        then()
            .statusCode(200)
            .body("isSuccessful", equalTo(false))
            .body("code", equalTo(errorCode));
    }

    @Test
    @Parameters({"fist_name_required_code"})
    public void RegisterUser_UserNameNotSend_ReturnsError(int errorCode) {
        final Map<String, Object> paramsWithoutUserName =
            ImmutableMap.copyOf(this.defaultParams.entrySet()
                .stream()
                .filter(e -> !e.getKey().equals("first_name"))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))
            );

        given()
            .cookie(this.cookie)
            .params(paramsWithoutUserName).
        when()
            .post(this.REGISTER_URI).
        then()
            .statusCode(200)
            .body("isSuccessful", equalTo(false))
            .body("code", equalTo(errorCode));
    }
}
