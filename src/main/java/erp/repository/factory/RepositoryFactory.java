package erp.repository.factory;

import erp.repository.Repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class RepositoryFactory {

    public static synchronized <I> I newInstance(Class<I> itfType, Repository underlyingRepository) {

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
                        } else {
                            throw new UnsupportedOperationException(method.getName());
                        }
                    }
                });

        return instance;
    }

}
