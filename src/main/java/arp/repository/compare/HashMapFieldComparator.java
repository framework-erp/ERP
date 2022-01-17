package arp.repository.compare;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import arp.util.Unsafe;

public class HashMapFieldComparator extends BaseFieldComparator {

	public HashMapFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {

		HashMap mOne = (HashMap) Unsafe
				.getObjectFieldOfObject(one, fieldOffset);
		HashMap mAnother = (HashMap) Unsafe.getObjectFieldOfObject(another,
				fieldOffset);
		if (mOne == null) {
			return mAnother == null;
		} else {
			if (mOne.size() != mAnother.size()) {
				return false;
			}

			try {
				Iterator<Entry> i = mOne.entrySet().iterator();
				while (i.hasNext()) {
					Entry<?, ?> e = i.next();
					Object key = e.getKey();
					Object value = e.getValue();
					if (value == null) {
						if (!(mAnother.get(key) == null && mAnother
								.containsKey(key))) {
							return false;
						}
					} else {
						if (!EntityComparator.equals(value, mAnother.get(key))) {
							return false;
						}
					}
				}
			} catch (ClassCastException unused) {
				return false;
			} catch (NullPointerException unused) {
				return false;
			}

			return true;
		}

	}

}
