package erp.repository.copy;

import erp.util.ClassUtil;
import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityCopier {

    private static Map<String, FieldCopier[]> typeFieldCopiers = new ConcurrentHashMap<>();

    public static <T> T copy(T entity) {
        if (entity == null) {
            return null;
        }

        Class<?> cls = entity.getClass();

        if (cls.isPrimitive() ||
                cls.equals(Byte.class) || cls.equals(Short.class) || cls.equals(Integer.class) ||
                cls.equals(Long.class) || cls.equals(Float.class) || cls.equals(Double.class) ||
                cls.equals(Boolean.class) || cls.equals(Character.class) ||
                cls.equals(String.class) || cls.equals(Object.class)) {
            return entity;
        }

        if (Enum.class.isInstance(entity) || (cls.getSuperclass() != null && cls.getSuperclass().equals(Enum.class))) {
            return entity;
        }

        if (entity instanceof List) {
            return (T) copyList((List<?>) entity);
        }
        if (entity instanceof Map) {
            return (T) copyMap((Map<?, ?>) entity);
        }
        if (entity instanceof Set) {
            return (T) copySet((Set<?>) entity);
        }
        if (entity instanceof Queue) {
            return (T) copyQueue((Queue<?>) entity);
        }

        if (cls.isArray()) {
            return (T) copyArray(entity);
        }

        T copyOfEntity = Unsafe.allocateInstance(entity.getClass());
        String clsName = entity.getClass().getName();
        FieldCopier[] fieldCopiers = typeFieldCopiers.get(clsName);
        if (fieldCopiers == null) {
            fieldCopiers = buildFieldCopiers(entity.getClass());
            typeFieldCopiers.putIfAbsent(clsName, fieldCopiers);
        }
        for (FieldCopier fieldCopier : fieldCopiers) {
            fieldCopier.copyField(entity, copyOfEntity);
        }
        return copyOfEntity;
    }

    private static List copyList(List<?> list) {
        List copy;
        if (list instanceof ArrayList) {
            copy = new ArrayList(list.size());
        } else if (list instanceof LinkedList) {
            copy = new LinkedList();
        } else {
            try {
                copy = list.getClass().newInstance();
            } catch (Exception e) {
                copy = new ArrayList(list.size());
            }
        }
        for (Object item : list) {
            copy.add(copy(item));
        }
        return copy;
    }

    private static Map copyMap(Map<?, ?> map) {
        Map copy;
        if (map instanceof LinkedHashMap) {
            copy = new LinkedHashMap(map.size());
        } else if (map instanceof HashMap) {
            copy = new HashMap(map.size());
        } else {
            try {
                copy = map.getClass().newInstance();
            } catch (Exception e) {
                copy = new HashMap(map.size());
            }
        }
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            copy.put(copy(entry.getKey()), copy(entry.getValue()));
        }
        return copy;
    }

    private static Set copySet(Set<?> set) {
        Set copy;
        if (set instanceof HashSet) {
            copy = new HashSet(set.size());
        } else {
            try {
                copy = set.getClass().newInstance();
            } catch (Exception e) {
                copy = new HashSet(set.size());
            }
        }
        for (Object item : set) {
            copy.add(copy(item));
        }
        return copy;
    }

    private static Queue copyQueue(Queue<?> queue) {
        Queue copy;
        try {
            copy = queue.getClass().newInstance();
        } catch (Exception e) {
            copy = new LinkedList();
        }
        for (Object item : queue) {
            copy.add(copy(item));
        }
        return copy;
    }

    private static Object copyArray(Object array) {
        int length = java.lang.reflect.Array.getLength(array);
        Object copy = java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), length);
        for (int i = 0; i < length; i++) {
            java.lang.reflect.Array.set(copy, i, copy(java.lang.reflect.Array.get(array, i)));
        }
        return copy;
    }

    private static FieldCopier[] buildFieldCopiers(Class<?> cls) {
        Field[] fields = ClassUtil.getAllNonStaticFields(cls);
        FieldCopier[] copiers = new FieldCopier[fields.length];
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            Class<?> fieldType = field.getType();
            if (byte.class.equals(fieldType)) {
                copiers[i] = new ByteFieldCopier(field);
            } else if (short.class.equals(fieldType)) {
                copiers[i] = new ShortFieldCopier(field);
            } else if (char.class.equals(fieldType)) {
                copiers[i] = new CharFieldCopier(field);
            } else if (int.class.equals(fieldType)) {
                copiers[i] = new IntFieldCopier(field);
            } else if (long.class.equals(fieldType)) {
                copiers[i] = new LongFieldCopier(field);
            } else if (float.class.equals(fieldType)) {
                copiers[i] = new FloatFieldCopier(field);
            } else if (double.class.equals(fieldType)) {
                copiers[i] = new DoubleFieldCopier(field);
            } else if (boolean.class.equals(fieldType)) {
                copiers[i] = new BooleanFieldCopier(field);
            } else if (Object.class.equals(fieldType)) {
                copiers[i] = new ObjectFieldCopier(field);
            } else if (Byte.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Short.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Character.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Integer.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Long.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Float.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Double.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Boolean.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (String.class.equals(fieldType)) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (Enum.class.equals(fieldType.getSuperclass())) {
                copiers[i] = new ValueObjectFieldCopier(field);
            } else if (byte[].class.equals(fieldType)) {
                copiers[i] = new ByteArrayFieldCopier(field);
            } else if (short[].class.equals(fieldType)) {
                copiers[i] = new ShortArrayFieldCopier(field);
            } else if (char[].class.equals(fieldType)) {
                copiers[i] = new CharArrayFieldCopier(field);
            } else if (int[].class.equals(fieldType)) {
                copiers[i] = new IntArrayFieldCopier(field);
            } else if (long[].class.equals(fieldType)) {
                copiers[i] = new LongArrayFieldCopier(field);
            } else if (float[].class.equals(fieldType)) {
                copiers[i] = new FloatArrayFieldCopier(field);
            } else if (double[].class.equals(fieldType)) {
                copiers[i] = new DoubleArrayFieldCopier(field);
            } else if (boolean[].class.equals(fieldType)) {
                copiers[i] = new BooleanArrayFieldCopier(field);
            } else if (fieldType.getName().startsWith("[L")) {
                copiers[i] = new ValueObjectArrayFieldCopier(field);
            } else if (ArrayList.class.equals(fieldType)) {
                copiers[i] = new ArrayListFieldCopier(field);
            } else if (LinkedList.class.equals(fieldType)) {
                copiers[i] = new LinkedListFieldCopier(field);
            } else if (List.class.equals(fieldType)) {
                copiers[i] = new ListFieldCopier(field);
            } else if (Queue.class.equals(fieldType)) {
                copiers[i] = new QueueFieldCopier(field);
            } else if (HashSet.class.equals(fieldType)) {
                copiers[i] = new HashSetFieldCopier(field);
            } else if (Set.class.equals(fieldType)) {
                copiers[i] = new SetFieldCopier(field);
            } else if (LinkedHashMap.class.equals(fieldType)) {
                copiers[i] = new LinkedHashMapFieldCopier(field);
            } else if (HashMap.class.equals(fieldType)) {
                copiers[i] = new HashMapFieldCopier(field);
            } else if (Map.class.equals(fieldType)) {
                copiers[i] = new MapFieldCopier(field);
            } else {
                copiers[i] = new EntityFieldCopier(field);
            }
        }
        return copiers;
    }

}
