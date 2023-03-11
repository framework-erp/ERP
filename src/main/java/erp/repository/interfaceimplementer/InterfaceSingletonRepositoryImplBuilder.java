package erp.repository.interfaceimplementer;

import erp.repository.SingletonRepository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class InterfaceSingletonRepositoryImplBuilder {

    private static Map<String, Object> itfTypeInstanceMap = new HashMap<>();

    public static synchronized <I> I build(Class<I> itfType, SingletonRepository underlyingRepository) {
        if (itfTypeInstanceMap.containsKey(itfType.getName())) {
            return (I) itfTypeInstanceMap.get(itfType.getName());
        }

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
        itfTypeInstanceMap.put(itfType.getName(), instance);
        return instance;
    }

}
