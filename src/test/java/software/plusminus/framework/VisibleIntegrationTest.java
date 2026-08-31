package software.plusminus.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import software.plusminus.framework.fixtures.TestEntity;
import software.plusminus.visible.annotation.Visible;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

public class VisibleIntegrationTest extends FrameworkIntegrationTest {

    private static final Set<String> ADMIN = Collections.singleton(Visible.ADMIN);
    private static final Set<String> NO_ROLES = Collections.emptySet();

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void hidesTenantFromCallerWithoutTheRole() throws IOException {
        TestEntity entity = create(OTHER_DEVICE, newEntity(null));

        JsonNode json = get("/test/" + entity.getId(), NO_ROLES);

        assertThat(json.get("tenant")).isNull();
    }

    @Test
    public void showsTenantToAdmin() throws IOException {
        TestEntity entity = create(OTHER_DEVICE, newEntity(null));

        JsonNode json = get("/test/" + entity.getId(), ADMIN);

        check(json.get("tenant").asText()).is(TENANT);
    }

    @Test
    public void keepsEveryUnrestrictedPropertyForCallerWithoutTheRole() throws IOException {
        TestEntity entity = create(OTHER_DEVICE, newEntity(null));

        JsonNode json = get("/test/" + entity.getId(), NO_ROLES);

        check(json.get("class").asText()).is("TestEntity");
        check(json.get("id").asLong()).is(entity.getId());
        check(json.get("uuid").asText()).is(entity.getUuid().toString());
        check(json.get("version").asLong()).is(entity.getVersion());
        check(json.get("myField").asText()).is(entity.getMyField());
        assertThat(json.get("modifiedTime")).isNotNull();
    }

    @Test
    public void hidesTenantInsideANestedReference() throws IOException {
        TestEntity parent = create(OTHER_DEVICE, newEntity(null));
        TestEntity child = create(OTHER_DEVICE, newEntity(parent));

        JsonNode reference = get("/test/" + child.getId(), NO_ROLES).get("reference");

        check(reference.get("myField").asText()).is(parent.getMyField());
        assertThat(reference.get("tenant")).isNull();
    }

    @Test
    public void showsTenantInsideANestedReferenceToAdmin() throws IOException {
        TestEntity parent = create(OTHER_DEVICE, newEntity(null));
        TestEntity child = create(OTHER_DEVICE, newEntity(parent));

        JsonNode reference = get("/test/" + child.getId(), ADMIN).get("reference");

        check(reference.get("tenant").asText()).is(TENANT);
    }

    /* A dehydrated stub is written by DehydrationSerializer itself and never reaches the property
       writers plusminus-visible wraps, so what it may carry is decided by that serializer rather
       than by the annotations: only :dehydrated, class, id and uuid, none of which is tenant. */
    @Test
    public void dehydratedStubCarriesNoTenantForEitherCaller() throws IOException {
        TestEntity parent = create(OTHER_DEVICE, newEntity(null));
        TestEntity child = create(OTHER_DEVICE, newEntity(parent));
        String path = "/test/" + child.getId() + "?dehydrate=true";

        JsonNode asAnonymous = get(path, NO_ROLES).get("reference");
        JsonNode asAdmin = get(path, ADMIN).get("reference");

        check(asAnonymous.get(":dehydrated").asBoolean()).is(true);
        check(asAnonymous.get("id").asLong()).is(parent.getId());
        check(asAnonymous.get("uuid").asText()).is(parent.getUuid().toString());
        assertThat(asAnonymous.get("tenant")).isNull();
        check(asAdmin.get(":dehydrated").asBoolean()).is(true);
        assertThat(asAdmin.get("tenant")).isNull();
    }

    /* The root of a dehydrated response is still serialized in full, so it does go through the
       wrapped property writers and tenant is filtered there as on any other response. */
    @Test
    public void dehydratedRootStillFiltersTenant() throws IOException {
        TestEntity parent = create(OTHER_DEVICE, newEntity(null));
        TestEntity child = create(OTHER_DEVICE, newEntity(parent));
        String path = "/test/" + child.getId() + "?dehydrate=true";

        JsonNode asAnonymous = get(path, NO_ROLES);
        JsonNode asAdmin = get(path, ADMIN);

        check(asAnonymous.get("myField").asText()).is(child.getMyField());
        assertThat(asAnonymous.get("tenant")).isNull();
        check(asAdmin.get("tenant").asText()).is(TENANT);
    }

    /* Jackson caches one serializer per class in the application's single ObjectMapper, and
       plusminus-dehydration wraps that cached serializer once more. Neither may remember who was
       asking, so an admin request must not fix the shape for the ordinary caller behind it. */
    @Test
    public void firstCallerDoesNotFixTheShapeForTheNext() throws IOException {
        TestEntity entity = create(OTHER_DEVICE, newEntity(null));
        String path = "/test/" + entity.getId();

        JsonNode asAdmin = get(path, ADMIN);
        JsonNode asAnonymous = get(path, NO_ROLES);

        check(asAdmin.get("tenant").asText()).is(TENANT);
        assertThat(asAnonymous.get("tenant")).isNull();
    }

    @Test
    public void hidesTenantInsideAPage() throws IOException {
        create(OTHER_DEVICE, newEntity(null));

        JsonNode content = get("/test?page=0&size=100", NO_ROLES).get("content");

        check(content.size()).is(1);
        check(content.get(0).get("class").asText()).is("TestEntity");
        assertThat(content.get(0).get("tenant")).isNull();
    }

    private JsonNode get(String path, Set<String> roles) throws IOException {
        ResponseEntity<String> response = httpGet(path, roles);
        check(response.getStatusCodeValue()).is(200);
        return objectMapper.readTree(response.getBody());
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
