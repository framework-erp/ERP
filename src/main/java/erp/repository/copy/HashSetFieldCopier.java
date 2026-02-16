package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.HashSet;

public class HashSetFieldCopier extends BaseFieldCopier {

    public HashSetFieldCopier(Field field) {
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
