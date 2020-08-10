package arp.store.compare;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class BooleanFieldComparator extends BaseFieldComparator {

	public BooleanFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareBooleanFieldOfObject(one, another, fieldOffset) < 0;
	}

}
