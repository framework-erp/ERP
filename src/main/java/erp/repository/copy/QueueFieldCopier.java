package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Queue;

public class QueueFieldCopier extends BaseFieldCopier {

    public QueueFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Queue queue = (Queue) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        Queue queueCopy = null;
        if (queue instanceof LinkedList) {
            queueCopy = new LinkedList();
            for (Object element : queue) {
                Class<?> elementClass = element.getClass();
                Object elementCopy = null;
                if (Object.class.equals(elementClass)
                        || Byte.class.equals(elementClass)
                        || Short.class.equals(elementClass)
                        || Integer.class.equals(elementClass)
                        || Long.class.equals(elementClass)
                        || Float.class.equals(elementClass)
                        || Double.class.equals(elementClass)
                        || Boolean.class.equals(elementClass)
                        || Character.class.equals(elementClass)
                        || String.class.equals(elementClass)
                        || Enum.class.equals(elementClass)) {
                    elementCopy = element;
                } else {
                    elementCopy = EntityCopier.copy(element);
                }
                queueCopy.add(elementCopy);
            }
        }
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, queueCopy);
    }

}
