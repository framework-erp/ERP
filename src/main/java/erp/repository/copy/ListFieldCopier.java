package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListFieldCopier extends BaseFieldCopier {

    public ListFieldCopier(Field field) {
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
