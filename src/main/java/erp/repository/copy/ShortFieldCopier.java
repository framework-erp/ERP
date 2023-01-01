package erp.repository.copy;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class ShortFieldCopier extends BaseFieldCopier {

    public ShortFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Unsafe.copyShortFieldOfObject(fromEntity, toEntity, fieldOffset);
    }

}
