package erp.repository.copy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import erp.util.Unsafe;

public class EntityCopier {

    private static Map<String, FieldCopier[]> typeFieldCopiers = new ConcurrentHashMap<>();

    public static <T> T copy(T entity) {
        if (entity == null) {
            return null;
        }

        if (Enum.class.equals(entity.getClass().getSuperclass())) {
            return entity;
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

    private static FieldCopier[] buildFieldCopiers(Class<?> cls) {
        Field[] fields = cls.getDeclaredFields();
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
                copiers[i] = new ValueObjectFieldCopier(field);
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
