package software.plusminus.framework;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.plusminus.framework.fixtures.TestEntity;
import software.plusminus.hibernate.HibernateFilterService;

import javax.persistence.EntityManager;

import static software.plusminus.check.Checks.check;

/* Covers plusminus-hibernate: HibernateFilterService enables/disables the tenant + soft-delete
   Hibernate filters declared on AbstractEntity, changing which rows subsequent queries return.
   This is the low-level machinery that plusminus-tenant and plusminus-softdelete rely on. */
public class HibernateFilterIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private HibernateFilterService hibernateFilterService;
    @Autowired
    private EntityManager entityManager;

    @Test
    public void filtersHideOtherTenantAndDeletedRowsWhenEnabled() {
        TestEntity live = newEntity(TENANT, false);
        create(OTHER_DEVICE, live);
        TestEntity deleted = newEntity(TENANT, true);
        create(OTHER_DEVICE, deleted);
        TestEntity otherTenant = newEntity("Other tenant", false);
        create(OTHER_DEVICE, otherTenant);

        run(TENANT, CURRENT_DEVICE, () -> {
            hibernateFilterService.enableFilters();
            check(count()).is(1L);

            hibernateFilterService.disableFilters();
            check(count()).is(3L);
        });
    }

    private TestEntity newEntity(String tenant, boolean deleted) {
        TestEntity entity = readTestEntity();
        entity.setId(null);
        entity.setVersion(null);
        entity.setTenant(tenant);
        entity.setDeleted(deleted);
        return entity;
    }

    private long count() {
        return entityManager.createQuery("select count(e) from TestEntity e", Long.class)
                .getSingleResult();
    }
}
