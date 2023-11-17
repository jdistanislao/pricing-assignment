package org.example.assignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.core.Response;
import org.example.assignment.controller.dto.FinalPriceRequest;
import org.example.assignment.gateway.impl.MarkdownGatewayImpl;
import org.example.assignment.model.Markdown;
import org.example.assignment.model.MarkdownID;
import org.example.assignment.model.ProductID;
import org.example.assignment.policy.impl.CountMarkdownPolicy;
import org.example.assignment.policy.impl.PercentageMarkdownPolicy;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class PricingControllerTest {

    private static final String FINAL_PRICE_PATH = "v1/pricing/finalprice";
    private static MarkdownGatewayImpl markdownGateway;

    private final ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @BeforeAll
    public static void setup() {
        markdownGateway = Mockito.mock(MarkdownGatewayImpl.class);
        QuarkusMock.installMockForType(markdownGateway, MarkdownGatewayImpl.class);
    }

    @Test
    public void wrongPath() {
        given()
                .when()
                .get("wrongpath")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @ParameterizedTest
    @CsvSource({
            ",1.0,1,productId",
            "00000000-0000-0000-0000-000000000000,1.0,1,productId",
            "11111111-1111-1111-1111-111111111111,1.0,-1,quantity",
            "11111111-1111-1111-1111-111111111111,1.0,0,quantity",
            "11111111-1111-1111-1111-111111111111,0.0,1,productPrice",
            "11111111-1111-1111-1111-111111111111,-1.0,1,productPrice"
    })
    public void wrongRequest(String productId, double price, int quantity, String errorField) throws JsonProcessingException {

        var request = new FinalPriceRequest();
        request.productId = productId;
        request.productPrice = price;
        request.quantity = quantity;

        var body = given()
                .body(jsonMapper.writeValueAsString(request))
                .contentType(ContentType.JSON)
                .when()
                .get(FINAL_PRICE_PATH)
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode())
                .extract().body().asString();

        assertTrue(body.contains(errorField));
    }

    @Test
    public void noMarkdownApplied() throws JsonProcessingException {

        var productId = new ProductID(UUID.randomUUID());
        var request = new FinalPriceRequest();
        request.productId = productId.id().toString();
        request.productPrice = 1d;
        request.quantity = 100;

        Mockito.when(markdownGateway.getPolicyByProductId(productId)).thenReturn(Optional.empty());

        given()
                .body(jsonMapper.writeValueAsString(request))
                .contentType(ContentType.JSON)
                .when()
                .get(FINAL_PRICE_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is("100.0"));

        Mockito.verify(markdownGateway, Mockito.times(1)).getPolicyByProductId(productId);
    }

    @Test
    public void percentageMarkdownApplied() throws JsonProcessingException {

        var productId = new ProductID(UUID.randomUUID());
        var request = new FinalPriceRequest();
        request.productId = productId.id().toString();
        request.productPrice = 1d;
        request.quantity = 100;

        var policy = Optional.of(new Markdown(new MarkdownID(UUID.randomUUID()), new PercentageMarkdownPolicy(50f)));

        Mockito.when(markdownGateway.getPolicyByProductId(productId)).thenReturn(policy);

        given()
                .body(jsonMapper.writeValueAsString(request))
                .contentType(ContentType.JSON)
                .when()
                .get(FINAL_PRICE_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is("50.0"));

        Mockito.verify(markdownGateway, Mockito.times(1)).getPolicyByProductId(productId);
    }

    @ParameterizedTest
    @CsvSource({
            "20,20.0",
            "30,21.0",
            "60,24.0",
            "100,10.0"
    })
    public void countMarkdownApplied(int quantity, String expectedPrice) throws JsonProcessingException {

        var productId = new ProductID(UUID.randomUUID());
        var request = new FinalPriceRequest();
        request.productId = productId.id().toString();
        request.productPrice = 1d;
        request.quantity = quantity;

        var thresholds = new HashMap<Integer, Float>() {{
            put(30, 30.0f);
            put(60, 60.0f);
            put(90, 90.0f);
        }};
        var policy = Optional.of(new Markdown(new MarkdownID(UUID.randomUUID()), new CountMarkdownPolicy(thresholds)));

        Mockito.when(markdownGateway.getPolicyByProductId(productId)).thenReturn(policy);

        given()
                .body(jsonMapper.writeValueAsString(request))
                .contentType(ContentType.JSON)
                .when()
                .get(FINAL_PRICE_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is(expectedPrice));

        Mockito.verify(markdownGateway, Mockito.times(1)).getPolicyByProductId(productId);
    }

}