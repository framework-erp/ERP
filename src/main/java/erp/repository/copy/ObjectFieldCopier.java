package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectFieldCopier extends BaseFieldCopier {

    public ObjectFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Object fromEntityField = Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        if (fromEntityField == null) {
            return;
        }
        Object copiedValue = EntityCopier.copy(fromEntityField);
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, copiedValue);
    }
}
