package org.example.assignment.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.example.assignment.controller.dto.MarkdownDTO;
import org.example.assignment.gateway.MarkdownGateway;
import org.example.assignment.model.MarkdownConfiguration;
import org.example.assignment.model.MarkdownPolicySpecification;
import org.example.assignment.model.MarkdownType;
import org.example.assignment.model.ProductID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MarkdownControllerTest {
    private static final String MARKDOWN_BASE_PATH = "v1/pricing/markdowns";

    private final ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Inject
    @Named("MarkdownGateway")
    private MarkdownGateway markdownGateway;

    @AfterEach
    void resetData() {
        markdownGateway
                .getAll()
                .forEach(x -> markdownGateway.delete(x.id()));
    }

    @Test
    void getEmptyMarkdownList() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(MARKDOWN_BASE_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .body(is("[]"));
    }

    @Test
    void getMarkdownList() throws JsonProcessingException {

        var confDef = new MarkdownConfiguration(Optional.empty(), Optional.empty());
        var specDef = new MarkdownPolicySpecification(MarkdownType.DEFAULT, confDef);

        var confPerc = new MarkdownConfiguration(Optional.of(1f), Optional.empty());
        var specPerc = new MarkdownPolicySpecification(MarkdownType.PERCENTAGE, confPerc);

        var thresholds = new HashMap<Integer, Float>() {{
            put(30, 30.0f);
            put(60, 60.0f);
            put(90, 90.0f);
        }};
        var confCount = new MarkdownConfiguration(Optional.empty(), Optional.of(thresholds));
        var specCount = new MarkdownPolicySpecification(MarkdownType.COUNT, confCount);

        markdownGateway.createNew(specDef);
        markdownGateway.createNew(specPerc);
        markdownGateway.createNew(specCount);

        var strList = given()
                .contentType(ContentType.JSON)
                .when()
                .get(MARKDOWN_BASE_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract().body().asString();

        assertNotNull(strList);
        var mList = jsonMapper.readValue(strList, new TypeReference<List<MarkdownDTO>>(){});
        assertEquals(3, mList.size());
    }

    @Test
    void getMarkdownById() {

        var confDef = new MarkdownConfiguration(Optional.empty(), Optional.empty());
        var specDef = new MarkdownPolicySpecification(MarkdownType.DEFAULT, confDef);

        var id = markdownGateway.createNew(specDef);

        var markdown = given()
                .contentType(ContentType.JSON)
                .pathParam("id", id.get().id())
                .when()
                .get(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .body()
                .as(MarkdownDTO.class);

        assertNotNull(markdown);
        assertEquals(id.get().id(), markdown.id);
        assertEquals(MarkdownType.DEFAULT, markdown.type);
    }

    @Test
    void getMarkdownByIdNotFound() {

        var markdown = given()
                .contentType(ContentType.JSON)
                .pathParam("id", UUID.randomUUID().toString())
                .when()
                .get(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void addNewMarkdown() throws JsonProcessingException {

        MarkdownDTO markdown = new MarkdownDTO();
        markdown.type = MarkdownType.DEFAULT;

        var location = given()
                .contentType(ContentType.JSON)
                .body(jsonMapper.writeValueAsString(markdown))
                .when()
                .post(MARKDOWN_BASE_PATH)
                .then()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .header("Location");

        assertNotNull(location);
        var paths = location.split("/");
        var id = paths[paths.length-1];
        assertInstanceOf(UUID.class, UUID.fromString(id));
    }

    @Test
    void deleteMarkdown() throws JsonProcessingException {

        var confDef = new MarkdownConfiguration(Optional.empty(), Optional.empty());
        var specDef = new MarkdownPolicySpecification(MarkdownType.DEFAULT, confDef);

        var id = markdownGateway.createNew(specDef);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", UUID.randomUUID().toString())
                .when()
                .delete(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id.get().id())
                .when()
                .delete(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id.get().id())
                .when()
                .delete(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void updateMarkdown() throws JsonProcessingException {

        var confDef = new MarkdownConfiguration(Optional.empty(), Optional.empty());
        var specDef = new MarkdownPolicySpecification(MarkdownType.DEFAULT, confDef);

        var confPerc = new MarkdownConfiguration(Optional.of(1f), Optional.empty());
        var specPerc = new MarkdownPolicySpecification(MarkdownType.PERCENTAGE, confPerc);

        var thresholds = new HashMap<Integer, Float>() {{
            put(30, 30.0f);
            put(60, 60.0f);
            put(90, 90.0f);
        }};
        var confCount = new MarkdownConfiguration(Optional.empty(), Optional.of(thresholds));
        var specCount = new MarkdownPolicySpecification(MarkdownType.COUNT, confCount);

        var defId = markdownGateway.createNew(specDef);
        var percId = markdownGateway.createNew(specPerc);
        var countId =markdownGateway.createNew(specCount);

        MarkdownDTO updPercentage = new MarkdownDTO();
        updPercentage.type = MarkdownType.PERCENTAGE;
        updPercentage.percentage = 666f;

        var updThresholds = new HashMap<Integer, Float>() {{ put(50, 50.0f); }};
        MarkdownDTO updCount = new MarkdownDTO();
        updCount.type = MarkdownType.COUNT;
        updCount.thresholds = updThresholds;

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", defId.get().id())
                .body(jsonMapper.writeValueAsString(updPercentage))
                .when()
                .patch(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode());

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", percId.get().id())
                .body(jsonMapper.writeValueAsString(updPercentage))
                .when()
                .patch(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        var updatedPerc = markdownGateway.get(percId.get()).get();
        assertEquals(666f, updatedPerc.policy().describe().configuration().percentage().get());

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", countId.get().id())
                .body(jsonMapper.writeValueAsString(updCount))
                .when()
                .patch(MARKDOWN_BASE_PATH+"/{id}")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        var updatedCount = markdownGateway.get(countId.get()).get();
        assertEquals(1, updatedCount.policy().describe().configuration().thresholds().get().size());

    }

    @Test
    void associateMarkdownToProducts() throws JsonProcessingException {

        var confDef = new MarkdownConfiguration(Optional.empty(), Optional.empty());
        var specDef = new MarkdownPolicySpecification(MarkdownType.DEFAULT, confDef);

        var id = markdownGateway.createNew(specDef);
        var productsIds = IntStream.of(3)
                .mapToObj(__ -> UUID.randomUUID())
                .toList();

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", UUID.randomUUID())
                .body(jsonMapper.writeValueAsString(productsIds))
                .when()
                .post(MARKDOWN_BASE_PATH+"/{id}/associate")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id.get().id())
                .body(jsonMapper.writeValueAsString(productsIds))
                .when()
                .post(MARKDOWN_BASE_PATH+"/{id}/associations")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        productsIds.forEach(p -> assertEquals(id.get().id(), markdownGateway.getPolicyByProductId(new ProductID(p)).get().id().id()));
    }

    @Test
    void removeAssociationToProducts() throws JsonProcessingException {

        var confDef = new MarkdownConfiguration(Optional.empty(), Optional.empty());
        var specDef = new MarkdownPolicySpecification(MarkdownType.DEFAULT, confDef);

        var id = markdownGateway.createNew(specDef);
        var productsIds = IntStream.of(3)
                .mapToObj(__ -> UUID.randomUUID())
                .toList();

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", UUID.randomUUID())
                .body(jsonMapper.writeValueAsString(productsIds))
                .when()
                .delete(MARKDOWN_BASE_PATH+"/{id}/associations")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode());

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", id.get().id())
                .body(jsonMapper.writeValueAsString(productsIds))
                .when()
                .delete(MARKDOWN_BASE_PATH+"/{id}/associations")
                .then()
                .statusCode(Response.Status.OK.getStatusCode());

        productsIds.forEach(p -> assertTrue(markdownGateway.getPolicyByProductId(new ProductID(p)).isEmpty()));
    }
}