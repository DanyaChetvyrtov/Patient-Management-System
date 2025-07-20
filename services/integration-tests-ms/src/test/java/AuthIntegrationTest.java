import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {
    @BeforeAll
    static void setUpt() {
        RestAssured.baseURI = "http://localhost:4003";
    }

    @Test
    public void shouldReturnOKWithValidToken() {
        var loginPayload = """
                {
                  "email": "danil2@gmail.com",
                  "password": "root12345"
                }
                """;

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(loginPayload)
                .when().post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .extract().response();

        System.out.println("Generated token: " + response.jsonPath().getString("accessToken"));
    }

    @Test
    public void shouldReturnNotAuthorizedOnInvalidCredentials() {
        var loginPayload = """
                {
                  "email": "invalid_user@gmail.com",
                  "password": "wrong_password"
                }
                """;

        RestAssured.given()
                .contentType("application/json")
                .body(loginPayload)
                .when().post("/api/auth/login")
                .then()
                .statusCode(401);
    }
}
