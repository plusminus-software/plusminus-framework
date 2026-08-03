package software.plusminus.framework;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import software.amazon.awssdk.services.s3.S3Client;
import software.plusminus.collection.TypedMap;
import software.plusminus.file.service.FileService;
import software.plusminus.framework.fixtures.InMemoryFileService;
import software.plusminus.framework.fixtures.TestEntity;
import software.plusminus.job.Job;
import software.plusminus.job.JobStatus;
import software.plusminus.job.Step;
import software.plusminus.metadata.MetadataContext;
import software.plusminus.patch.service.PatchService;
import software.plusminus.spring.SpringUtil;
import software.plusminus.tenant.annotation.Tenant;
import software.plusminus.type.ParseService;
import software.plusminus.type.model.Type;
import software.plusminus.util.EntityUtils;
import software.plusminus.util.FieldUtils;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static software.plusminus.check.Checks.check;

/* Covers the standalone plusminus modules working inside one framework application context:
   type, patch, job, collection, spring, metadata, utils, file/s3 autoconfig. */
public class ModulesIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ParseService parseService;
    @Autowired
    private PatchService patchService;

    @Test
    public void typeModuleParsesEntity() {
        Type type = parseService.parse(TestEntity.class);

        check(type.getName()).is("TestEntity");
        assertThat(type.getFields()).anySatisfy(field -> assertThat(field.getName()).isEqualTo("myField"));
    }

    @Test
    public void patchModulePatchesObject() {
        TestEntity target = readTestEntity();
        TestEntity patch = new TestEntity();
        patch.setMyField("patched value");
        patch.setId(null);
        patch.setUuid(null);
        patch.setDeleted(null);

        patchService.patch(patch, target);

        check(target.getMyField()).is("patched value");
        check(target.getId()).is(1L);
    }

    @Test
    public void jobModuleRunsSteps() {
        AtomicInteger counter = new AtomicInteger();
        Job job = new Job();
        job.addStep(Step.of(counter::incrementAndGet));
        job.addStep(Step.of(counter::incrementAndGet));

        job.run();

        check(job.getStatus()).is(JobStatus.SUCCESS);
        check(counter.get()).is(2);
    }

    @Test
    public void collectionModuleIndexesByType() {
        TypedMap<Object> map = new TypedMap<>();
        TestEntity entity = readTestEntity();
        map.put(entity);

        assertThat(map.get(TestEntity.class)).containsExactly(entity);
    }

    @Test
    public void springModuleExposesApplicationContext() {
        assertThat(SpringUtil.isRunningInSpring()).isTrue();
        assertThat(SpringUtil.getApplicationContext()).isNotNull();
    }

    @Test
    public void metadataModuleRegistersEntities() {
        Class<Object> entityClass = MetadataContext.getClass("TestEntity");

        assertThat(entityClass).isEqualTo(TestEntity.class);
    }

    @Test
    public void utilsModuleFindsEntityId() {
        TestEntity entity = readTestEntity();

        check(EntityUtils.findId(entity, Long.class)).is(1L);
    }

    @Test
    public void utilsModuleFindsAnnotatedFieldOfFrameworkEntity() {
        Optional<Field> field = FieldUtils.findFirstWithAnnotation(TestEntity.class, Tenant.class);

        check(field).isPresent();
        check(field.get().getName()).is("tenant");
    }

    @Test
    public void s3AutoconfigBacksOffWhenFileServiceIsPresent() {
        assertThat(applicationContext.getBean(FileService.class)).isInstanceOf(InMemoryFileService.class);
        assertThat(applicationContext.getBean(S3Client.class)).isNotNull();
    }
}
