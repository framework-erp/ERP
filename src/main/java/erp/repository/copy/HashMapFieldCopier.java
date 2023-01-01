package erp.repository.copy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import erp.util.Unsafe;

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
            copiedMap.put(e.getKey(), EntityCopier.copy(e.getValue()));
        }
        Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, copiedMap);
    }
}
