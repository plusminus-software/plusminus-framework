package software.plusminus.framework.fixtures;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.plusminus.data.controller.CrudController;

@RestController
@RequestMapping("/test")
@Profile("test")
public class TestController extends CrudController<TestEntity, Long> {
}
