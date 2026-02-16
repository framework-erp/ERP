package erp.repository.copy;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class ValueObjectArrayFieldCopier extends BaseFieldCopier {

    public ValueObjectArrayFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Object value = Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        if (value == null) {
            Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, null);
            return;
        }
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, EntityCopier.copy(value));
    }
}
