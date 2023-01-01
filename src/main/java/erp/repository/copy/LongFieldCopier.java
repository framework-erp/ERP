package erp.repository.copy;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class LongFieldCopier extends BaseFieldCopier {

    public LongFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Unsafe.copyLongFieldOfObject(fromEntity, toEntity, fieldOffset);
    }

}
