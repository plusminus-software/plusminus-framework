package software.plusminus.framework.fixtures;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.Context;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@Profile("test")
@AllArgsConstructor
public class HttpContextController {

    private Context<HttpServletRequest> requestContext;
    private Context<HttpServletResponse> responseContext;
    private Context<HandlerMethod> handlerMethodContext;

    @GetMapping("/http-context")
    public Map<String, Object> httpContext() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("requestUri", requestContext.get().getRequestURI());
        result.put("responsePresent", responseContext.get() != null);
        result.put("handlerMethod", handlerMethodContext.get().getMethod().getName());
        return result;
    }
}
