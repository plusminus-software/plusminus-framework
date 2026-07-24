package software.plusminus.framework;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import software.plusminus.framework.fixtures.RecordingScopeListener;
import software.plusminus.scope.ScopeRunner;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-scope: ScopeRunner is a Spring bean that runs a scope and publishes
   started/completed/failed/finalized lifecycle events that application listeners consume. */
public class ScopeIntegrationTest extends FrameworkIntegrationTest {

    @Autowired
    private ScopeRunner scopeRunner;
    @Autowired
    private RecordingScopeListener listener;

    @Before
    public void clearListener() {
        listener.clear();
    }

    @Test
    public void successfulScopePublishesStartedCompletedFinalized() {
        AtomicBoolean executed = new AtomicBoolean();

        scopeRunner.run(this, () -> executed.set(true));

        check(executed.get()).is(true);
        check(listener.started()).is(1);
        check(listener.completed()).is(1);
        check(listener.finalized()).is(1);
        check(listener.failed()).is(0);
    }

    @Test
    public void failingScopePublishesFailedAndFinalized() {
        assertThatThrownBy(() -> scopeRunner.run(this, () -> {
            throw new IllegalStateException("boom");
        })).isInstanceOf(IllegalStateException.class);

        check(listener.started()).is(1);
        check(listener.completed()).is(0);
        check(listener.failed()).is(1);
        check(listener.finalized()).is(1);
    }
}
