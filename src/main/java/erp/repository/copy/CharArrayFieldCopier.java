package erp.repository.copy;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class CharArrayFieldCopier extends BaseFieldCopier {

    public CharArrayFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        char[] array = (char[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
    }

}
