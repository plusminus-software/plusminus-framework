package software.plusminus.framework;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Set;

import static software.plusminus.check.Checks.check;

/* Covers plusminus-authorization: the admin panel controller is annotated with @Role("admin"),
   so RoleAuthorizer must let an authenticated admin through and reject an authenticated
   non-admin with 403. Both requests carry a valid token, so only the role decides the outcome. */
public class AuthorizationIntegrationTest extends FrameworkIntegrationTest {

    @Test
    public void roleProtectedEndpointIsAllowedForRequiredRole() {
        ResponseEntity<String> response = get("/admin", Collections.singleton("admin"));

        check(response.getStatusCodeValue()).is(200);
    }

    @Test
    public void roleProtectedEndpointIsForbiddenWithoutRequiredRole() {
        ResponseEntity<String> response = get("/admin", Collections.singleton("user"));

        check(response.getStatusCodeValue()).is(403);
    }

    @Test
    public void roleProtectedEndpointIsForbiddenWithoutAnyRole() {
        ResponseEntity<String> response = get("/admin", Collections.emptySet());

        check(response.getStatusCodeValue()).is(403);
    }

    private ResponseEntity<String> get(String path, Set<String> roles) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken(TENANT, CURRENT_DEVICE, roles));
        return restTemplate().exchange(web().url() + path, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
    }
}
