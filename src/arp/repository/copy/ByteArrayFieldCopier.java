package arp.repository.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class ByteArrayFieldCopier extends BaseFieldCopier {

	public ByteArrayFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		byte[] array = (byte[]) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, array.clone());
	}

}
