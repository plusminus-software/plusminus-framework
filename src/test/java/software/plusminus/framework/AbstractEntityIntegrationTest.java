package software.plusminus.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import software.plusminus.framework.fixtures.TestEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

public class AbstractEntityIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private TestEntity entity1;
    private TestEntity entity2;
    private TestEntity entity3;

    @Before
    public void before() {
        entity1 = readTestEntity();
        entity1.setId(null);
        entity1.setVersion(null);
        entity1.setTenant(TENANT);
        create(OTHER_DEVICE, entity1);

        entity2 = readTestEntity();
        entity2.setId(null);
        entity2.setVersion(null);
        entity2.setTenant(TENANT);
        create(OTHER_DEVICE, entity2);

        TestEntity entityWithUnknownTenant = readTestEntity();
        entityWithUnknownTenant.setId(null);
        entityWithUnknownTenant.setVersion(null);
        entityWithUnknownTenant.setTenant("Unknown tenant");
        create(OTHER_DEVICE, entityWithUnknownTenant);

        TestEntity entityWithEmptyTenant = readTestEntity();
        entityWithEmptyTenant.setId(null);
        entityWithEmptyTenant.setVersion(null);
        entityWithEmptyTenant.setTenant("");
        create(OTHER_DEVICE, entityWithEmptyTenant);

        TestEntity deletedEntity = readTestEntity();
        deletedEntity.setId(null);
        deletedEntity.setVersion(null);
        deletedEntity.setTenant(TENANT);
        deletedEntity.setDeleted(Boolean.TRUE);
        create(OTHER_DEVICE, deletedEntity);

        entity3 = readTestEntity();
        entity3.setId(null);
        entity3.setVersion(null);
        entity3.setTenant(TENANT);
        create(OTHER_DEVICE, entity3);
    }

    @Test
    public void filteredByTenantAndSoftDelete() throws IOException {
        ResponseEntity<String> response = httpGet("/test?page=0&size=100&sort=id,asc");

        check(response.getStatusCodeValue()).is(200);
        JsonNode content = objectMapper.readTree(response.getBody()).get("content");
        assertThat(content).isNotNull();
        check(content.size()).is(3);
        check(content.get(0).get("id").asLong()).is(entity1.getId());
        check(content.get(1).get("id").asLong()).is(entity2.getId());
        check(content.get(2).get("id").asLong()).is(entity3.getId());
        assertThat(content.get(0).get("modifiedTime")).isNotNull();
        assertThat(content.get(0).get("uuid").asText()).isEqualTo(entity1.getUuid().toString());
    }

    @Test
    public void unauthenticatedRequestIsRejected() {
        ResponseEntity<String> response = restTemplate()
                .getForEntity(web().url() + "/test?page=0&size=100", String.class);

        assertThat(response.getStatusCodeValue()).isGreaterThanOrEqualTo(400);
    }
}
