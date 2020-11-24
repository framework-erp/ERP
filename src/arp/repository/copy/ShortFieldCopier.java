package arp.repository.copy;

import java.lang.reflect.Field;

import arp.enhance.Unsafe;

public class ShortFieldCopier extends BaseFieldCopier {

	public ShortFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Unsafe.copyShortFieldOfObject(fromEntity, toEntity, fieldOffset);
	}

}
