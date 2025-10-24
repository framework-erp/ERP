package erp.annotation;

import erp.process.ProcessWrapper;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class ProcessEnhancer {

    public static void scanAndEnhanceProcess() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL[] urls = null;
        if (cl instanceof URLClassLoader) {
            urls = ((URLClassLoader) cl).getURLs();
        } else {
            Class clc = cl.getClass();
            while (clc != null) {
                try {
                    Field ucpField = clc.getDeclaredField("ucp");
                    ucpField.setAccessible(true);
                    Object ucp = null;
                    try {
                        ucp = ucpField.get(cl);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("can not get ucp.", e);
                    }
                    Method getURLs = null;
                    try {
                        getURLs = ucp.getClass().getDeclaredMethod("getURLs");
                    } catch (NoSuchMethodException e) {
                        throw new RuntimeException("can not getDeclaredMethod getURLs.", e);
                    }
                    try {
                        urls = (URL[]) getURLs.invoke(ucp);
                        break;
                    } catch (Exception e) {
                        throw new RuntimeException("can not invoke getURLs.", e);
                    }
                } catch (NoSuchFieldException e) {
                    clc = clc.getSuperclass();
                    continue;
                }
            }
        }
        if (urls == null) {
            throw new RuntimeException("can not find urls.");
        }
        for (URL url : urls) {
            if (url.toString().endsWith(".jar") || url.toString().endsWith(".jar!/")) {
                continue;
            }
            URI uri = null;
            try {
                uri = url.toURI();
            } catch (URISyntaxException e) {
                throw new RuntimeException("can not toURI.", e);
            }
            FileSystem zipfs = null;
            Path rootPath = null;
            String uriStr = uri.toString();
            if (uriStr.contains("jar:file:")) {// jar
                int idx = uriStr.indexOf(".jar");
                String zipFilePath = uriStr.substring(0, idx) + ".jar";
                String pathInFile = uriStr.substring(idx + ".jar".length())
                        .replaceAll("!", "");
                try {
                    URI zipFile = URI.create(zipFilePath);
                    Map<String, String> env = new HashMap<>();
                    env.put("create", "true");
                    zipfs = FileSystems.newFileSystem(zipFile, env);
                    rootPath = zipfs.getPath(pathInFile);
                } catch (Exception e) {
                    throw new RuntimeException("zipFile error.", e);
                }
            } else {
                rootPath = Paths.get(uri);
            }
            try {
                Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

                    @Override
                    public FileVisitResult visitFile(Path file,
                                                     BasicFileAttributes attrs) throws IOException {
                        String fileName = file.getFileName().toString();
                        if (!fileName.endsWith(".class")) {
                            return FileVisitResult.CONTINUE;
                        }
                        byte[] bytes = Files.readAllBytes(file);
                        ResolvedClass rc = parseProcess(bytes);
                        if (rc != null) {
                            byte[] enhancedBytes = enhanceProcess(rc);
                            try {
                                loadClass(rc, enhancedBytes);
                            } catch (Exception e) {
                                throw new RuntimeException("can not loadClass.", e);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                });
            } catch (IOException e) {
                throw new RuntimeException("can not walkFileTree.", e);
            }
            if (zipfs != null) {
                try {
                    zipfs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private static void loadClass(ResolvedClass rc, byte[] enhancedBytes) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class clCls = Class.forName("java.lang.ClassLoader");
        java.lang.reflect.Method method = clCls.getDeclaredMethod("defineClass", new Class[]{String.class, byte[].class, int.class, int.class});
        method.setAccessible(true);
        Object[] argArray = new Object[]{rc.getName(), enhancedBytes, new Integer(0), new Integer(enhancedBytes.length)};
        method.invoke(cl, argArray);
        method.setAccessible(false);
    }

    private static ResolvedClass parseProcess(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        Map<String, Object> clsInfoMap = new HashMap<>();
        Map<String, ProcessInfo> processInfos = new HashMap<>();
        final ResolvedClass rc = new ResolvedClass();
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {

            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                clsInfoMap.put("name", name.replace('/', '.'));
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String mthName, String mthDesc, String signature, String[] exceptions) {
                // 过滤构造器
                if ("<init>".equals(mthName)) {
                    return super.visitMethod(access, mthName, mthDesc, signature, exceptions);
                }
                Type[] argumentTypes = Type.getArgumentTypes(mthDesc);
                return new AdviceAdapter(Opcodes.ASM9, super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {
                    private ProcessInfo processInfo = null;

                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                        // 只处理 @Process 注解
                        if (Type.getDescriptor(Process.class).equals(desc)) {
                            processInfo = new ProcessInfo((String) clsInfoMap.get("name"), mthName, mthDesc);
                        }
                        return super.visitAnnotation(desc, visible);
                    }

                    protected void onMethodEnter() {
                        // 如果找到了 @Process 注解，则处理
                        if (processInfo != null) {
                            processInfos.put(processInfo.getMthName() + "@" + processInfo.getMthDesc(), processInfo);
                        }
                        super.onMethodEnter();
                    }

                };
            }

            public void visitEnd() {
                if (!processInfos.isEmpty()) {
                    rc.setName((String) clsInfoMap.get("name"));
                    rc.setProcessInfos(processInfos);
                    rc.setClassBytes(bytes);
                }
                super.visitEnd();
            }

        }, ClassReader.EXPAND_FRAMES);
        if (rc.getName() != null) {
            return rc;
        } else {
            return null;
        }
    }

    private static byte[] enhanceProcess(ResolvedClass resolvedClass) {
        Map<String, ProcessInfo> processInfos = resolvedClass.getProcessInfos();
        ClassReader cr = new ClassReader(resolvedClass.getClassBytes());
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {

            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String mthName, String mthDesc, String signature, String[] exceptions) {
                Type[] argumentTypes = Type.getArgumentTypes(mthDesc);
                String returnTypeDesc = mthDesc.substring(mthDesc.indexOf(")") + 1);
                return new AdviceAdapter(Opcodes.ASM9, super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {

                    private Label lTryBlockStart;
                    private Label lTryBlockEnd;

                    protected void onMethodEnter() {
                        ProcessInfo processInfo = processInfos.get(mthName + "@" + mthDesc);
                        if (processInfo != null) {
                            visitLdcInsn(processInfo.getClsName() + "." + processInfo.getMthName());
                            visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class), "beforeProcessStart", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(String.class)), false);

                            if (argumentTypes != null) {
                                int localNum = 1;
                                for (int argIdx = 0; argIdx < argumentTypes.length; argIdx++) {
                                    Type argType = argumentTypes[argIdx];
                                    localNum = loadLocalAndToObject(localNum, argType.getDescriptor(), this);
                                    visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class), "recordProcessArgument", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(Object.class)), false);
                                }
                            }


                            lTryBlockStart = new Label();
                            lTryBlockEnd = new Label();

                            mark(lTryBlockStart);
                        }
                        super.onMethodEnter();
                    }

                    public void visitMaxs(int maxStack, int maxLocals) {
                        ProcessInfo processInfo = processInfos.get(mthName + "@" + mthDesc);
                        if (processInfo != null) {
                            mark(lTryBlockEnd);
                            catchException(lTryBlockStart, lTryBlockEnd, null);

                            visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class), "afterProcessFailed", "()V", false);

                            throwException();

                        }
                        super.visitMaxs(maxStack, maxLocals);
                    }

                    protected void onMethodExit(int opcode) {
                        ProcessInfo processInfo = processInfos.get(mthName + "@" + mthDesc);
                        if (processInfo != null && opcode != ATHROW) {
                            if (!Type.getDescriptor(void.class).equals(returnTypeDesc)) {
                                dupStackTopAndToObject(returnTypeDesc, this);
                                visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class), "recordProcessResult", Type.getMethodDescriptor(Type.getType(void.class), Type.getType(Object.class)), false);
                            }
                            visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class), "afterProcessFinish", "()V", false);
                        }
                        super.onMethodExit(opcode);
                    }

                };
            }

        }, ClassReader.EXPAND_FRAMES);
        byte[] enhancedBytes = cw.toByteArray();
        return enhancedBytes;
    }

    private static void dupStackTop(String stackTopTypeDesc, AdviceAdapter adviceAdapter) {
        if (Type.getDescriptor(long.class).equals(stackTopTypeDesc) || Type.getDescriptor(double.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitInsn(Opcodes.DUP2);
        } else {
            adviceAdapter.visitInsn(Opcodes.DUP);
        }
    }

    private static void dupStackTopAndToObject(String stackTopTypeDesc, AdviceAdapter adviceAdapter) {
        dupStackTop(stackTopTypeDesc, adviceAdapter);
        stackTopToObject(stackTopTypeDesc, adviceAdapter);
    }

    private static int loadLocalAndToObject(int localNum, String localTypeDesc, AdviceAdapter adviceAdapter) {
        int newLocalNum = loadLocal(localNum, localTypeDesc, adviceAdapter);
        stackTopToObject(localTypeDesc, adviceAdapter);
        return newLocalNum;
    }

    private static void stackTopToObject(String stackTopTypeDesc, AdviceAdapter adviceAdapter) {
        if (Type.getDescriptor(byte.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Byte.class), "valueOf", Type.getMethodDescriptor(Type.getType(Byte.class), Type.getType(byte.class)), false);
        } else if (Type.getDescriptor(char.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Character.class), "valueOf", Type.getMethodDescriptor(Type.getType(Character.class), Type.getType(char.class)), false);
        } else if (Type.getDescriptor(short.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Short.class), "valueOf", Type.getMethodDescriptor(Type.getType(Short.class), Type.getType(short.class)), false);
        } else if (Type.getDescriptor(float.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Float.class), "valueOf", Type.getMethodDescriptor(Type.getType(Float.class), Type.getType(float.class)), false);
        } else if (Type.getDescriptor(int.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Integer.class), "valueOf", Type.getMethodDescriptor(Type.getType(Integer.class), Type.getType(int.class)), false);
        } else if (Type.getDescriptor(double.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Double.class), "valueOf", Type.getMethodDescriptor(Type.getType(Double.class), Type.getType(double.class)), false);
        } else if (Type.getDescriptor(long.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", Type.getMethodDescriptor(Type.getType(Long.class), Type.getType(long.class)), false);
        } else if (Type.getDescriptor(boolean.class).equals(stackTopTypeDesc)) {
            adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Boolean.class), "valueOf", Type.getMethodDescriptor(Type.getType(Boolean.class), Type.getType(boolean.class)), false);
        } else {
        }
    }

    private static int loadLocal(int localNum, String localTypeDesc, AdviceAdapter adviceAdapter) {
        if (Type.getDescriptor(byte.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.ILOAD, localNum);
            return localNum + 1;
        } else if (Type.getDescriptor(char.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.ILOAD, localNum);
            return localNum + 1;
        } else if (Type.getDescriptor(short.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.ILOAD, localNum);
            return localNum + 1;
        } else if (Type.getDescriptor(float.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.FLOAD, localNum);
            return localNum + 1;
        } else if (Type.getDescriptor(int.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.ILOAD, localNum);
            return localNum + 1;
        } else if (Type.getDescriptor(double.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.DLOAD, localNum);
            return localNum + 2;
        } else if (Type.getDescriptor(long.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.LLOAD, localNum);
            return localNum + 2;
        } else if (Type.getDescriptor(boolean.class).equals(localTypeDesc)) {
            adviceAdapter.visitVarInsn(Opcodes.ILOAD, localNum);
            return localNum + 1;
        } else {
            adviceAdapter.visitVarInsn(Opcodes.ALOAD, localNum);
            return localNum + 1;
        }
    }
}
