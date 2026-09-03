package com.example.productapp.integration;

import com.example.productapp.dto.ProductRequestDto;
import com.example.productapp.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full-stack integration test: real Spring context + real PostgreSQL (Testcontainers).
 * Exercises the REST API end-to-end through HTTP, verifying persistence.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductApiIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("productdb_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Test
    void fullCrudLifecycle_worksEndToEnd() {
        // CREATE
        ProductRequestDto createRequest = new ProductRequestDto("Integration Product", "desc",
                new BigDecimal("9.99"), 3);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(url("/api/products"), createRequest, Map.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Number id = (Number) createResponse.getBody().get("id");
        assertThat(id).isNotNull();

        // READ (single)
        ResponseEntity<Map> getResponse = restTemplate.getForEntity(url("/api/products/" + id), Map.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().get("name")).isEqualTo("Integration Product");

        // READ (list contains it)
        ResponseEntity<Map[]> listResponse = restTemplate.getForEntity(url("/api/products"), Map[].class);
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).extracting(m -> m.get("id")).contains(id);

        // UPDATE
        ProductRequestDto updateRequest = new ProductRequestDto("Updated Product", "new desc",
                new BigDecimal("15.50"), 7);
        restTemplate.put(url("/api/products/" + id), updateRequest);
        ResponseEntity<Map> afterUpdate = restTemplate.getForEntity(url("/api/products/" + id), Map.class);
        assertThat(afterUpdate.getBody().get("name")).isEqualTo("Updated Product");
        assertThat(afterUpdate.getBody().get("quantity")).isEqualTo(7);

        // DELETE
        restTemplate.delete(url("/api/products/" + id));
        ResponseEntity<Map> afterDelete = restTemplate.getForEntity(url("/api/products/" + id), Map.class);
        assertThat(afterDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getNonExistentProduct_returns404WithErrorBody() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url("/api/products/999999"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKeys("status", "message", "path");
    }

    @Test
    void createProduct_invalidPayload_returns400() {
        ProductRequestDto invalid = new ProductRequestDto("", "desc", new BigDecimal("-5.00"), -1);

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/products"), invalid, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void deleteThenDeleteAgain_returns404OnSecondCall() {
        ProductRequestDto createRequest = new ProductRequestDto("Temp", "desc", new BigDecimal("1.00"), 1);
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(url("/api/products"), createRequest, Map.class);
        Number id = (Number) createResponse.getBody().get("id");

        restTemplate.delete(url("/api/products/" + id));

        ResponseEntity<Void> secondDelete = restTemplate.exchange(
                url("/api/products/" + id), org.springframework.http.HttpMethod.DELETE, null, Void.class);
        assertThat(secondDelete.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
