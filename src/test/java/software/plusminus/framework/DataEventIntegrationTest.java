package software.plusminus.framework;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.plusminus.framework.fixtures.RecordingDataEventListener;
import software.plusminus.framework.fixtures.TestEntity;
import software.plusminus.framework.fixtures.TestRepository;

import static software.plusminus.check.Checks.check;

/* Covers plusminus-data-event: repository operations publish before/after events
   that application listeners can consume. */
public class DataEventIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private TestRepository repository;
    @Autowired
    private RecordingDataEventListener listener;

    @Before
    public void clearListener() {
        listener.clear();
    }

    @Test
    public void repositoryOperationsPublishEvents() {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);

        TestEntity saved = call(TENANT, CURRENT_DEVICE, () -> repository.save(entity));
        check(listener.created()).hasSize(1);
        check(listener.created().get(0).getMyField()).is(entity.getMyField());

        TestEntity fetched = call(TENANT, CURRENT_DEVICE, () -> repository.getById(saved.getId()));
        check(listener.read()).hasSize(1);

        fetched.setMyField("updated");
        TestEntity updated = call(TENANT, CURRENT_DEVICE, () -> repository.save(fetched));
        check(listener.updated()).hasSize(1);

        TestEntity toDelete = call(TENANT, CURRENT_DEVICE, () -> repository.getById(updated.getId()));
        run(TENANT, CURRENT_DEVICE, () -> repository.delete(toDelete));
        check(listener.deleted()).hasSize(1);
    }
}
