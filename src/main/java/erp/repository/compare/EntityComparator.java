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
        if (!one.getClass().equals(another.getClass())) {
            return false;
        }

        Class<?> cls = one.getClass();

        if (cls.isPrimitive() ||
                cls.equals(Byte.class) || cls.equals(Short.class) || cls.equals(Integer.class) ||
                cls.equals(Long.class) || cls.equals(Float.class) || cls.equals(Double.class) ||
                cls.equals(Boolean.class) || cls.equals(Character.class) ||
                cls.equals(String.class) || cls.equals(Object.class)) {
            return one.equals(another);
        }

        if (Enum.class.isInstance(one) || (cls.getSuperclass() != null && cls.getSuperclass().equals(Enum.class))) {
            return one.equals(another);
        }

        if (one instanceof List) {
            return equalsList((List<?>) one, (List<?>) another);
        }
        if (one instanceof Map) {
            return equalsMap((Map<?, ?>) one, (Map<?, ?>) another);
        }
        if (one instanceof Set) {
            return equalsSet((Set<?>) one, (Set<?>) another);
        }
        if (one instanceof Queue) {
            return equalsCollection((Collection<?>) one, (Collection<?>) another);
        }

        if (cls.isArray()) {
            return equalsArray(one, another);
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

    private static boolean equalsList(List<?> l1, List<?> l2) {
        if (l1.size() != l2.size()) {
            return false;
        }
        for (int i = 0; i < l1.size(); i++) {
            if (!equals(l1.get(i), l2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsMap(Map<?, ?> m1, Map<?, ?> m2) {
        if (m1.size() != m2.size()) {
            return false;
        }
        if (m1 instanceof LinkedHashMap && m2 instanceof LinkedHashMap) {
            Iterator<? extends Map.Entry<?, ?>> i1 = m1.entrySet().iterator();
            Iterator<? extends Map.Entry<?, ?>> i2 = m2.entrySet().iterator();
            while (i1.hasNext()) {
                Map.Entry<?, ?> e1 = i1.next();
                Map.Entry<?, ?> e2 = i2.next();
                if (!equals(e1.getKey(), e2.getKey())) {
                    return false;
                }
                if (!equals(e1.getValue(), e2.getValue())) {
                    return false;
                }
            }
            return true;
        }
        for (Map.Entry<?, ?> entry : m1.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                if (!(m2.get(key) == null && m2.containsKey(key))) {
                    return false;
                }
            } else {
                if (!equals(value, m2.get(key))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean equalsSet(Set<?> s1, Set<?> s2) {
        if (s1.size() != s2.size()) {
            return false;
        }
        // 对于Set，如果要深比较且元素没有重写hashCode/equals，这在通用层面很难实现高效比较
        // 这里暂时遵循原有逻辑，主要解决嵌套集合的识别问题
        return s1.equals(s2);
    }

    private static boolean equalsCollection(Collection<?> c1, Collection<?> c2) {
        if (c1.size() != c2.size()) {
            return false;
        }
        Iterator<?> i1 = c1.iterator();
        Iterator<?> i2 = c2.iterator();
        while (i1.hasNext()) {
            if (!equals(i1.next(), i2.next())) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalsArray(Object a1, Object a2) {
        int length = java.lang.reflect.Array.getLength(a1);
        if (length != java.lang.reflect.Array.getLength(a2)) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!equals(java.lang.reflect.Array.get(a1, i), java.lang.reflect.Array.get(a2, i))) {
                return false;
            }
        }
        return true;
    }

    private static FieldComparator[] buildFieldComparators(Class<?> cls) {
        Field[] fields = ClassUtil.getAllNonStaticFields(cls);
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
                comparators[i] = new ObjectFieldComparator(field);
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
            } else if (LinkedHashMap.class.equals(fieldType)) {
                comparators[i] = new LinkedHashMapFieldComparator(field);
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
