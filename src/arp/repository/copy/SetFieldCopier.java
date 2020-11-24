package arp.repository.copy;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import arp.enhance.Unsafe;

public class SetFieldCopier extends BaseFieldCopier {

	public SetFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Set set = (Set) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		if (set instanceof HashSet) {
			Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, ((HashSet) set).clone());
		}
	}

}
