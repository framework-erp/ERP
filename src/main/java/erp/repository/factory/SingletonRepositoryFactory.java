package erp.repository.factory;

import erp.repository.SingletonRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class SingletonRepositoryFactory {

    public static synchronized <I> I newInstance(Class<I> itfType, SingletonRepository underlyingRepository) {

        I instance = (I) Proxy.newProxyInstance(underlyingRepository.getClass().getClassLoader(), new Class[]{itfType},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("get".equals(method.getName())) {
                            return underlyingRepository.get();
                        } else if ("take".equals(method.getName())) {
                            return underlyingRepository.take();
                        } else if ("put".equals(method.getName())) {
                            underlyingRepository.put(args[0]);
                            return null;
                        } else {
                            throw new UnsupportedOperationException(method.getName());
                        }
                    }
                });

        return instance;
    }

}
