package arp.repository.copy;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class IntFieldCopier extends BaseFieldCopier {

	public IntFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Unsafe.copyIntFieldOfObject(fromEntity, toEntity, fieldOffset);
	}

}
