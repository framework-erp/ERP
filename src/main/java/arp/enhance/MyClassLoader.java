package arp.enhance;

/**
 * @author zheng chengdong
 */
public class MyClassLoader extends ClassLoader{

    public MyClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("in MyClassLoader");
        return super.loadClass(name);
    }

}
