package arp.repository;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class CommonMemRepository<E> extends MemRepository<E, Object> {

	private Class<?> idFieldType;
	private long idFieldOffset;

	public CommonMemRepository(Class<E> cls) {
		try {
			Field idField = cls.getDeclaredField("id");
			idFieldType = idField.getType();
			idFieldOffset = Unsafe.getFieldOffset(idField);
		} catch (Exception e) {
		}
	}

	@Override
	protected Object getId(E entity) {
		try {
			if (byte.class.equals(idFieldType)) {
				return Unsafe.getByteFieldOfObject(entity, idFieldOffset);
			} else if (short.class.equals(idFieldType)) {
				return Unsafe.getShortFieldOfObject(entity, idFieldOffset);
			} else if (char.class.equals(idFieldType)) {
				return Unsafe.getCharFieldOfObject(entity, idFieldOffset);
			} else if (int.class.equals(idFieldType)) {
				return Unsafe.getIntFieldOfObject(entity, idFieldOffset);
			} else if (float.class.equals(idFieldType)) {
				return Unsafe.getFloatFieldOfObject(entity, idFieldOffset);
			} else if (long.class.equals(idFieldType)) {
				return Unsafe.getLongFieldOfObject(entity, idFieldOffset);
			} else if (double.class.equals(idFieldType)) {
				return Unsafe.getDoubleFieldOfObject(entity, idFieldOffset);
			} else if (boolean.class.equals(idFieldType)) {
				return Unsafe.getBooleanFieldOfObject(entity, idFieldOffset);
			} else {
				return Unsafe.getObjectFieldOfObject(entity, idFieldOffset);
			}
		} catch (Exception e) {
			return null;
		}
	}

}
