package software.plusminus.framework.fixtures;

import org.springframework.context.annotation.Profile;
import software.plusminus.framework.repository.AbstractRepository;

@Profile("test")
public interface TestRepository extends AbstractRepository<TestEntity> {
}