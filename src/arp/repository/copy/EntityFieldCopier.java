package arp.repository.copy;

import java.lang.reflect.Field;

import arp.util.Unsafe;

public class EntityFieldCopier extends BaseFieldCopier {

	public EntityFieldCopier(Field field) {
		super(field);
	}

	@Override
	public void copyField(Object fromEntity, Object toEntity) {
		Object fromEntityField = Unsafe.getObjectFieldOfObject(fromEntity, fieldOffset);
		Object toEntityField = EntityCopier.copy(fromEntityField);
		Unsafe.setObjectFieldOfObject(toEntity, fieldOffset, toEntityField);
	}

}
