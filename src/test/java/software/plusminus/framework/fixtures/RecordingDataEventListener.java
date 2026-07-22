package software.plusminus.framework.fixtures;

import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.plusminus.data.event.CreateEvent;
import software.plusminus.data.event.DeleteEvent;
import software.plusminus.data.event.ReadEvent;
import software.plusminus.data.event.UpdateEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Profile("test")
public class RecordingDataEventListener {

    private final List<TestEntity> created = new CopyOnWriteArrayList<>();
    private final List<TestEntity> updated = new CopyOnWriteArrayList<>();
    private final List<TestEntity> deleted = new CopyOnWriteArrayList<>();
    private final List<TestEntity> read = new CopyOnWriteArrayList<>();

    @EventListener
    public void onCreate(CreateEvent<TestEntity> event) {
        created.add(event.getEntity());
    }

    @EventListener
    public void onUpdate(UpdateEvent<TestEntity> event) {
        updated.add(event.getEntity());
    }

    @EventListener
    public void onDelete(DeleteEvent<TestEntity> event) {
        deleted.add(event.getEntity());
    }

    @EventListener
    public void onRead(ReadEvent<TestEntity> event) {
        read.add(event.getEntity());
    }

    public List<TestEntity> created() {
        return created;
    }

    public List<TestEntity> updated() {
        return updated;
    }

    public List<TestEntity> deleted() {
        return deleted;
    }

    public List<TestEntity> read() {
        return read;
    }

    public void clear() {
        created.clear();
        updated.clear();
        deleted.clear();
        read.clear();
    }
}
