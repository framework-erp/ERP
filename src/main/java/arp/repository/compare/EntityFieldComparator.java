package arp.repository.compare;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class EntityFieldComparator extends BaseFieldComparator {

	public EntityFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		Object oneEntityField = Unsafe.getObjectFieldOfObject(one, fieldOffset);
		Object anotherEntityField = Unsafe.getObjectFieldOfObject(another,
				fieldOffset);
		return EntityComparator.equals(oneEntityField, anotherEntityField);
	}

}
