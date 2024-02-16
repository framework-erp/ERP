package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class HashMapFieldCopier extends BaseFieldCopier {

    public HashMapFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        HashMap map = (HashMap) Unsafe.getObjectFieldOfObject(fromEntity,
                fieldOffset);
        HashMap copiedMap = new HashMap(map.size());
        Iterator<Entry> i = map.entrySet().iterator();
        while (i.hasNext()) {
            Entry<?, ?> e = i.next();
            Class<?> valueClass = e.getValue().getClass();
            Object valueCopy = null;
            if (Object.class.equals(valueClass)
                    || Byte.class.equals(valueClass)
                    || Short.class.equals(valueClass)
                    || Integer.class.equals(valueClass)
                    || Long.class.equals(valueClass)
                    || Float.class.equals(valueClass)
                    || Double.class.equals(valueClass)
                    || Boolean.class.equals(valueClass)
                    || Character.class.equals(valueClass)
                    || String.class.equals(valueClass)
                    || Enum.class.equals(valueClass)) {
                valueCopy = e.getValue();
            } else {
                valueCopy = EntityCopier.copy(e.getValue());
            }
            copiedMap.put(e.getKey(), valueCopy);
        }
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, copiedMap);
    }
}
