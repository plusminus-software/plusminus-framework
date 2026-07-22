package software.plusminus.framework;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.plusminus.browser.BrowserSettings;
import software.plusminus.user.model.User;
import software.plusminus.user.service.UserService;

import java.util.Collections;

import static software.plusminus.check.Checks.check;

/* Covers plusminus-browser + plusminus-selenium + plusminus-login + plusminus-user
   + plusminus-jwt + plusminus-authentication end to end: a real browser submits the login form,
   receives a token cookie and reaches a protected page. */
public class LoginBrowserTest extends FrameworkBrowserTest {

    private static final String EMAIL = "user1@localhost.com";
    private static final String PASSWORD = "test-password";

    @Autowired
    private UserService userService;

    @Before
    public void openLoginPage() {
        go("/login");
    }

    @Override
    protected BrowserSettings settings() {
        return super.settings()
                .logsFilter(message -> !message.contains("favicon"));
    }

    @Test
    public void loginThroughBrowser() {
        registerUser();
        find("#email").one().sendKeys(EMAIL);
        find("#password").one().sendKeys(PASSWORD);
        find("#submit").one().click();

        String greeting = browser().currentPage().find("#greeting").one().text();
        check(greeting).is("Logged in");
    }

    @Test
    public void badCredentialsShowError() {
        registerUser();
        find("#email").one().sendKeys(EMAIL);
        find("#password").one().sendKeys("wrong-password");
        find("#submit").one().click();

        String error = browser().currentPage().find("#error").one().text();
        check(error).is("Invalid username or password!");
    }

    @SuppressFBWarnings(value = "HARD_CODE_PASSWORD", justification = "Test credentials only")
    private void registerUser() {
        User user = new User();
        user.setUsername(EMAIL);
        user.setEmail(EMAIL);
        user.setPassword(PASSWORD);
        user.setTenant("localhost");
        user.setRoles(Collections.singleton("admin"));
        data().transaction().run(() -> userService.register(user));
    }
}
