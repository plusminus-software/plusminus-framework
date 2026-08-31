package software.plusminus.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import software.plusminus.framework.fixtures.TestEntity;

import java.io.IOException;
import javax.persistence.EntityManager;

import static software.plusminus.check.Checks.check;

/* Covers plusminus-data (CrudController + DataService), plusminus-json (class property),
   plusminus-tenant (tenant population from the token), plusminus-softdelete (delete hides rows),
   plusminus-security + plusminus-jwt (Bearer token auth) working together over HTTP. */
public class CrudIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void createOverHttp() throws IOException {
        ResponseEntity<String> response = httpPost("/test", "{\"class\":\"TestEntity\",\"myField\":\"created\"}");

        check(response.getStatusCodeValue()).is(201);
        JsonNode json = objectMapper.readTree(response.getBody());
        check(json.get("id").asLong()).is(1L);
        check(json.get("myField").asText()).is("created");
        check(json.get("tenant")).isNull();
        check(entityManager.find(TestEntity.class, 1L).getTenant()).is(TENANT);
        check(json.get("uuid").asText()).isNotEmpty();
        check(json.get("modifiedTime")).isNotNull();
        check(json.get("version").asLong()).is(0L);
    }

    @Test
    public void readById() throws IOException {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(OTHER_DEVICE, entity);

        ResponseEntity<String> response = httpGet("/test/" + entity.getId());

        check(response.getStatusCodeValue()).is(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        check(json.get("id").asLong()).is(entity.getId());
        check(json.get("myField").asText()).is(entity.getMyField());
    }

    @Test
    public void updateOverHttp() throws IOException {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(OTHER_DEVICE, entity);

        entity.setMyField("updated");
        ResponseEntity<String> response = httpPut("/test", objectMapper.writeValueAsString(entity));

        check(response.getStatusCodeValue()).is(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        check(json.get("myField").asText()).is("updated");
        check(json.get("version").asLong()).is(entity.getVersion() + 1);
    }

    @Test
    public void patchOverHttp() throws IOException {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(OTHER_DEVICE, entity);

        ResponseEntity<String> response = httpPatch("/test",
                "{\"class\":\"TestEntity\",\"id\":" + entity.getId() + ",\"myField\":\"patched\"}");

        check(response.getStatusCodeValue()).is(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        check(json.get("id").asLong()).is(entity.getId());
        check(json.get("myField").asText()).is("patched");
    }

    @Test
    public void deleteOverHttp() throws IOException {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(OTHER_DEVICE, entity);

        ResponseEntity<String> response = httpDelete("/test", objectMapper.writeValueAsString(entity));
        check(response.getStatusCodeValue()).is(204);

        ResponseEntity<String> page = httpGet("/test?page=0&size=100");
        JsonNode content = objectMapper.readTree(page.getBody()).get("content");
        check(content.size()).is(0);

        TestEntity inDatabase = entityManager.find(TestEntity.class, entity.getId());
        check(inDatabase).isNull();
    }
}
