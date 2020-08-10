package arp.store.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class BooleanArrayFieldCopier extends BaseFieldCopier {

	public BooleanArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		boolean[] array = (boolean[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
