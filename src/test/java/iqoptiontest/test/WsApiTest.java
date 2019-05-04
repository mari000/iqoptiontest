package iqoptiontest.test;

import com.jayway.jsonpath.ReadContext;
import io.restassured.http.Cookie;
import iqoptiontest.helper.WsHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class WsApiTest {

    private Cookie cookie;
    private URI LOGIN_URI;
    private URI WS_URI;
    private WsHelper wsHelper;

    @BeforeClass
    @Parameters({"login_service_url", "ws_service_url", "cookie_lang", "ws_connection_ms_timeout"})
    private void beforeClass(String loginUrl, String wsUrl, String cookieLang, int connectionTimeout) throws IOException {
        this.LOGIN_URI = URI.create(loginUrl);
        this.WS_URI = URI.create(wsUrl);
        this.cookie = new Cookie.Builder("lang", cookieLang).build();
        this.wsHelper = new WsHelper(this.WS_URI, connectionTimeout);
    }

    @Test
    @Parameters({"existing_user_email", "existing_user_password", "request_id", "ws_response_ms_timeout"})
    public void WsAuth_ExistingSession_AuthSuccessfulReturnsProfile(String email, String password, String requestId, int responseTimeout) throws Exception {
        // Given
        ReadContext ctx;
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

        // When
        this.wsHelper.connect();
        ctx = this.wsHelper.authorize(SSID, requestId,  responseTimeout);
        this.wsHelper.disconnect();

        // Then
        Assert.assertNotNull(ctx);
        Assert.assertNotEquals(ctx.read("$.msg"), false);
        Assert.assertEquals(ctx.read("$.request_id"), requestId);
        Assert.assertEquals(ctx.read("$.msg.email"), email);
    }

    @Test
    @Parameters({"ws_not_existing_ssid", "request_id", "ws_response_ms_timeout"})
    public void WsAuth_NotExistingSession_AuthFailReturnsFalse(String ssid, String requestId, int responseTimeout) throws Exception {
        // Given
        ReadContext ctx;

        // When
        this.wsHelper.connect();
        ctx = this.wsHelper.authorize(ssid, requestId,  responseTimeout);
        this.wsHelper.disconnect();

        // Then
        Assert.assertNotNull(ctx);
        Assert.assertFalse(ctx.read("$.msg"));
    }
}
