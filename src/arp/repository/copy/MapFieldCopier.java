package arp.repository.copy;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import arp.enhance.Unsafe;

public class MapFieldCopier extends BaseFieldCopier {

	public MapFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Map map = (Map) Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		if (map instanceof HashMap) {
			Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, ((HashMap) map).clone());
		}
	}

}
