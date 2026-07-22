package software.plusminus.framework.fixtures;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import software.plusminus.authentication.annotation.Public;

@Controller
@Profile("test")
public class LoginPageController {

    @Public
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, Model model) {
        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        return "index";
    }

    @GetMapping("/")
    public String indexPage() {
        return "logged-in";
    }
}
