package software.plusminus.framework;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.plusminus.framework.fixtures.TestEntity;
import software.plusminus.generator.java.CrudControllerGenerator;
import software.plusminus.generator.java.CrudRepositoryGenerator;
import software.plusminus.generator.model.GeneratedFile;

import static org.assertj.core.api.Assertions.assertThat;

/* Covers plusminus-generator + plusminus-type:
   generators are Spring beans that produce code from framework entities. */
public class GeneratorIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private CrudRepositoryGenerator crudRepositoryGenerator;
    @Autowired
    private CrudControllerGenerator crudControllerGenerator;

    @Test
    public void generatesCrudRepository() {
        GeneratedFile file = crudRepositoryGenerator.generate(TestEntity.class);

        assertThat(file.getCode())
                .contains("public interface TestEntityRepository extends CrudRepository<TestEntity, Long>");
        assertThat(file.getPath().toString()).endsWith("TestEntityRepository.java");
    }

    @Test
    public void generatesCrudController() {
        GeneratedFile file = crudControllerGenerator.generate(TestEntity.class);

        assertThat(file.getCode()).contains("class TestEntityController");
        assertThat(file.getPath().toString()).endsWith("TestEntityController.java");
    }
}
