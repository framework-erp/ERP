package erp.repository.compare;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectFieldComparator extends BaseFieldComparator {

    public ObjectFieldComparator(Field field) {
        super(field);
    }

    @Override
    public <T> boolean equals(T one, T another) {
        Object oneEntityField = Unsafe.getObjectFieldOfObject(one, fieldOffset);
        Object anotherEntityField = Unsafe.getObjectFieldOfObject(another, fieldOffset);
        return EntityComparator.equals(oneEntityField, anotherEntityField);
    }
}
