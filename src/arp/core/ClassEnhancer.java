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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jdk.internal.org.objectweb.asm.AnnotationVisitor;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.Label;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import jdk.internal.org.objectweb.asm.commons.AdviceAdapter;

public class ClassEnhancer {

	public static void enhance(String... pkgs) throws Exception {
		if (pkgs != null) {
			Map<String, byte[]> enhancedClassBytes = new HashMap<>();
			List<Map<String, Object>> listnersList = new ArrayList<>();
			for (int i = 0; i < pkgs.length; i++) {
				enhanceClassesForPackage(pkgs[i], enhancedClassBytes);
			}

			for (byte[] bytes : enhancedClassBytes.values()) {
				parseListeners(bytes, listnersList);
			}

			if (!listnersList.isEmpty()) {
				createMessageProcessorClasses(listnersList);
				enhanceClassesForListners(enhancedClassBytes, listnersList);
			}
			loadClasses(enhancedClassBytes);
		}
	}

	private static void createMessageProcessorClasses(List<Map<String, Object>> listnersList) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class cls = Class.forName("java.lang.ClassLoader");
		java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass",
				new Class[] { String.class, byte[].class, int.class, int.class });
		method.setAccessible(true);
		for (Map<String, Object> listenerData : listnersList) {
			Type processOutputType = (Type) listenerData.get("processOutputType");
			String listenerProcessObjType = (String) listenerData.get("listenerProcessObjType");
			String messageProcessorClasseType = listenerProcessObjType.replace('.', '/') + "/MessageProcessor_"
					+ (String) listenerData.get("listenerMthName");
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, messageProcessorClasseType, null,
					Type.getType(Object.class).getInternalName(),
					new String[] { Type.getType(MessageProcessor.class).getInternalName() });
			FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "processor",
					"L" + listenerProcessObjType.replace('.', '/') + ";", null, null);
			fv.visitEnd();

			MethodVisitor cmv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>",
					Type.getMethodDescriptor(Type.getType(void.class),
							Type.getType("L" + listenerProcessObjType.replace('.', '/') + ";")),
					null, null);
			cmv.visitMaxs(2, 2);
			cmv.visitCode();
			cmv.visitVarInsn(Opcodes.ALOAD, 0);
			cmv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);
			cmv.visitVarInsn(Opcodes.ALOAD, 0);
			cmv.visitVarInsn(Opcodes.ALOAD, 1);
			cmv.visitFieldInsn(Opcodes.PUTFIELD, messageProcessorClasseType, "processor",
					"L" + listenerProcessObjType.replace('.', '/') + ";");
			cmv.visitInsn(Opcodes.RETURN);
			cmv.visitEnd();

			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "process",
					Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object.class)), null, null);
			mv.visitMaxs(2, 2);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, messageProcessorClasseType, "processor",
					"L" + listenerProcessObjType.replace('.', '/') + ";");
			if (processOutputType != null) {
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitTypeInsn(Opcodes.CHECKCAST, processOutputType.getInternalName());
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, listenerProcessObjType.replace('.', '/'),
						(String) listenerData.get("listenerMthName"), (String) listenerData.get("listenerMthDesc"),
						false);
			} else {
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, listenerProcessObjType.replace('.', '/'),
						(String) listenerData.get("listenerMthName"), (String) listenerData.get("listenerMthDesc"),
						false);
			}
			mv.visitInsn(Opcodes.RETURN);
			mv.visitEnd();

			byte[] enhancedBytes = cw.toByteArray();

//			File outputFile = new File("output.class");
//			FileOutputStream outputFileStream = null;
//			outputFileStream = new FileOutputStream(outputFile);
//			outputFileStream.write(enhancedBytes);
//			outputFileStream.close();

			Object[] argArray = new Object[] { messageProcessorClasseType.replace('/', '.'), enhancedBytes,
					new Integer(0), new Integer(enhancedBytes.length) };
			method.invoke(cl, argArray);

		}
		method.setAccessible(false);
	}

	private static void enhanceClassesForListners(Map<String, byte[]> enhancedClassBytes,
			List<Map<String, Object>> listnersList) {
		int startIdx = 0;
		int listnersCount = 0;
		String listenerProcessObjType = null;
		for (int i = 0; i < listnersList.size(); i++) {
			Map<String, Object> listenerData = listnersList.get(i);
			if (listenerData.get("listenerProcessObjType").equals(listenerProcessObjType)) {
				listnersCount++;
			} else {
				enhanceProcessClassWithListners(enhancedClassBytes, listenerProcessObjType, startIdx, listnersCount,
						listnersList);
				startIdx = i;
				listnersCount = 1;
				listenerProcessObjType = (String) listenerData.get("listenerProcessObjType");
			}
		}
		enhanceProcessClassWithListners(enhancedClassBytes, listenerProcessObjType, startIdx, listnersCount,
				listnersList);
	}

	private static void enhanceProcessClassWithListners(Map<String, byte[]> enhancedClassBytes,
			String listenerProcessObjType, int startIdx, int listnersCount, List<Map<String, Object>> listnersList) {
		if (listnersCount == 0) {
			return;
		}
		byte[] bytes = enhancedClassBytes.get(listenerProcessObjType);
		// 所有构造器要注入
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature,
					String[] exceptions) {
				return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, name, desc, signature, exceptions),
						access, name, desc) {

					protected void onMethodExit(int opcode) {
						if (name.equals("<init>")) {
							for (int i = 0; i < listnersCount; i++) {
								Map<String, Object> listenerData = listnersList.get(startIdx + i);
								String processDesc = (String) listenerData.get("processDesc");
								String listenerProcessObjType = (String) listenerData.get("listenerProcessObjType");
								String messageProcessorClasseType = listenerProcessObjType.replace('.', '/')
										+ "/MessageProcessor_" + (String) listenerData.get("listenerMthName");

								visitLdcInsn(processDesc);
								visitTypeInsn(Opcodes.NEW, messageProcessorClasseType);
								visitInsn(Opcodes.DUP);
								visitVarInsn(Opcodes.ALOAD, 0);
								visitMethodInsn(Opcodes.INVOKESPECIAL, messageProcessorClasseType, "<init>",
										Type.getMethodDescriptor(Type.getType(void.class),
												Type.getType("L" + listenerProcessObjType.replace('.', '/') + ";")),
										false);
								visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ARP.class),
										"registerMessageProcessor", Type.getMethodDescriptor(Type.getType(void.class),
												Type.getType(String.class), Type.getType(MessageProcessor.class)),
										false);
							}
						}
						super.onMethodExit(opcode);
					}

				};
			}

		}, ClassReader.EXPAND_FRAMES);
		byte[] enhancedBytes = cw.toByteArray();
		enhancedClassBytes.put(listenerProcessObjType, enhancedBytes);
	}

	private static void enhanceClassesForPackage(String pkg, Map<String, byte[]> enhancedClassBytes) throws Exception {
		String pkgDir = pkg.replace('.', '/');
		URL url = Thread.currentThread().getContextClassLoader().getResource(pkgDir);
		Path path = Paths.get(new URI(url.toURI().toString()));
		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				byte[] bytes = Files.readAllBytes(file);
				enhanceProcess(bytes, enhancedClassBytes);
				return FileVisitResult.CONTINUE;
			}

		});

	}

	private static void parseListeners(byte[] bytes, List<Map<String, Object>> listnersList) {
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
			public MethodVisitor visitMethod(int access, String mthName, String mthDesc, String signature,
					String[] exceptions) {
				Type[] argumentTypes = Type.getArgumentTypes(mthDesc);
				return new AdviceAdapter(Opcodes.ASM5,
						super.visitMethod(access, mthName, mthDesc, signature, exceptions), access, mthName, mthDesc) {

					private boolean isListener;

					public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
						isListener = Type.getDescriptor(Listener.class).equals(desc);
						if (isListener) {
							return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {
								@Override
								public void visit(String name, Object value) {
									if ("value".equals(name)) {
										Map<String, Object> listenerData = new HashMap<>();
										listenerData.put("processDesc", value);// 源过程描述
										if (argumentTypes != null && argumentTypes.length > 0) {
											listenerData.put("processOutputType", argumentTypes[0]);// 源过程输出类型
										}
										listenerData.put("listenerProcessObjType", clsInfoMap.get("name"));// listener所在的处理类类型
										listenerData.put("listenerMthName", mthName);
										listenerData.put("listenerMthDesc", mthDesc);
										listnersList.add(listenerData);
									}
									super.visit(name, value);
								}
							};
						}
						return super.visitAnnotation(desc, visible);
					}

				};
			}

		}, ClassReader.EXPAND_FRAMES);
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
									if ("publish".equals(name) && Boolean.TRUE.equals(value)) {
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
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class)),
											false);
								} else if (Type.getDescriptor(byte.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(byte.class)),
											false);
								} else if (Type.getDescriptor(char.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(char.class)),
											false);
								} else if (Type.getDescriptor(short.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(short.class)),
											false);
								} else if (Type.getDescriptor(float.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(float.class)),
											false);
								} else if (Type.getDescriptor(int.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(int.class)),
											false);
								} else if (Type.getDescriptor(double.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP2);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(double.class)),
											false);
								} else if (Type.getDescriptor(long.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP2);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(long.class)),
											false);
								} else if (Type.getDescriptor(boolean.class).equals(returnTypeDesc)) {
									visitInsn(Opcodes.DUP);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(String.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(boolean.class)),
											false);
								} else {
									visitInsn(Opcodes.DUP);
									visitLdcInsn(clsInfoMap.get("name"));
									visitLdcInsn(name);
									visitLdcInsn(desc);
									visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(ProcessPublisher.class),
											"publish",
											Type.getMethodDescriptor(Type.getType(void.class),
													Type.getType(Object.class), Type.getType(String.class),
													Type.getType(String.class), Type.getType(String.class)),
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
