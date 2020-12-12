package arp.repository.copy;

import java.lang.reflect.Field;
import java.util.HashSet;

import arp.util.Unsafe;

public class HashSetFieldCopier extends BaseFieldCopier {

	public HashSetFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		HashSet set = (HashSet) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, set.clone());
	}

}
