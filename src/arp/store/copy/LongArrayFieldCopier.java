package arp.store.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class LongArrayFieldCopier extends BaseFieldCopier {

	public LongArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		long[] array = (long[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
