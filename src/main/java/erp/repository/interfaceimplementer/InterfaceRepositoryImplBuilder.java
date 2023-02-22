package erp.repository.interfaceimplementer;

import erp.repository.Repository;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InterfaceRepositoryImplBuilder {

    private static Map<String, Object> itfTypeInstanceMap = new HashMap<>();

    public static synchronized <I> I build(Class<I> itfType, Repository<?, ?> underlyingRepository) {
        if (itfTypeInstanceMap.containsKey(itfType.getName())) {
            return (I) itfTypeInstanceMap.get(itfType.getName());
        }
        String newTypeClsName = "erp.repository.generated." + itfType.getSimpleName() + "_implemented_by_" + underlyingRepository.getClass().getSimpleName();
        defineClass(itfType, underlyingRepository.getEntityType(), underlyingRepository.getIdType(), newTypeClsName);
        Constructor constructor = null;
        try {
            constructor = Class.forName(newTypeClsName).getDeclaredConstructor(Repository.class);
        } catch (Exception e) {
            throw new RuntimeException("getDeclaredConstructor for " + newTypeClsName + " error", e);
        }
        constructor.setAccessible(true);
        try {
            I instance = (I) constructor.newInstance(underlyingRepository);
            itfTypeInstanceMap.put(itfType.getName(), instance);
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("newInstance for " + newTypeClsName + " error", e);
        }
    }

    private static <I> void defineClass(Class<I> itfClass, String entityType, String idType, String newTypeClsName) {
        byte[] newClsBytes = new byte[0];
        Class<?> itfEntityClass = null;
        Class<?> itfIdClass = null;
        Class<?> objIdClass;
        try {
            objIdClass = toObjIdClass(idType);
        } catch (Exception e) {
            throw new RuntimeException("idType to ObjIdClass error", e);
        }
        Method[] methods = itfClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("find")) {
                itfEntityClass = method.getReturnType();
                itfIdClass = method.getParameterTypes()[0];
                break;
            }
        }
        boolean hasBridge = !itfEntityClass.getName().equals(entityType) || !itfIdClass.getName().equals(objIdClass.getName());
        String tplRepoImplClassName;
        if (hasBridge) {
            tplRepoImplClassName = "erp/repository/interfaceimplementer/GenericTemplateEntityRepositoryImpl";
        } else {
            tplRepoImplClassName = "erp/repository/interfaceimplementer/TemplateEntityImplRepositoryImpl";
        }
        newClsBytes = generateNewClsBytes(itfClass, itfEntityClass, itfIdClass, entityType, objIdClass, tplRepoImplClassName, newTypeClsName);
        callClassLoaderDefineClass(newTypeClsName, newClsBytes);
    }

    private static Class<?> toObjIdClass(String idType) throws Exception {
        if (byte.class.getName().equals(idType)) {
            return Byte.class;
        } else if (short.class.getName().equals(idType)) {
            return Short.class;
        } else if (char.class.getName().equals(idType)) {
            return Character.class;
        } else if (int.class.getName().equals(idType)) {
            return Integer.class;
        } else if (float.class.getName().equals(idType)) {
            return Float.class;
        } else if (long.class.getName().equals(idType)) {
            return Long.class;
        } else if (double.class.getName().equals(idType)) {
            return Double.class;
        } else if (boolean.class.getName().equals(idType)) {
            return Boolean.class;
        } else {
            return Class.forName(idType);
        }
    }

    private static void callClassLoaderDefineClass(String newTypeClsName, byte[] newClsBytes) {
        Object[] argArray = new Object[]{newTypeClsName, newClsBytes, Integer.valueOf(0), Integer.valueOf(newClsBytes.length)};
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class cls = null;
        try {
            cls = Class.forName("java.lang.ClassLoader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("get class for java.lang.ClassLoader error", e);
        }
        Method method = null;
        try {
            method = cls.getDeclaredMethod(
                    "defineClass",
                    new Class[]{String.class, byte[].class, int.class, int.class});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("get getDeclaredMethod for defineClass error", e);
        }
        method.setAccessible(true);
        try {
            method.invoke(cl, argArray);
        } catch (Exception e) {
            throw new RuntimeException("invoke defineClass error", e);
        }
    }

    private static byte[] generateNewClsBytes(Class<?> itfType,
                                              Class<?> itfEntityClass,
                                              Class<?> itfIdClass,
                                              String entityType,
                                              Class<?> idClass,
                                              String tplRepoImplClassName,
                                              String newTypeClsName) {
        String entityTypeDesc = "L" + entityType.replace('.', '/') + ";";
        String idTypeDesc = Type.getDescriptor(idClass);

        String itfEntityTypeDesc = Type.getDescriptor(itfEntityClass);
        String itfIdTypeDesc = Type.getDescriptor(itfIdClass);

        String templateEntityTypeDesc = Type.getDescriptor(TemplateEntityImpl.class);
        String templateIdTypeDesc = Type.getDescriptor(Integer.class);

        String templateItfEntityTypeDesc = Type.getDescriptor(TemplateEntity.class);
        String templateItfIdTypeDesc = Type.getDescriptor(Object.class);

        String tplRepoImplDesc = "L" + tplRepoImplClassName + ";";

        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(tplRepoImplClassName + ".class");
        byte[] bytes = new byte[0];
        try {
            bytes = new byte[is.available()];
            is.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException("read TemplateEntityRepositoryImpl.class error", e);
        }

        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                interfaces[0] = Type.getInternalName(itfType);
                name = newTypeClsName.replace('.', '/');
                if (signature != null) {
                    signature = signature.replaceAll(templateEntityTypeDesc, entityTypeDesc)
                            .replaceAll(templateIdTypeDesc, idTypeDesc);
                }
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String mthName, String mthDesc, String signature, String[] exceptions) {
                mthDesc = mthDesc
                        .replaceAll(templateEntityTypeDesc, entityTypeDesc)
                        .replaceAll(templateIdTypeDesc, idTypeDesc)
                        .replaceAll(templateItfEntityTypeDesc, itfEntityTypeDesc)
                        .replaceAll(templateItfIdTypeDesc, itfIdTypeDesc);

                return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {

                    @Override
                    public void visitLocalVariable(
                            final String name,
                            final String descriptor,
                            final String signature,
                            final Label start,
                            final Label end,
                            final int index) {
                        String realDescriptor = descriptor;
                        if (realDescriptor.equals(tplRepoImplDesc)) {
                            realDescriptor = "L" + newTypeClsName.replace('.', '/') + ";";
                        } else if (realDescriptor.equals(templateEntityTypeDesc)) {
                            realDescriptor = entityTypeDesc;
                        } else if (realDescriptor.equals(templateIdTypeDesc)) {
                            realDescriptor = idTypeDesc;
                        } else {
                        }
                        super.visitLocalVariable(name, realDescriptor, signature, start, end, index);
                    }

                    @Override
                    public void visitTypeInsn(final int opcode, final String type) {
                        String realType = type;
                        if (Opcodes.CHECKCAST == opcode) {
                            if (realType.equals(Type.getInternalName(TemplateEntityImpl.class))) {
                                realType = entityType.replace('.', '/');
                            } else if (realType.equals(Type.getInternalName(Integer.class))) {
                                realType = Type.getInternalName(idClass);
                            } else {
                            }
                        }
                        super.visitTypeInsn(opcode, realType);
                    }

                    @Override
                    public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
                        String realOwner = owner;
                        if (Opcodes.PUTFIELD == opcode || Opcodes.GETFIELD == opcode) {
                            realOwner = newTypeClsName.replace('.', '/');
                        }
                        super.visitFieldInsn(opcode, realOwner, name, descriptor);
                    }

                    @Override
                    public void visitMethodInsn(
                            final int opcode,
                            final String owner,
                            final String name,
                            final String descriptor,
                            final boolean isInterface) {
                        String realOwner = owner;
                        String realDescriptor = descriptor;
                        if (Opcodes.INVOKEVIRTUAL == opcode) {
                            if (owner.equals(tplRepoImplClassName)) {
                                realOwner = newTypeClsName.replace('.', '/');
                            }
                            realDescriptor = realDescriptor
                                    .replaceAll(templateEntityTypeDesc, entityTypeDesc)
                                    .replaceAll(templateIdTypeDesc, idTypeDesc);
                        }
                        super.visitMethodInsn(opcode, realOwner, name, realDescriptor, isInterface);
                    }
                };
            }
        }, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }

}
