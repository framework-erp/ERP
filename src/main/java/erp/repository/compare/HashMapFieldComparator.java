package erp.repository.compare;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class HashMapFieldComparator extends BaseFieldComparator {

    public HashMapFieldComparator(Field field) {
        super(field);
    }

    @Override
    public <T> boolean equals(T one, T another) {
        Object mOne = Unsafe.getObjectFieldOfObject(one, fieldOffset);
        Object mAnother = Unsafe.getObjectFieldOfObject(another, fieldOffset);
        return EntityComparator.equals(mOne, mAnother);
    }
}
