package arp.store.compare;

import java.lang.reflect.Field;

import arp.core.Unsafe;

public class ShortFieldComparator extends BaseFieldComparator {

	public ShortFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		return Unsafe.compareShortFieldOfObject(one, another, fieldOffset) < 0;
	}

}
