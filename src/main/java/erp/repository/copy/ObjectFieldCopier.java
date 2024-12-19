package erp.repository.copy;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.*;

public class ObjectFieldCopier extends BaseFieldCopier {

    public ObjectFieldCopier(Field field) {
        super(field);
    }

    @Override
    public void copyField(Object fromEntity, Object toEntity) {
        Object fromEntityField = Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
        if (fromEntityField == null) {
            return;
        }
        Class fieldType = fromEntityField.getClass();
        FieldCopier fieldCopier;
        if (Object.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Byte.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Short.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Character.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Integer.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Long.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Float.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Double.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Boolean.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (String.class.equals(fieldType)) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (Enum.class.equals(fieldType.getSuperclass())) {
            fieldCopier = new ValueObjectFieldCopier(field);
        } else if (byte[].class.equals(fieldType)) {
            fieldCopier = new ByteArrayFieldCopier(field);
        } else if (short[].class.equals(fieldType)) {
            fieldCopier = new ShortArrayFieldCopier(field);
        } else if (char[].class.equals(fieldType)) {
            fieldCopier = new CharArrayFieldCopier(field);
        } else if (int[].class.equals(fieldType)) {
            fieldCopier = new IntArrayFieldCopier(field);
        } else if (long[].class.equals(fieldType)) {
            fieldCopier = new LongArrayFieldCopier(field);
        } else if (float[].class.equals(fieldType)) {
            fieldCopier = new FloatArrayFieldCopier(field);
        } else if (double[].class.equals(fieldType)) {
            fieldCopier = new DoubleArrayFieldCopier(field);
        } else if (boolean[].class.equals(fieldType)) {
            fieldCopier = new BooleanArrayFieldCopier(field);
        } else if (fieldType.getName().startsWith("[L")) {
            fieldCopier = new ValueObjectArrayFieldCopier(field);
        } else if (ArrayList.class.equals(fieldType)) {
            fieldCopier = new ArrayListFieldCopier(field);
        } else if (LinkedList.class.equals(fieldType)) {
            fieldCopier = new LinkedListFieldCopier(field);
        } else if (List.class.equals(fieldType)) {
            fieldCopier = new ListFieldCopier(field);
        } else if (Queue.class.equals(fieldType)) {
            fieldCopier = new QueueFieldCopier(field);
        } else if (HashSet.class.equals(fieldType)) {
            fieldCopier = new HashSetFieldCopier(field);
        } else if (Set.class.equals(fieldType)) {
            fieldCopier = new SetFieldCopier(field);
        } else if (HashMap.class.equals(fieldType)) {
            fieldCopier = new HashMapFieldCopier(field);
        } else if (Map.class.equals(fieldType)) {
            fieldCopier = new MapFieldCopier(field);
        } else {
            fieldCopier = new EntityFieldCopier(field);
        }
        fieldCopier.copyField(fromEntity, toEntity);
    }
}
