package erp.repository.compare;

import erp.util.Unsafe;

import java.lang.reflect.Field;

public abstract class BaseFieldComparator implements FieldComparator {

    protected Field field;
    protected long fieldOffset;

    public BaseFieldComparator(Field field) {
        this.field = field;
        fieldOffset = Unsafe.getFieldOffset(field);
    }

}
