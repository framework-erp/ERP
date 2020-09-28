package arp.repository.copy;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class LongFieldCopier extends BaseFieldCopier {

	public LongFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Unsafe.copyLongFieldOfObject(fromEntity, toEntity, fieldOffset);
	}

}
