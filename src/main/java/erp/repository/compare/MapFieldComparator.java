package erp.repository.compare;

import erp.util.Unsafe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapFieldComparator extends BaseFieldComparator {

    public MapFieldComparator(Field field) {
        super(field);
    }

    @Override
    public <T> boolean equals(T one, T another) {
        Map mOne = (Map) Unsafe.getObjectFieldOfObject(one, fieldOffset);
        if (mOne instanceof HashMap) {
            HashMap hmOne = (HashMap) mOne;
            HashMap hmAnother = (HashMap) Unsafe.getObjectFieldOfObject(
                    another, fieldOffset);
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
                            Class<?> valueClass = e.getValue().getClass();
                            Class<?> anotherValueClass = hmAnother.get(key).getClass();
                            if (!valueClass.equals(anotherValueClass)) {
                                return false;
                            }
                            if (Object.class.equals(valueClass)
                                    || Byte.class.equals(valueClass)
                                    || Short.class.equals(valueClass)
                                    || Integer.class.equals(valueClass)
                                    || Long.class.equals(valueClass)
                                    || Float.class.equals(valueClass)
                                    || Double.class.equals(valueClass)
                                    || Boolean.class.equals(valueClass)
                                    || Character.class.equals(valueClass)
                                    || String.class.equals(valueClass)
                                    || Enum.class.equals(valueClass)) {
                                if (!value.equals(hmAnother.get(key))) {
                                    return false;
                                }
                            } else {
                                if (!EntityComparator.equals(value, hmAnother.get(key))) {
                                    return false;
                                }
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
