package software.plusminus.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import software.plusminus.framework.fixtures.TestEntity;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-dehydration: when ?dehydrate=true is requested, nested @Entity references are
   serialized as lightweight {":dehydrated": true, class, id, uuid} stubs instead of full objects,
   while the requested entity itself is serialized in full. */
public class DehydrationIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void nestedReferenceIsDehydratedWhenRequested() throws IOException {
        TestEntity parent = create(OTHER_DEVICE, newEntity(null));
        TestEntity child = create(OTHER_DEVICE, newEntity(parent));

        ResponseEntity<String> response = httpGet("/test/" + child.getId() + "?dehydrate=true");

        check(response.getStatusCodeValue()).is(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        /* the requested entity is full, not dehydrated */
        assertThat(json.get(":dehydrated")).isNull();
        check(json.get("myField").asText()).is(child.getMyField());
        /* the nested reference is dehydrated to an id/uuid stub */
        JsonNode reference = json.get("reference");
        assertThat(reference).isNotNull();
        check(reference.get(":dehydrated").asBoolean()).is(true);
        check(reference.get("class").asText()).is("TestEntity");
        check(reference.get("id").asLong()).is(parent.getId());
        check(reference.get("uuid").asText()).is(parent.getUuid().toString());
        assertThat(reference.get("myField")).isNull();
    }

    @Test
    public void nestedReferenceIsFullWhenNotRequested() throws IOException {
        TestEntity parent = create(OTHER_DEVICE, newEntity(null));
        TestEntity child = create(OTHER_DEVICE, newEntity(parent));

        ResponseEntity<String> response = httpGet("/test/" + child.getId());

        check(response.getStatusCodeValue()).is(200);
        JsonNode reference = objectMapper.readTree(response.getBody()).get("reference");
        assertThat(reference).isNotNull();
        assertThat(reference.get(":dehydrated")).isNull();
        check(reference.get("myField").asText()).is(parent.getMyField());
    }

    private TestEntity newEntity(TestEntity reference) {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        entity.setReference(reference);
        return entity;
    }
}
