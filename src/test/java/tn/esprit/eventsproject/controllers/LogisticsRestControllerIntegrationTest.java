package tn.esprit.eventsproject.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.esprit.eventsproject.repositories.LogisticsRepository;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:logistics-api;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class LogisticsRestControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private LogisticsRepository logisticsRepository;

    @BeforeEach
    void clearLogistics() {
        logisticsRepository.deleteAll();
    }

    @Test
    void logisticsCanBeCreatedReadUpdatedAndDeleted() {
        Map<String, Object> logistics = new HashMap<>();
        logistics.put("description", "Projecteur");
        logistics.put("reserve", true);
        logistics.put("prixUnit", 100.0);
        logistics.put("quantite", 2);

        ResponseEntity<Map> created = restTemplate.postForEntity(
                url("/logistics"), logistics, Map.class);

        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertNotNull(created.getBody());
        int id = ((Number) created.getBody().get("idLog")).intValue();

        ResponseEntity<Map> read = restTemplate.getForEntity(
                url("/logistics/" + id), Map.class);
        assertEquals(HttpStatus.OK, read.getStatusCode());
        assertEquals("Projecteur", read.getBody().get("description"));

        logistics.put("description", "Projecteur HD");
        ResponseEntity<Map> updated = restTemplate.exchange(
                url("/logistics/" + id),
                HttpMethod.PUT,
                new HttpEntity<>(logistics),
                Map.class);
        assertEquals(HttpStatus.OK, updated.getStatusCode());
        assertEquals("Projecteur HD", updated.getBody().get("description"));

        ResponseEntity<Void> deleted = restTemplate.exchange(
                url("/logistics/" + id),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Void.class);
        assertEquals(HttpStatus.NO_CONTENT, deleted.getStatusCode());

        assertEquals(HttpStatus.NOT_FOUND, restTemplate.getForEntity(
                url("/logistics/" + id), Map.class).getStatusCode());
    }

    @Test
    void openApiSpecificationIsAvailable() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                url("/v3/api-docs"), String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("/logistics"));
    }

    private String url(String path) {
        return "http://localhost:" + port + "/events" + path;
    }
}
