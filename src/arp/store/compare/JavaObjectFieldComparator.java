package arp.store.compare;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class JavaObjectFieldComparator extends BaseFieldComparator {

	public JavaObjectFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		Object oneObjectField = Unsafe.getObjectFieldOfObject(one, fieldOffset);
		Object anotherObjectField = Unsafe.getObjectFieldOfObject(another, fieldOffset);
		if (oneObjectField == null) {
			return anotherObjectField == null;
		} else {
			return oneObjectField.equals(anotherObjectField);
		}
	}

}
