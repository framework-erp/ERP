package arp.repository.compare;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class ByteFieldComparator extends BaseFieldComparator {

	public ByteFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareByteFieldOfObject(one, another, fieldOffset) < 0;
	}

}
