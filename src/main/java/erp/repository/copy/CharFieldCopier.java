package erp.repository.copy;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class CharFieldCopier extends BaseFieldCopier {

    public CharFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Unsafe.copyCharFieldOfObject(fromEntity, toEntity, fieldOffset);
    }

}
