package arp.repository.copy;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class ByteFieldCopier extends BaseFieldCopier {

	public ByteFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Unsafe.copyByteFieldOfObject(fromEntity, toEntity, fieldOffset);
	}

}
