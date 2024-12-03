package erp.repository.factory;

import erp.repository.Repository;

import java.lang.reflect.*;

public class RepositoryFactory {

    public static synchronized <I> I newInstance(Class<I> itfType, Repository underlyingRepository) {

        //验证itfType中的实体的类型是否和underlyingRepository中的实体类型一致
        verifyEntityType(itfType, underlyingRepository);

        I instance = (I) Proxy.newProxyInstance(underlyingRepository.getClass().getClassLoader(), new Class[]{itfType},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("find".equals(method.getName())) {
                            return underlyingRepository.find(args[0]);
                        } else if ("take".equals(method.getName())) {
                            return underlyingRepository.take(args[0]);
                        } else if ("put".equals(method.getName())) {
                            underlyingRepository.put(args[0]);
                            return null;
                        } else if ("putIfAbsent".equals(method.getName())) {
                            return underlyingRepository.putIfAbsent(args[0]);
                        } else if ("takeOrPutIfAbsent".equals(method.getName())) {
                            return underlyingRepository.takeOrPutIfAbsent(args[0], args[1]);
                        } else if ("remove".equals(method.getName())) {
                            return underlyingRepository.remove(args[0]);
                        } else if ("equals".equals(method.getName())) {
                            return underlyingRepository.equals(args[0]);
                        } else {
                            throw new UnsupportedOperationException(method.getName());
                        }
                    }
                });

        return instance;
    }

    private static <I> void verifyEntityType(Class<I> itfType, Repository underlyingRepository) {
        Class underlyingRepositoryEntityType = underlyingRepository.getEntityType();
        try {
            // 获取put方法
            Method putMethod = itfType.getMethod("put", underlyingRepositoryEntityType);
            return;
        } catch (NoSuchMethodException e) {
        }

        // 获取本身的泛型参数
        TypeVariable<?>[] typeParameters = itfType.getTypeParameters();
        for (TypeVariable<?> typeParameter : typeParameters) {
            // 获取泛型的上界（即 extends 后面的类型）
            Type[] bounds = typeParameter.getBounds();
            for (Type bound : bounds) {
                if (bound instanceof Class) {
                    Class<?> clazz = (Class<?>) bound;
                    if (clazz.equals(Object.class)) {
                        continue;
                    }
                    if (clazz.equals(underlyingRepositoryEntityType)) {
                        return;
                    }
                    //如果underlyingRepositoryEntityType是clazz的子类也是可以的
                    if (clazz.isAssignableFrom(underlyingRepositoryEntityType)) {
                        return;
                    }
                }
            }
        }

        // 获取继承的泛型接口的泛型参数
        Type[] genericInterfaces = itfType.getGenericInterfaces();
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                // 获取泛型参数
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                for (Type actualTypeArgument : actualTypeArguments) {
                    if (actualTypeArgument instanceof Class) {
                        Class<?> clazz = (Class<?>) actualTypeArgument;
                        if (clazz.equals(underlyingRepositoryEntityType)) {
                            return;
                        }
                        //如果underlyingRepositoryEntityType是clazz的子类也是可以的
                        if (clazz.isAssignableFrom(underlyingRepositoryEntityType)) {
                            return;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("The entity type of the repository is not compatible with the entity type of the interface");
    }

}
