package arp.repository.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class FloatArrayFieldCopier extends BaseFieldCopier {

	public FloatArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		float[] array = (float[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
