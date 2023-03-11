package erp.repository.interfaceimplementer;

import erp.repository.Repository;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class InterfaceRepositoryImplBuilder {

    private static Map<String, Object> itfTypeInstanceMap = new HashMap<>();

    public static synchronized <I> I build(Class<I> itfType, Repository underlyingRepository) {
        if (itfTypeInstanceMap.containsKey(itfType.getName())) {
            return (I) itfTypeInstanceMap.get(itfType.getName());
        }

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
                            underlyingRepository.putIfAbsent(args[0]);
                            return null;
                        } else if ("takeOrPutIfAbsent".equals(method.getName())) {
                            return underlyingRepository.takeOrPutIfAbsent(args[0], args[1]);
                        } else if ("remove".equals(method.getName())) {
                            underlyingRepository.remove(args[0]);
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
