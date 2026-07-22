package software.plusminus.framework;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.plusminus.audit.model.AuditLog;
import software.plusminus.data.event.CrudAction;
import software.plusminus.framework.fixtures.TestEntity;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-audit + plusminus-data-event + plusminus-transactional + plusminus-tenant:
   every CRUD operation on an @Auditable entity must produce an AuditLog row. */
public class AuditLogIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    public void createWritesAuditLog() {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(CURRENT_DEVICE, entity);

        AuditLog<?> log = entityManager.find(AuditLog.class, 1L);
        assertThat(log).isNotNull();
        check(log.getEntityType()).is(TestEntity.class.getName());
        check(log.getEntityId()).is(entity.getId());
        check(log.getAction()).is(CrudAction.CREATE);
        check(log.isCurrent()).is(true);
        check(log.getTenant()).is(TENANT);
        check(log.getDevice()).is(CURRENT_DEVICE);
    }

    @Test
    public void updateMarksPreviousLogAsNotCurrent() {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(CURRENT_DEVICE, entity);

        entity.setMyField("updated");
        update(OTHER_DEVICE, entity);

        AuditLog<?> createLog = entityManager.find(AuditLog.class, 1L);
        AuditLog<?> updateLog = entityManager.find(AuditLog.class, 2L);
        assertThat(createLog).isNotNull();
        assertThat(updateLog).isNotNull();
        check(createLog.isCurrent()).is(false);
        check(updateLog.isCurrent()).is(true);
        check(updateLog.getAction()).is(CrudAction.UPDATE);
        check(updateLog.getDevice()).is(OTHER_DEVICE);
    }

    @Test
    public void deleteWritesAuditLog() {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(TENANT);
        create(CURRENT_DEVICE, entity);
        remove(CURRENT_DEVICE, entity);

        AuditLog<?> deleteLog = entityManager.find(AuditLog.class, 2L);
        assertThat(deleteLog).isNotNull();
        check(deleteLog.getAction()).is(CrudAction.DELETE);
        check(deleteLog.isCurrent()).is(true);
    }
}
