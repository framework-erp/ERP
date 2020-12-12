package arp.repository.copy;

import java.lang.reflect.Field;
import java.util.HashMap;

import arp.util.Unsafe;

public class HashMapFieldCopier extends BaseFieldCopier {

	public HashMapFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		HashMap map = (HashMap) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, map.clone());
	}
}
