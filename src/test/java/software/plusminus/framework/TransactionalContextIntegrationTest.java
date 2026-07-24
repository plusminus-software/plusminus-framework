package software.plusminus.framework;

import org.junit.Test;
import software.plusminus.transactional.context.TransactionalContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static software.plusminus.check.Checks.check;

/* Covers plusminus-transactional: the JPA transaction manager is wrapped by ContextTransactionManager,
   so TransactionalContext caches a value for the lifetime of the current transaction and throws
   when accessed outside of one. */
public class TransactionalContextIntegrationTest extends FrameworkIntegrationTest {

    @Test
    public void cachesValuePerTransaction() {
        AtomicInteger counter = new AtomicInteger();
        TransactionalContext<Integer> context = TransactionalContext.of(counter::incrementAndGet);

        List<Integer> firstTransaction = new ArrayList<>();
        data().transaction().run(() -> {
            firstTransaction.add(context.get());
            firstTransaction.add(context.get());
        });

        List<Integer> secondTransaction = new ArrayList<>();
        data().transaction().run(() -> secondTransaction.add(context.get()));

        /* provider is invoked once per transaction: stable within one, fresh in the next */
        check(firstTransaction.get(0)).is(1);
        check(firstTransaction.get(1)).is(1);
        check(secondTransaction.get(0)).is(2);
    }

    @Test
    public void getOutsideTransactionThrows() {
        TransactionalContext<Integer> context = TransactionalContext.of(() -> 1);

        assertThatThrownBy(context::get)
                .isInstanceOf(IllegalStateException.class);
    }
}
