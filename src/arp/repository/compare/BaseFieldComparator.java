package arp.repository.compare;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public abstract class BaseFieldComparator implements FieldComparator {

	protected long fieldOffset;

	public BaseFieldComparator(Field field) {
		fieldOffset = Unsafe.getFieldOffset(field);
	}

}
