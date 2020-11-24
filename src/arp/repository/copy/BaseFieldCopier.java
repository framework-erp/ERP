package arp.repository.copy;

import java.lang.reflect.Field;

import arp.enhance.Unsafe;

public abstract class BaseFieldCopier implements FieldCopier {

	protected long fieldOffset;

	public BaseFieldCopier(Field field) {
		fieldOffset = Unsafe.getFieldOffset(field);
	}

}
