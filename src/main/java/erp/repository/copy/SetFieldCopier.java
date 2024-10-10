package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class SetFieldCopier extends BaseFieldCopier {

    public SetFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Set set = (Set) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        Set setCopy = null;
        if (set instanceof HashSet) {
            setCopy = new HashSet();
            for (Object element : set) {
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
                setCopy.add(elementCopy);
            }
        }
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, setCopy);
    }

}
