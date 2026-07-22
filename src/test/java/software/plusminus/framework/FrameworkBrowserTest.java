package software.plusminus.framework;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import software.plusminus.audit.context.DeviceContext;
import software.plusminus.jwt.service.IssuerContext;
import software.plusminus.security.Security;
import software.plusminus.tenant.context.TenantContext;
import software.plusminus.test.BrowserTest;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

/* Declares the same @SpyBean set as FrameworkIntegrationTest so that Spring's test context cache
   can reuse a single application context across browser and non-browser framework tests. */
public abstract class FrameworkBrowserTest extends BrowserTest {

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    /* tenantContext and deviceContext are unused here but keep the @SpyBean set identical
       to FrameworkIntegrationTest so both test families share one cached application context */
    @SuppressWarnings("PMD.UnusedPrivateField")
    @SpyBean
    private TenantContext tenantContext;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @SpyBean
    private DeviceContext deviceContext;
    @SpyBean
    private IssuerContext issuerContext;

    @Override
    public void login(String username, String... roles) {
        /* Tokens are generated outside of an HTTP request: stub the issuer the same way
           FrameworkIntegrationTest.generateToken does */
        doReturn("localhost").when(issuerContext).get();
        try {
            super.login(username, roles);
        } finally {
            doCallRealMethod().when(issuerContext).get();
        }
    }

    protected String generateToken(Security security) {
        doReturn("localhost").when(issuerContext).get();
        try {
            return security().generateToken(security);
        } finally {
            doCallRealMethod().when(issuerContext).get();
        }
    }

    protected TestRestTemplate restTemplate() {
        return new TestRestTemplate(restTemplateBuilder);
    }
}
