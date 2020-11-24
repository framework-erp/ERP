package arp.repository.copy;

import java.lang.reflect.Field;

import arp.enhance.Unsafe;

public class FloatFieldCopier extends BaseFieldCopier {

	public FloatFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Unsafe.copyFloatFieldOfObject(fromEntity, toEntity, fieldOffset);
	}

}
