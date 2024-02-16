package erp.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassUtil {
    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            // 获取当前类的所有声明字段，并添加到列表中
            Field[] declaredFields = clazz.getDeclaredFields();
            fieldList.addAll(Arrays.asList(declaredFields));
            // 获取当前类的父类，准备继续获取父类的字段
            clazz = clazz.getSuperclass();
        }
        // 将列表转换为数组并返回
        return fieldList.toArray(new Field[0]);
    }
}
