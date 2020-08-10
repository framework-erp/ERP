package arp.store.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class DoubleArrayFieldCopier extends BaseFieldCopier {

	public DoubleArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		double[] array = (double[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
