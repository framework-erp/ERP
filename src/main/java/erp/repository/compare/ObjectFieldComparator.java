package erp.repository.compare;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectFieldComparator extends BaseFieldComparator {

    public ObjectFieldComparator(Field field) {
        super(field);
    }

    @Override
    public <T> boolean equals(T one, T another) {
        Object oneEntityField = Unsafe.getObjectFieldOfObject(one, fieldOffset);
        if (oneEntityField == null) {
            Object anotherEntityField = Unsafe.getObjectFieldOfObject(another, fieldOffset);
            return anotherEntityField == null;
        }
        Class fieldType = oneEntityField.getClass();
        FieldComparator fieldComparator;
        if (Object.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Byte.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Short.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Character.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Integer.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Long.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Float.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Double.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Boolean.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (String.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Enum.class.equals(fieldType.getSuperclass())) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (fieldType.getName().startsWith("[")) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (ArrayList.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (LinkedList.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (List.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Queue.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (HashSet.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (Set.class.equals(fieldType)) {
            fieldComparator = new JavaObjectFieldComparator(field);
        } else if (HashMap.class.equals(fieldType)) {
            fieldComparator = new HashMapFieldComparator(field);
        } else if (Map.class.equals(fieldType)) {
            fieldComparator = new MapFieldComparator(field);
        } else {
            fieldComparator = new EntityFieldComparator(field);
        }
        return fieldComparator.equals(one, another);
    }
}
