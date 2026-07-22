package software.plusminus.framework;

import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import software.plusminus.browser.BrowserSettings;
import software.plusminus.browser.Element;
import software.plusminus.security.Security;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-admin + plusminus-type + plusminus-browser + plusminus-selenium
   + plusminus-security: an authenticated browser opens the admin panel generated
   from the framework entity types. */
public class AdminPanelBrowserTest extends FrameworkBrowserTest {

    @Override
    protected BrowserSettings settings() {
        /* The admin page loads Vue/Buefy from public CDNs: console messages are not asserted here */
        return super.settings()
                .logsFilter(message -> false);
    }

    @Test
    public void adminPanelRenders() {
        go("/login");
        login("TestUser", "admin");
        go("/admin");

        Element app = find("#app").one();

        check(app.isDisplayed()).isTrue();
    }

    @Test
    public void adminPanelContainsFrameworkTypes() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + generateToken(Security.builder()
                .username("TestUser")
                .roles(Collections.singleton("admin"))
                .build()));

        ResponseEntity<String> response = restTemplate().exchange(web().url() + "/admin",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        check(response.getStatusCodeValue()).is(200);
        assertThat(response.getBody()).contains("id=\"app\"");
        assertThat(response.getBody()).contains("TestEntity");
    }
}
