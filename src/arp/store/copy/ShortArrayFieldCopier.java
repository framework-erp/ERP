package arp.store.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class ShortArrayFieldCopier extends BaseFieldCopier {

	public ShortArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Short[] array = (Short[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
