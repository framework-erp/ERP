package erp.repository.copy;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class DoubleFieldCopier extends BaseFieldCopier {

    public DoubleFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Unsafe.copyDoubleFieldOfObject(fromEntity, toEntity, fieldOffset);
    }

}
