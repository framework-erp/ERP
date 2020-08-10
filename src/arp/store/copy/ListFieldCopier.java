package arp.store.copy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import arp.core.Unsafe;

public class ListFieldCopier extends BaseFieldCopier {

	public ListFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		List list = (List) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		if (list instanceof ArrayList) {
			Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, ((ArrayList) list).clone());
		} else if (list instanceof LinkedList) {
			Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, ((LinkedList) list).clone());
		}
	}

}
