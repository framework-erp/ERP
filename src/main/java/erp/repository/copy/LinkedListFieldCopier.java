package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.LinkedList;

public class LinkedListFieldCopier extends BaseFieldCopier {
    public LinkedListFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        LinkedList list = (LinkedList) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        LinkedList listCopy = new LinkedList();
        for (Object element : list) {
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
            listCopy.add(elementCopy);
        }
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, listCopy);
    }
}
