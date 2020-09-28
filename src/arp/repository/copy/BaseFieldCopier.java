package arp.repository.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public abstract class BaseFieldCopier implements FieldCopier {

	protected long fieldOffset;

	public BaseFieldCopier(Field field) {
		fieldOffset = Unsafe.getFieldOffset(field);
	}

}
