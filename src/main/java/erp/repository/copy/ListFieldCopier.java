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
        List list = (List) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        List listCopy = null;
        if (list instanceof ArrayList) {
            listCopy = new ArrayList(list.size());
            for (Object element : list) {
                if (element == null) {
                    listCopy.add(null);
                    continue;
                }
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
        } else if (list instanceof LinkedList) {
            listCopy = new LinkedList();
            for (Object element : list) {
                if (element == null) {
                    listCopy.add(null);
                    continue;
                }
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
        }
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, listCopy);
    }

}
