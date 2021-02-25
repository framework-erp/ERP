package arp.repository.copy;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class ValueObjectFieldCopier extends BaseFieldCopier {

	public ValueObjectFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Unsafe.copyRefFieldOfObject(fromEntity, toEntity, fieldOffset);
	}

}
