package software.plusminus.framework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import software.plusminus.audit.context.DeviceContext;
import software.plusminus.check.util.JsonUtil;
import software.plusminus.data.service.DataService;
import software.plusminus.framework.fixtures.TestEntity;
import software.plusminus.jwt.service.IssuerContext;
import software.plusminus.security.Security;
import software.plusminus.tenant.context.TenantContext;
import software.plusminus.test.IntegrationTest;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

/* All framework integration tests share this single set of spies so that Spring's test context cache
   reuses one application context for the whole module: @SpyBean/@MockBean declarations are part
   of the cache key, so per-class declarations would boot a separate context per class. */
public abstract class FrameworkIntegrationTest extends IntegrationTest {

    protected static final String TENANT = "localhost";
    protected static final String USERNAME = "TestUser";
    protected static final String CURRENT_DEVICE = "CurrentDevice";
    protected static final String OTHER_DEVICE = "OtherDevice";

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;
    @Autowired
    private DataService dataService;

    @SpyBean
    private TenantContext tenantContext;
    @SpyBean
    private DeviceContext deviceContext;
    @SpyBean
    private IssuerContext issuerContext;

    protected DataService dataService() {
        return dataService;
    }

    protected TenantContext tenantContext() {
        return tenantContext;
    }

    protected DeviceContext deviceContext() {
        return deviceContext;
    }

    protected IssuerContext issuerContext() {
        return issuerContext;
    }

    protected TestRestTemplate restTemplate() {
        return new TestRestTemplate(restTemplateBuilder);
    }

    protected ResponseEntity<String> httpGet(String path) {
        return exchange(path, HttpMethod.GET, null);
    }

    protected ResponseEntity<String> httpGet(String path, Set<String> roles) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken(TENANT, CURRENT_DEVICE, roles));
        return restTemplate().exchange(web().url() + path, HttpMethod.GET,
                new HttpEntity<>(headers), String.class);
    }

    protected ResponseEntity<String> httpPost(String path, String json) {
        return exchange(path, HttpMethod.POST, json);
    }

    protected ResponseEntity<String> httpPut(String path, String json) {
        return exchange(path, HttpMethod.PUT, json);
    }

    protected ResponseEntity<String> httpPatch(String path, String json) {
        return exchange(path, HttpMethod.PATCH, json);
    }

    protected ResponseEntity<String> httpDelete(String path, String json) {
        return exchange(path, HttpMethod.DELETE, json);
    }

    protected ResponseEntity<String> exchange(String path, HttpMethod method, String json) {
        HttpHeaders headers = authHeaders();
        HttpEntity<String> entity;
        if (json == null) {
            entity = new HttpEntity<>(headers);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
            entity = new HttpEntity<>(json, headers);
        }
        return restTemplate().exchange(web().url() + path, method, entity, String.class);
    }

    protected HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken(TENANT, CURRENT_DEVICE));
        return headers;
    }

    protected String generateToken(String tenant, String device) {
        return generateToken(tenant, device, Collections.emptySet());
    }

    protected String generateToken(String tenant, String device, Set<String> roles) {
        doReturn(tenant).when(issuerContext).get();
        try {
            Map<String, String> parameters = new HashMap<>();
            parameters.put("tenant", tenant);
            parameters.put("device", device);
            return security().generateToken(Security.builder()
                    .username(USERNAME)
                    .roles(roles)
                    .parameters(parameters)
                    .build());
        } finally {
            doCallRealMethod().when(issuerContext).get();
        }
    }

    protected TestEntity create(String device, TestEntity entity) {
        return call(entity.getTenant(), device, () -> dataService.create(entity));
    }

    protected TestEntity update(String device, TestEntity entity) {
        return call(entity.getTenant(), device, () -> dataService.update(entity));
    }

    protected void remove(String device, TestEntity entity) {
        run(entity.getTenant(), device, () -> dataService.delete(entity));
    }

    protected <T> T call(String tenant, String device, Supplier<T> supplier) {
        AtomicReference<T> container = new AtomicReference<>();
        run(tenant, device, () -> container.set(supplier.get()));
        return container.get();
    }

    protected void run(String tenant, String device, Runnable runnable) {
        doReturn(device).when(deviceContext).get();
        doReturn(tenant).when(tenantContext).get();
        data().transaction().run(runnable);
        doCallRealMethod().when(tenantContext).get();
        doCallRealMethod().when(deviceContext).get();
    }

    protected TestEntity readTestEntity() {
        return JsonUtil.fromJson("/json/test-entity.json", TestEntity.class);
    }
}
