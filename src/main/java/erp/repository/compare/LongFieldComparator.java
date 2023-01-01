package erp.repository.compare;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class LongFieldComparator extends BaseFieldComparator {

    public LongFieldComparator(Field field) {
        super(field);
    }

    @Override
    public <T> boolean equals(T one, T another) {
        return Unsafe.compareLongFieldOfObject(one, another, fieldOffset) == 0;
    }

}
