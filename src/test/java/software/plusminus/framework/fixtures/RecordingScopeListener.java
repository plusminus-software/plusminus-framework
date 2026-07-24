package software.plusminus.framework.fixtures;

import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.plusminus.scope.events.ScopeCompletedEvent;
import software.plusminus.scope.events.ScopeFailedEvent;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Profile("test")
public class RecordingScopeListener {

    private final AtomicInteger started = new AtomicInteger();
    private final AtomicInteger completed = new AtomicInteger();
    private final AtomicInteger finalized = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();

    @EventListener
    public void onStarted(ScopeStartedEvent event) {
        started.incrementAndGet();
    }

    @EventListener
    public void onCompleted(ScopeCompletedEvent event) {
        completed.incrementAndGet();
    }

    @EventListener
    public void onFinalized(ScopeFinalizedEvent event) {
        finalized.incrementAndGet();
    }

    @EventListener
    public void onFailed(ScopeFailedEvent<?> event) {
        failed.incrementAndGet();
    }

    public int started() {
        return started.get();
    }

    public int completed() {
        return completed.get();
    }

    public int finalized() {
        return finalized.get();
    }

    public int failed() {
        return failed.get();
    }

    public void clear() {
        started.set(0);
        completed.set(0);
        finalized.set(0);
        failed.set(0);
    }
}
