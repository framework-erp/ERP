package arp.store.copy;

import java.lang.reflect.Field;
import java.util.LinkedList;

import arp.core.Unsafe;

public class LinkedListFieldCopier extends BaseFieldCopier {
	public LinkedListFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		LinkedList list = (LinkedList) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, list.clone());
	}
}
