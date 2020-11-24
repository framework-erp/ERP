package arp.repository.compare;

import java.lang.reflect.Field;

import arp.enhance.Unsafe;

public class DoubleFieldComparator extends BaseFieldComparator {

	public DoubleFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareDoubleFieldOfObject(one, another, fieldOffset) < 0;
	}

}
