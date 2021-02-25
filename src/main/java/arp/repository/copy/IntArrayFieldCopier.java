package arp.repository.copy;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class IntArrayFieldCopier extends BaseFieldCopier {

	public IntArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		int[] array = (int[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
