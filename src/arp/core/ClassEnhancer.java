package arp.core;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class ClassEnhancer {

	public static void enhance(String... pkgs) throws Exception {
		if (pkgs != null) {
			for (int i = 0; i < pkgs.length; i++) {
				enhanceClassesForPackage(pkgs[i]);
			}
		}
	}

	private static void enhanceClassesForPackage(String pkg) throws Exception {
		String pkgDir = pkg.replace('.', '/');
		URL url = Thread.currentThread().getContextClassLoader().getResource(pkgDir);
		Path path = Paths.get(new URI(url.toURI().toString()));
		Map<String, byte[]> enhancedClassBytes = new HashMap<>();
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				byte[] bytes = Files.readAllBytes(file);
				enhanceProcess(bytes, enhancedClassBytes);
				return FileVisitResult.CONTINUE;
			}

		});
		loadClasses(enhancedClassBytes);
	}

	private static void loadClasses(Map<String, byte[]> enhancedClassBytes) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class cls = Class.forName("java.lang.ClassLoader");
		java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass",
				new Class[] { String.class, byte[].class, int.class, int.class });
		method.setAccessible(true);

		for (Entry<String, byte[]> entry : enhancedClassBytes.entrySet()) {
			String clsName = entry.getKey();
			byte[] bytes = entry.getValue();
			Object[] argArray = new Object[] { clsName, bytes, new Integer(0), new Integer(bytes.length) };
			method.invoke(cl, argArray);
		}

		method.setAccessible(false);

	}

	private static void enhanceProcess(byte[] bytes, Map<String, byte[]> enhancedClassBytes) {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		Map<String, Object> clsInfoMap = new HashMap<>();
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				clsInfoMap.put("name", name.replace('/', '.'));
				super.visit(version, access, name, signature, superName, interfaces);
			}

			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature,
					String[] exceptions) {
				String returnTypeDesc = desc.substring(desc.indexOf(")") + 1);
				return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions),
						access, name, desc) {

					private boolean isProcess;
					private boolean publish;

					private Label lTryBlockStart;
					private Label lTryBlockEnd;

					public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
						isProcess = Type.getDescriptor(Process.class).equals(desc);
						if (isProcess) {
							clsInfoMap.put("hasProcess", true);
							return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {
								@Override
								public void visit(String name, Object value) {
									if ("publish".equals(name)) {
										publish = true;
									}
									super.visit(name, value);
								}
							};
						}
						return super.visitAnnotation(desc, visible);
					}

					protected void onMethodEnter() {
						if (isProcess) {
							visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class),
									"beforeProcessStart", "()V", false);

							lTryBlockStart = new Label();
							lTryBlockEnd = new Label();

							mark(lTryBlockStart);
						}
						super.onMethodEnter();
					}

					public void visitMaxs(int maxStack, int maxLocals) {
						if (isProcess) {
							mark(lTryBlockEnd);
							catchException(lTryBlockStart, lTryBlockEnd, null);

							visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class),
									"afterProcessFaild", "()V", false);

							throwException();

						}
						super.visitMaxs(maxStack, maxLocals);
					}

					protected void onMethodExit(int opcode) {
						if (isProcess) {
							if (publish) {
								if (Type.getDescriptor(void.class).equals(returnTypeDesc)) {
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class)), false);
								} else if (Type.getDescriptor(byte.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(byte.class)),
											false);
								} else if (Type.getDescriptor(char.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(char.class)),
											false);
								} else if (Type.getDescriptor(short.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(short.class)),
											false);
								} else if (Type.getDescriptor(float.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(float.class)),
											false);
								} else if (Type.getDescriptor(int.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class), Type.getType(int.class)),
											false);
								} else if (Type.getDescriptor(double.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP2);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(double.class)),
											false);
								} else if (Type.getDescriptor(long.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP2);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(long.class)),
											false);
								} else if (Type.getDescriptor(boolean.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(boolean.class)),
											false);
								} else {
									visitInsn(Opcodes.DUP);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish", Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(Object.class)),
											false);
								}
							}
							visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessWrapper.class),
									"afterProcessFinish", "()V", false);
						}
						super.onMethodExit(opcode);
					}

				};
			}

		}, ClassReader.EXPAND_FRAMES);
		if (Boolean.TRUE.equals(clsInfoMap.get("hasProcess"))) {
			byte[] enhancedBytes = cw.toByteArray();
			enhancedClassBytes.put((String) clsInfoMap.get("name"), enhancedBytes);
		}
	}
}
