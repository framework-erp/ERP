package arp.repository.copy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import arp.util.Unsafe;

public class MapFieldCopier extends BaseFieldCopier {

	public MapFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Map map = (Map) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		if (map instanceof HashMap) {
			HashMap hMap = (HashMap) map;
			HashMap copiedMap = new HashMap(hMap.size());
			Iterator<Entry> i = hMap.entrySet().iterator();
			while (i.hasNext()) {
				Entry<?, ?> e = i.next();
				copiedMap.put(e.getKey(), EntityCopier.copy(e.getValue()));
			}
			Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, copiedMap);
		}

	}

}
