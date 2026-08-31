package software.plusminus.framework;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import software.plusminus.check.util.JsonUtil;
import software.plusminus.framework.fixtures.TestEntity;
import software.plusminus.sync.dto.Sync;
import software.plusminus.sync.dto.SyncType;

import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-sync + plusminus-audit + plusminus-data + plusminus-security working together:
   the /sync endpoint exposes audit-derived changes and applies incoming changes. */
public class SyncEndpointIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void readReturnsChangesOfOtherDevices() {
        TestEntity entity1 = readTestEntity();
        entity1.setId(null);
        entity1.setVersion(null);
        entity1.setTenant(TENANT);
        create(OTHER_DEVICE, entity1);

        TestEntity entity2 = readTestEntity();
        entity2.setId(null);
        entity2.setVersion(null);
        entity2.setTenant(TENANT);
        create(OTHER_DEVICE, entity2);

        entity1.setTenant(null);
        entity2.setTenant(null);
        List<Sync<TestEntity>> expected = Arrays.asList(
                Sync.of(entity1, SyncType.CREATE, 1L, null),
                Sync.of(entity2, SyncType.CREATE, 2L, null));

        ResponseEntity<String> response = httpGet("/sync?types=TestEntity");

        check(response.getStatusCodeValue()).is(200);
        check(response.getBody()).isJson().is(JsonUtil.toJson(expected));
    }

    @Test
    public void readExcludesCurrentDevice() {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(CURRENT_DEVICE, entity);

        ResponseEntity<String> response = httpGet("/sync?types=TestEntity");

        check(response.getStatusCodeValue()).is(200);
        check(response.getBody()).isJson().is("[]");
    }

    @Test
    public void writeCreatesEntity() throws Exception {
        TestEntity newEntity = new TestEntity();
        newEntity.setMyField("synced entity");

        List<Sync<TestEntity>> items = Arrays.asList(
                Sync.of(newEntity, SyncType.CREATE, null, null));
        String json = objectMapper.writerFor(new TypeReference<List<Sync<TestEntity>>>() {})
                .writeValueAsString(items);

        ResponseEntity<String> response = httpPost("/sync", json);

        check(response.getStatusCodeValue()).is(200);
        assertThat(response.getBody()).isNotEmpty();
        TestEntity inDatabase = entityManager.find(TestEntity.class, 1L);
        assertThat(inDatabase).isNotNull();
        check(inDatabase.getMyField()).is("synced entity");
        check(inDatabase.getTenant()).is(TENANT);
    }
}
