package arp.repository.compare;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class IntFieldComparator extends BaseFieldComparator {

	public IntFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareIntFieldOfObject(one, another, fieldOffset) < 0;
	}

}
