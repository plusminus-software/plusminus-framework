package software.plusminus.framework.fixtures;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import software.plusminus.framework.model.AbstractEntity;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

@Data
@EqualsAndHashCode(of = "", callSuper = true)
@ToString(of = "", callSuper = true)
@Entity
public class TestEntity extends AbstractEntity {

    private String myField;

    @ElementCollection
    private List<String> list = new ArrayList<>();

}