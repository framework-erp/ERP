package arp.repository.copy;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class BooleanFieldCopier extends BaseFieldCopier {

	public BooleanFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Unsafe.copyBooleanFieldOfObject(fromEntity, toEntity, fieldOffset);
	}

}
