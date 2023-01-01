package erp.repository.compare;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class IntFieldComparator extends BaseFieldComparator {

    public IntFieldComparator(Field field) {
        super(field);
    }

    @Override
    public <T> boolean equals(T one, T another) {
        return Unsafe.compareIntFieldOfObject(one, another, fieldOffset) == 0;
    }

}
