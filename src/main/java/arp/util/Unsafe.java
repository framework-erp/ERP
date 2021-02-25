package arp.util;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

public class Unsafe {
	public static sun.misc.Unsafe theUnsafe;
	public static int objArray_BUFFER_PAD;
	public static int intArray_BUFFER_PAD;
	public static int longArray_BUFFER_PAD;

	private static long FIELD_OFFSET_String_value;

	static {
		try {
			final PrivilegedExceptionAction<sun.misc.Unsafe> action = new PrivilegedExceptionAction<sun.misc.Unsafe>() {
				public sun.misc.Unsafe run() throws Exception {
					Field theUnsafe = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
					theUnsafe.setAccessible(true);
					return (sun.misc.Unsafe) theUnsafe.get(null);
				}
			};

			theUnsafe = AccessController.doPrivileged(action);
			objArray_BUFFER_PAD = 128 / sun.misc.Unsafe.ARRAY_OBJECT_INDEX_SCALE;
			intArray_BUFFER_PAD = 128 / sun.misc.Unsafe.ARRAY_INT_INDEX_SCALE;
			longArray_BUFFER_PAD = 128 / sun.misc.Unsafe.ARRAY_LONG_INDEX_SCALE;

			FIELD_OFFSET_String_value = theUnsafe.objectFieldOffset(String.class.getDeclaredField("value"));

		} catch (Exception e) {
			throw new RuntimeException("Unable to load unsafe", e);
		}
	}

	public static void storeFence() {
		theUnsafe.storeFence();
	}

	public static void loadFence() {
		theUnsafe.loadFence();
	}

	public static long getFieldOffset(String type, String field) throws Exception {
		return theUnsafe.objectFieldOffset(Class.forName(type).getDeclaredField(field));
	}

	public static long getFieldOffset(Field field) {
		return theUnsafe.objectFieldOffset(field);
	}

	public static Object getObjectFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getObject(obj, fieldOffset);
	}

	public static void setObjectFieldOfObject(Object obj, long fieldOffset, Object value) {
		theUnsafe.putObject(obj, fieldOffset, value);
	}

	public static void setBooleanFieldOfObject(Object obj, long fieldOffset, boolean value) {
		theUnsafe.putBoolean(obj, fieldOffset, value);
	}

	public static void setByteFieldOfObject(Object obj, long fieldOffset, byte value) {
		theUnsafe.putByte(obj, fieldOffset, value);
	}

	public static void setCharFieldOfObject(Object obj, long fieldOffset, char value) {
		theUnsafe.putChar(obj, fieldOffset, value);
	}

	public static void setShortFieldOfObject(Object obj, long fieldOffset, short value) {
		theUnsafe.putShort(obj, fieldOffset, value);
	}

	public static void setIntFieldOfObject(Object obj, long fieldOffset, int value) {
		theUnsafe.putInt(obj, fieldOffset, value);
	}

	public static void setFloatFieldOfObject(Object obj, long fieldOffset, float value) {
		theUnsafe.putFloat(obj, fieldOffset, value);
	}

	public static void setLongFieldOfObject(Object obj, long fieldOffset, long value) {
		theUnsafe.putLong(obj, fieldOffset, value);
	}

	public static void setDoubleFieldOfObject(Object obj, long fieldOffset, double value) {
		theUnsafe.putDouble(obj, fieldOffset, value);
	}

	public static byte getByteFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getByte(obj, fieldOffset);
	}

	public static short getShortFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getShort(obj, fieldOffset);
	}

	public static char getCharFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getChar(obj, fieldOffset);
	}

	public static int getIntFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getInt(obj, fieldOffset);
	}

	public static float getFloatFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getFloat(obj, fieldOffset);
	}

	public static long getLongFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getLong(obj, fieldOffset);
	}

	public static double getDoubleFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getDouble(obj, fieldOffset);
	}

	public static boolean getBooleanFieldOfObject(Object obj, long fieldOffset) {
		return theUnsafe.getBoolean(obj, fieldOffset);
	}

	public static char[] getValueOfString(String str) {
		return (char[]) theUnsafe.getObject(str, FIELD_OFFSET_String_value);
	}

	public static <T> T allocateInstance(Class<?> cls) {
		try {
			return (T) theUnsafe.allocateInstance(cls);
		} catch (InstantiationException e) {
			return null;
		}
	}

	public static void copyByteFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putByte(toObj, fieldOffset, theUnsafe.getByte(fromObj, fieldOffset));
	}

	public static void copyCharFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putChar(toObj, fieldOffset, theUnsafe.getChar(fromObj, fieldOffset));
	}

	public static void copyShortFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putShort(toObj, fieldOffset, theUnsafe.getShort(fromObj, fieldOffset));
	}

	public static void copyIntFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putInt(toObj, fieldOffset, theUnsafe.getInt(fromObj, fieldOffset));
	}

	public static void copyLongFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putLong(toObj, fieldOffset, theUnsafe.getLong(fromObj, fieldOffset));
	}

	public static void copyFloatFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putFloat(toObj, fieldOffset, theUnsafe.getFloat(fromObj, fieldOffset));
	}

	public static void copyDoubleFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putDouble(toObj, fieldOffset, theUnsafe.getDouble(fromObj, fieldOffset));
	}

	public static void copyBooleanFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putBoolean(toObj, fieldOffset, theUnsafe.getBoolean(fromObj, fieldOffset));
	}

	public static void copyRefFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		theUnsafe.putObject(toObj, fieldOffset, theUnsafe.getObject(fromObj, fieldOffset));
	}

	public static int compareByteFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		byte fromObjField = theUnsafe.getByte(fromObj, fieldOffset);
		byte toObjField = theUnsafe.getByte(toObj, fieldOffset);
		return Byte.compare(fromObjField, toObjField);
	}

	public static int compareShortFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		short fromObjField = theUnsafe.getShort(fromObj, fieldOffset);
		short toObjField = theUnsafe.getShort(toObj, fieldOffset);
		return Short.compare(fromObjField, toObjField);
	}

	public static int compareCharFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		char fromObjField = theUnsafe.getChar(fromObj, fieldOffset);
		char toObjField = theUnsafe.getChar(toObj, fieldOffset);
		return Character.compare(fromObjField, toObjField);
	}

	public static int compareIntFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		int fromObjField = theUnsafe.getInt(fromObj, fieldOffset);
		int toObjField = theUnsafe.getInt(toObj, fieldOffset);
		return Integer.compare(fromObjField, toObjField);
	}

	public static int compareLongFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		long fromObjField = theUnsafe.getLong(fromObj, fieldOffset);
		long toObjField = theUnsafe.getLong(toObj, fieldOffset);
		return Long.compare(fromObjField, toObjField);
	}

	public static int compareFloatFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		float fromObjField = theUnsafe.getFloat(fromObj, fieldOffset);
		float toObjField = theUnsafe.getFloat(toObj, fieldOffset);
		return Float.compare(fromObjField, toObjField);
	}

	public static int compareDoubleFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		double fromObjField = theUnsafe.getDouble(fromObj, fieldOffset);
		double toObjField = theUnsafe.getDouble(toObj, fieldOffset);
		return Double.compare(fromObjField, toObjField);
	}

	public static int compareBooleanFieldOfObject(Object fromObj, Object toObj, long fieldOffset) {
		boolean fromObjField = theUnsafe.getBoolean(fromObj, fieldOffset);
		boolean toObjField = theUnsafe.getBoolean(toObj, fieldOffset);
		return Boolean.compare(fromObjField, toObjField);
	}

}
