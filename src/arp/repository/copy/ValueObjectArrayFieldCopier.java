package arp.repository.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class ValueObjectArrayFieldCopier extends BaseFieldCopier {

	public ValueObjectArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Object[] array = (Object[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
