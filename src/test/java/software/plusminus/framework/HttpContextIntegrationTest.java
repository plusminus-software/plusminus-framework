package software.plusminus.framework;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import software.plusminus.framework.fixtures.RecordingScopeListener;

import java.io.IOException;

import static software.plusminus.check.Checks.check;

/* Covers plusminus-http: HttpFilter + HttpInterceptor populate the request, response
   and handler-method contexts while a request is processed, and every HTTP request
   runs inside a scope so scope lifecycle events are published. */
public class HttpContextIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RecordingScopeListener scopeListener;

    @Before
    public void clearListener() {
        scopeListener.clear();
    }

    @Test
    public void contextsArePopulatedDuringRequest() throws IOException {
        ResponseEntity<String> response = httpGet("/http-context");

        check(response.getStatusCodeValue()).is(200);
        JsonNode json = objectMapper.readTree(response.getBody());
        check(json.get("requestUri").asText()).is("/http-context");
        check(json.get("responsePresent").asBoolean()).is(true);
        check(json.get("handlerMethod").asText()).is("httpContext");
    }

    @Test
    public void httpRequestRunsInScope() {
        httpGet("/http-context");

        check(scopeListener.started()).is(1);
        check(scopeListener.completed()).is(1);
        check(scopeListener.finalized()).is(1);
        check(scopeListener.failed()).is(0);
    }
}
