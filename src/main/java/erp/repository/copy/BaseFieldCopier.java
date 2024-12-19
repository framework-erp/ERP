package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;

public abstract class BaseFieldCopier implements FieldCopier {

    protected long fieldOffset;
    protected Field field;

    public BaseFieldCopier(Field field) {
        this.field = field;
        fieldOffset = Unsafe.getFieldOffset(field);
    }

}
