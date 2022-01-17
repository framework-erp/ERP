package arp.repository.compare;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import arp.util.Unsafe;

public class MapFieldComparator extends BaseFieldComparator {

	public MapFieldComparator(Field field) {
		super(field);
	}

	@Override
	public <T> boolean equals(T one, T another) {
		Map mOne = (Map) Unsafe.getObjectFieldOfObject(one, fieldOffset);
		if (mOne instanceof HashMap) {
			HashMap hmOne = (HashMap) mOne;
			HashMap hmAnother = (HashMap) Unsafe.getObjectFieldOfObject(another,
					fieldOffset);
			if (hmOne == null) {
				return hmAnother == null;
			} else {
				if (hmOne.size() != hmAnother.size()) {
					return false;
				}

				try {
					Iterator<Entry> i = hmOne.entrySet().iterator();
					while (i.hasNext()) {
						Entry<?, ?> e = i.next();
						Object key = e.getKey();
						Object value = e.getValue();
						if (value == null) {
							if (!(hmAnother.get(key) == null && hmAnother
									.containsKey(key))) {
								return false;
							}
						} else {
							if (!value.equals(hmAnother.get(key))) {
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
		} else {
			return true;
		}
	}

}
