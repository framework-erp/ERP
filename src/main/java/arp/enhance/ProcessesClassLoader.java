package arp.enhance;

import arp.annotation.Process;
import arp.process.ProcessWrapper;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zheng chengdong
 */
public class ProcessesClassLoader extends ClassLoader {

    public ProcessesClassLoader(ClassLoader parent) {
        super(parent);
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        System.out.println(name);
        try {
            //系统初始和jdk的
            Class<?> cls = getParent().loadClass(name);
            if (cls != null) {
                return cls;
            }
        } catch (ClassNotFoundException e) {
        }
        if (name.startsWith("org.objectweb.")) {
            return super.loadClass(name, resolve);
        }
        if (name.startsWith("arp.enhance.")) {
            return super.loadClass(name, resolve);
        }
        String path = name.replace('.', '/') + ".class";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        try {
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            ResolvedClass rc = parseProcess(bytes);
            if (rc != null) {
                byte[] enhancedBytes = enhanceProcess(rc);
                Class<?> cls = defineClass(rc.getName(),enhancedBytes,0,enhancedBytes.length);
                return cls;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.loadClass(name, resolve);
    }

    private static byte[] enhanceProcess(ResolvedClass resolvedClass) {
        Map<String, ProcessInfo> processInfos = resolvedClass.getProcessInfos();
        ClassReader cr = new ClassReader(resolvedClass.getClassBytes());
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String mthName, String mthDesc, String signature, String[] exceptions) {
                Type[] argumentTypes = Type.getArgumentTypes(mthDesc);
                String returnTypeDesc = mthDesc.substring(mthDesc.indexOf(")") + 1);
                return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {

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

                            visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class), "afterProcessFaild", "()V", false);

                            throwException();

                        }
                        super.visitMaxs(maxStack, maxLocals);
                    }

                    protected void onMethodExit(int opcode) {
                        ProcessInfo processInfo = processInfos.get(mthName + "@" + mthDesc);
                        if (processInfo != null) {
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

    private static ResolvedClass parseProcess(byte[] bytes) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        Map<String, Object> clsInfoMap = new HashMap<>();
        Map<String, ProcessInfo> processInfos = new HashMap<>();
        final ResolvedClass rc = new ResolvedClass();
        cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

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
                return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {
                    private boolean isProcess;
                    private ProcessInfo processInfo = null;

                    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                        isProcess = Type.getDescriptor(Process.class).equals(desc);
                        if (isProcess) {
                            processInfo = new ProcessInfo((String) clsInfoMap.get("name"), mthName, mthDesc);
                        }
                        return super.visitAnnotation(desc, visible);
                    }

                    protected void onMethodEnter() {
                        if (isProcess) {
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

}
