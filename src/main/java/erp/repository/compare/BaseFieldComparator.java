package erp.repository.compare;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public abstract class BaseFieldComparator implements FieldComparator {

    protected long fieldOffset;

    public BaseFieldComparator(Field field) {
        fieldOffset = Unsafe.getFieldOffset(field);
    }

}
