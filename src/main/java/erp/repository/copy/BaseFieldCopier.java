package erp.repository.copy;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public abstract class BaseFieldCopier implements FieldCopier {

    protected long fieldOffset;

    public BaseFieldCopier(Field field) {
        fieldOffset = Unsafe.getFieldOffset(field);
    }

}
