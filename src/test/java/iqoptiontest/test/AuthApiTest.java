package iqoptiontest.test;

import io.restassured.http.Cookie;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class AuthApiTest {

    private Cookie cookie;
    private URI LOGIN_URI;
    private URI USER_PROFILE_URI;

    @BeforeClass
    @Parameters({"login_service_url", "user_profile_url", "cookie_lang"})
    private void beforeClass(String loginUrl, String userProfileUrl, String cookieLang) {
        this.LOGIN_URI = URI.create(loginUrl);
        this.USER_PROFILE_URI = URI.create(userProfileUrl);
        this.cookie = new Cookie.Builder("lang", cookieLang).build();
    }

    @Test
    @Parameters({"existing_user_email", "existing_user_password"})
    public void Auth_UserAlreadyExisting_ReturnsSsidAndUserProfile(final String email, final String password) {
        final String SSID = given()
            .cookie(this.cookie)
            .param("email", email)
            .param("password", password).
        when()
            .post(this.LOGIN_URI).
        then()
            .statusCode(200)
            .body("errors", equalTo(null))
            .body("data.ssid", not(is(emptyString())))
            .extract().path("data.ssid");

        given()
            .cookie(this.cookie)
            .cookie(new Cookie.Builder("ssid", SSID).build()).
        when()
            .get(this.USER_PROFILE_URI).
        then()
            .statusCode(200)
            .body("isSuccessful", equalTo(true))
            .body("result.email", equalTo(email));
    }

    @Test
    @Parameters({"existing_user_email", "wrong_password", "invalid_credentials_code"})
    public void Auth_UserAlreadyExistingWrongPassword_ReturnsError(final String email, final String password, int errorCode) {
        given()
            .cookie(this.cookie)
            .param("email", email)
            .param("password", password).
        when()
            .post(this.LOGIN_URI).
        then()
            .statusCode(403)
            .body(String.format("errors.find { it.code == %d }.code", errorCode), is(not(nullValue())));
    }

    @Test
    @Parameters({"not_valid_email", "valid_password", "not_valid_email_code"})
    public void Auth_EmailIsNotValid_ReturnsError(String notValidEmail, String password, int errorCode) {
        given()
            .cookie(this.cookie)
            .param("email", notValidEmail)
            .param("password", password).
        when()
            .post(this.LOGIN_URI).
        then()
            .statusCode(400)
            .body(String.format("errors.find { it.code == %d }.code", errorCode), is(not(nullValue())));
    }
}
