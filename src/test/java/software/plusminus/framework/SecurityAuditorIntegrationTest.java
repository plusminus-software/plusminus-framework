package software.plusminus.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import software.plusminus.framework.fixtures.TestEntity;

import java.io.IOException;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-security: the framework wires SecurityAuditorAware as Spring Data's AuditorAware
   (see AuditingConfig), so @CreatedBy/@LastModifiedBy fields are populated with the username taken
   from the authenticated request's Security context, and stay empty outside of a request. */
public class SecurityAuditorIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void createOverHttpSetsAuditorFromToken() throws IOException {
        ResponseEntity<String> response = httpPost("/test", "{\"class\":\"TestEntity\",\"myField\":\"created\"}");

        check(response.getStatusCodeValue()).is(201);
        JsonNode json = objectMapper.readTree(response.getBody());
        check(json.get("createdBy").asText()).is(USERNAME);
        check(json.get("modifiedBy").asText()).is(USERNAME);
    }

    @Test
    public void updateOverHttpSetsModifiedBy() throws IOException {
        TestEntity entity = newEntity();
        create(OTHER_DEVICE, entity);
        /* created outside of a request: no authenticated user, so no auditor */
        assertThat(entity.getCreatedBy()).isNull();

        entity.setMyField("updated");
        ResponseEntity<String> response = httpPut("/test", objectMapper.writeValueAsString(entity));

        check(response.getStatusCodeValue()).is(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        check(json.get("modifiedBy").asText()).is(USERNAME);
        /* @CreatedBy is not updatable: it stays empty because it was written outside a request */
        assertThat(json.get("createdBy")).isNull();
    }

    @Test
    public void auditorIsEmptyWithoutAuthenticatedUser() {
        TestEntity entity = newEntity();
        create(OTHER_DEVICE, entity);

        TestEntity inDatabase = entityManager.find(TestEntity.class, entity.getId());
        assertThat(inDatabase.getCreatedBy()).isNull();
        assertThat(inDatabase.getModifiedBy()).isNull();
    }

    private TestEntity newEntity() {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        return entity;
    }
}
