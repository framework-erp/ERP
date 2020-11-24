package arp.repository.compare;

import java.lang.reflect.Field;

import arp.enhance.Unsafe;

public class CharFieldComparator extends BaseFieldComparator {

	public CharFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareCharFieldOfObject(one, another, fieldOffset) < 0;
	}

}
