package erp.repository.compare;

import java.lang.reflect.Field;

import erp.util.Unsafe;

public class DoubleFieldComparator extends BaseFieldComparator {

    public DoubleFieldComparator(Field field) {
        super(field);
    }

    @Override
    public <T> boolean equals(T one, T another) {
        return Unsafe.compareDoubleFieldOfObject(one, another, fieldOffset) == 0;
    }

}
