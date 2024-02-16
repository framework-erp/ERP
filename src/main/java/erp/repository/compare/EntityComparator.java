package erp.repository.compare;

import erp.util.ClassUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityComparator {

    private static Map<String, FieldComparator[]> typeFieldComparators = new ConcurrentHashMap<>();

    public static <T> boolean equals(T one, T another) {
        if (one == null && another == null) {
            return true;
        }
        if (one == null || another == null) {
            return false;
        }

        String clsName = one.getClass().getName();
        FieldComparator[] fieldComparators = typeFieldComparators.get(clsName);
        if (fieldComparators == null) {
            fieldComparators = buildFieldComparators(one.getClass());
            typeFieldComparators.putIfAbsent(clsName, fieldComparators);
        }
        for (FieldComparator fieldComparator : fieldComparators) {
            if (!fieldComparator.equals(one, another)) {
                return false;
            }
        }
        return true;
    }

    private static FieldComparator[] buildFieldComparators(Class<?> cls) {
        Field[] fields = ClassUtil.getAllFields(cls);
        FieldComparator[] comparators = new FieldComparator[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Class<?> fieldType = field.getType();
            if (byte.class.equals(fieldType)) {
                comparators[i] = new ByteFieldComparator(field);
            } else if (short.class.equals(fieldType)) {
                comparators[i] = new ShortFieldComparator(field);
            } else if (char.class.equals(fieldType)) {
                comparators[i] = new CharFieldComparator(field);
            } else if (int.class.equals(fieldType)) {
                comparators[i] = new IntFieldComparator(field);
            } else if (long.class.equals(fieldType)) {
                comparators[i] = new LongFieldComparator(field);
            } else if (float.class.equals(fieldType)) {
                comparators[i] = new FloatFieldComparator(field);
            } else if (double.class.equals(fieldType)) {
                comparators[i] = new DoubleFieldComparator(field);
            } else if (boolean.class.equals(fieldType)) {
                comparators[i] = new BooleanFieldComparator(field);
            } else if (Object.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Byte.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Short.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Character.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Integer.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Long.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Float.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Double.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Boolean.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (String.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Enum.class.equals(fieldType.getSuperclass())) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (fieldType.getName().startsWith("[")) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (ArrayList.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (LinkedList.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (List.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Queue.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (HashSet.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (Set.class.equals(fieldType)) {
                comparators[i] = new JavaObjectFieldComparator(field);
            } else if (HashMap.class.equals(fieldType)) {
                comparators[i] = new HashMapFieldComparator(field);
            } else if (Map.class.equals(fieldType)) {
                comparators[i] = new MapFieldComparator(field);
            } else {
                comparators[i] = new EntityFieldComparator(field);
            }
        }
        return comparators;
    }
}
