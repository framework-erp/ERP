package arp.repository.compare;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class BooleanFieldComparator extends BaseFieldComparator {

	public BooleanFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareBooleanFieldOfObject(one, another, fieldOffset) < 0;
	}

}
