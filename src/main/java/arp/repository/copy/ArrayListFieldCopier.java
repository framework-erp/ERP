package arp.repository.copy;

import java.lang.reflect.Field;
import java.util.ArrayList;

import arp.util.Unsafe;

public class ArrayListFieldCopier extends BaseFieldCopier {

	public ArrayListFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		ArrayList list = (ArrayList) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, list.clone());
	}

}
