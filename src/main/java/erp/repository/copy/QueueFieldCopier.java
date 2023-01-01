package erp.repository.copy;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;

import erp.util.Unsafe;

public class QueueFieldCopier extends BaseFieldCopier {

    public QueueFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Queue queue = (Queue) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        if (queue instanceof LinkedList) {
            Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, ((LinkedList) queue).clone());
        }
    }

}
