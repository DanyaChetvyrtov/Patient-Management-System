import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.notNullValue;

public class PatientIntegrationTest {
    @BeforeAll
    static void setUpt() {
        RestAssured.baseURI = "http://localhost:4003";
    }

    @Test
    public void shouldReturnPatientsWithValidToken() {
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

        var token = response.jsonPath().getString("accessToken");
        System.out.println(token);

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when().get("/api/patients")
                .then().statusCode(200).body("patients", notNullValue());
    }
}
