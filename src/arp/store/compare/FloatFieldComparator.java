package arp.store.compare;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class FloatFieldComparator extends BaseFieldComparator {

	public FloatFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareFloatFieldOfObject(one, another, fieldOffset) < 0;
	}

}
