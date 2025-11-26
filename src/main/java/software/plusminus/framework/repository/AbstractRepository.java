package software.plusminus.framework.repository;

import org.springframework.data.repository.NoRepositoryBean;
import software.plusminus.data.repository.CrudRepository;
import software.plusminus.framework.model.AbstractEntity;

@NoRepositoryBean
public interface AbstractRepository<T extends AbstractEntity> extends CrudRepository<T, Long> {
}
