package arp.enhance;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import arp.ARP;
import arp.process.Process;
import arp.process.ProcessWrapper;
import arp.process.publish.ProcessListenerMessageProcessor;

public class ClassEnhancer {

	public static ClassParseResult parseResult;

	public static ClassParseResult parseAndEnhance(String... pkgs)
			throws Exception {
		if (pkgs != null) {
			ClassParseResult result = new ClassParseResult();
			Map<String, byte[]> enhancedClassBytes = new HashMap<>();
			List<ProcessInfo> processInfoList = new ArrayList<>();
			for (int i = 0; i < pkgs.length; i++) {
				enhanceClassesForPackage(pkgs[i], enhancedClassBytes,
						processInfoList);
			}

			result.setProcessInfoList(processInfoList);

			if (!processInfoList.isEmpty()) {
				createMessageProcessorClasses(processInfoList);
				enhanceClassesForListners(enhancedClassBytes, processInfoList);
			}
			loadClasses(enhancedClassBytes);
			ClassEnhancer.parseResult = result;
			return result;
		}
		return null;
	}

	private static void createMessageProcessorClasses(
			List<ProcessInfo> processInfoList) throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class cls = Class.forName("java.lang.ClassLoader");
		java.lang.reflect.Method method = cls
				.getDeclaredMethod("defineClass", new Class[] { String.class,
						byte[].class, int.class, int.class });
		method.setAccessible(true);
		for (ProcessInfo processInfo : processInfoList) {
			ListenerInfo listenerInfo = processInfo.getListenerInfo();
			if (listenerInfo == null) {
				continue;
			}
			Type processOutputType = listenerInfo.getProcessOutputType();
			String listenerProcessObjType = listenerInfo
					.getListenerProcessObjType();
			String messageProcessorClasseType = listenerProcessObjType.replace(
					'.', '/')
					+ "/MessageProcessor_"
					+ listenerInfo.getListenerMthName();
			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cw.visit(
					Opcodes.V1_8,
					Opcodes.ACC_PUBLIC,
					messageProcessorClasseType,
					null,
					Type.getType(Object.class).getInternalName(),
					new String[] { Type.getType(
							ProcessListenerMessageProcessor.class)
							.getInternalName() });
			FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "processor",
					"L" + listenerProcessObjType.replace('.', '/') + ";", null,
					null);
			fv.visitEnd();

			MethodVisitor cmv = cw.visitMethod(
					Opcodes.ACC_PUBLIC,
					"<init>",
					Type.getMethodDescriptor(
							Type.getType(void.class),
							Type.getType("L"
									+ listenerProcessObjType.replace('.', '/')
									+ ";")), null, null);
			cmv.visitMaxs(2, 2);
			cmv.visitCode();
			cmv.visitVarInsn(Opcodes.ALOAD, 0);
			cmv.visitMethodInsn(Opcodes.INVOKESPECIAL,
					Type.getInternalName(Object.class), "<init>", "()V", false);
			cmv.visitVarInsn(Opcodes.ALOAD, 0);
			cmv.visitVarInsn(Opcodes.ALOAD, 1);
			cmv.visitFieldInsn(Opcodes.PUTFIELD, messageProcessorClasseType,
					"processor", "L" + listenerProcessObjType.replace('.', '/')
							+ ";");
			cmv.visitInsn(Opcodes.RETURN);
			cmv.visitEnd();

			MethodVisitor mv = cw.visitMethod(
					Opcodes.ACC_PUBLIC,
					"process",
					Type.getMethodDescriptor(Type.VOID_TYPE,
							Type.getType(Object.class)), null, null);
			mv.visitMaxs(2, 2);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, messageProcessorClasseType,
					"processor", "L" + listenerProcessObjType.replace('.', '/')
							+ ";");
			if (processOutputType != null) {
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitTypeInsn(Opcodes.CHECKCAST,
						processOutputType.getInternalName());
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						listenerProcessObjType.replace('.', '/'),
						listenerInfo.getListenerMthName(),
						listenerInfo.getListenerMthDesc(), false);
			} else {
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						listenerProcessObjType.replace('.', '/'),
						listenerInfo.getListenerMthName(),
						listenerInfo.getListenerMthDesc(), false);
			}
			mv.visitInsn(Opcodes.RETURN);
			mv.visitEnd();

			byte[] enhancedBytes = cw.toByteArray();

			// File outputFile = new File("output.class");
			// FileOutputStream outputFileStream = null;
			// outputFileStream = new FileOutputStream(outputFile);
			// outputFileStream.write(enhancedBytes);
			// outputFileStream.close();

			Object[] argArray = new Object[] {
					messageProcessorClasseType.replace('/', '.'),
					enhancedBytes, new Integer(0),
					new Integer(enhancedBytes.length) };
			method.invoke(cl, argArray);

		}
		method.setAccessible(false);
	}

	private static void enhanceClassesForListners(
			Map<String, byte[]> enhancedClassBytes,
			List<ProcessInfo> processInfoList) {
		int startIdx = 0;
		int listnersCount = 0;
		String listenerProcessObjType = null;
		for (int i = 0; i < processInfoList.size(); i++) {
			ProcessInfo processInfo = processInfoList.get(i);
			ListenerInfo listenerInfo = processInfo.getListenerInfo();
			if (listenerInfo == null) {
				continue;
			}
			if (listenerInfo.getListenerProcessObjType().equals(
					listenerProcessObjType)) {
				listnersCount++;
			} else {
				enhanceProcessClassWithListners(enhancedClassBytes,
						listenerProcessObjType, startIdx, listnersCount,
						processInfoList);
				startIdx = i;
				listnersCount = 1;
				listenerProcessObjType = listenerInfo
						.getListenerProcessObjType();
			}
		}
		enhanceProcessClassWithListners(enhancedClassBytes,
				listenerProcessObjType, startIdx, listnersCount,
				processInfoList);
	}

	private static void enhanceProcessClassWithListners(
			Map<String, byte[]> enhancedClassBytes,
			String listenerProcessObjType, int startIdx, int listnersCount,
			List<ProcessInfo> processInfoList) {
		if (listnersCount == 0) {
			return;
		}
		byte[] bytes = enhancedClassBytes.get(listenerProcessObjType);
		// 所有构造器要注入
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

			@Override
			public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(
						access, name, desc, signature, exceptions), access,
						name, desc) {

					protected void onMethodExit(int opcode) {
						if (name.equals("<init>")) {
							for (int i = 0; i < listnersCount; i++) {
								ProcessInfo processInfo = processInfoList
										.get(startIdx + i);
								ListenerInfo listenerInfo = processInfo
										.getListenerInfo();
								if (listenerInfo == null) {
									continue;
								}
								String processDesc = listenerInfo
										.getProcessDesc();
								String listenerProcessObjType = listenerInfo
										.getListenerProcessObjType();
								String messageProcessorClasseType = listenerProcessObjType
										.replace('.', '/')
										+ "/MessageProcessor_"
										+ listenerInfo.getListenerMthName();

								visitLdcInsn(processDesc);
								visitTypeInsn(Opcodes.NEW,
										messageProcessorClasseType);
								visitInsn(Opcodes.DUP);
								visitVarInsn(Opcodes.ALOAD, 0);
								visitMethodInsn(
										Opcodes.INVOKESPECIAL,
										messageProcessorClasseType,
										"<init>",
										Type.getMethodDescriptor(
												Type.getType(void.class),
												Type.getType("L"
														+ listenerProcessObjType
																.replace('.',
																		'/')
														+ ";")), false);
								visitMethodInsn(
										Opcodes.INVOKESTATIC,
										Type.getInternalName(ARP.class),
										"registerMessageProcessor",
										Type.getMethodDescriptor(
												Type.getType(void.class),
												Type.getType(String.class),
												Type.getType(ProcessListenerMessageProcessor.class)),
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

	private static void enhanceClassesForPackage(String pkg,
			Map<String, byte[]> enhancedClassBytes,
			List<ProcessInfo> processInfoList) throws Exception {
		String pkgDir = pkg.replace('.', '/');
		URI uri = Thread.currentThread().getContextClassLoader()
				.getResource(pkgDir).toURI();

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
				FileSystem zipfs = FileSystems.newFileSystem(zipFile, env);
				rootPath = zipfs.getPath(pathInFile);
			} catch (Exception e) {
			}
		} else {
			rootPath = Paths.get(uri);
		}

		Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file,
					BasicFileAttributes attrs) throws IOException {
				byte[] bytes = Files.readAllBytes(file);
				enhanceProcess(bytes, enhancedClassBytes, processInfoList);
				return FileVisitResult.CONTINUE;
			}

		});

	}

	private static void loadClasses(Map<String, byte[]> enhancedClassBytes)
			throws Exception {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class cls = Class.forName("java.lang.ClassLoader");
		java.lang.reflect.Method method = cls
				.getDeclaredMethod("defineClass", new Class[] { String.class,
						byte[].class, int.class, int.class });
		method.setAccessible(true);

		for (Entry<String, byte[]> entry : enhancedClassBytes.entrySet()) {
			String clsName = entry.getKey();
			byte[] bytes = entry.getValue();
			Object[] argArray = new Object[] { clsName, bytes, new Integer(0),
					new Integer(bytes.length) };
			method.invoke(cl, argArray);
		}

		method.setAccessible(false);

	}

	private static void enhanceProcess(byte[] bytes,
			Map<String, byte[]> enhancedClassBytes,
			List<ProcessInfo> processInfoList) {
		ClassReader cr = new ClassReader(bytes);
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		Map<String, Object> clsInfoMap = new HashMap<>();
		cr.accept(new ClassVisitor(Opcodes.ASM5, cw) {

			public void visit(int version, int access, String name,
					String signature, String superName, String[] interfaces) {
				clsInfoMap.put("name", name.replace('/', '.'));
				super.visit(version, access, name, signature, superName,
						interfaces);
			}

			@Override
			public MethodVisitor visitMethod(int access, String mthName,
					String mthDesc, String signature, String[] exceptions) {
				// 过滤构造器
				if ("<init>".equals(mthName)) {
					return super.visitMethod(access, mthName, mthDesc,
							signature, exceptions);
				}
				Type[] argumentTypes = Type.getArgumentTypes(mthDesc);
				String returnTypeDesc = mthDesc.substring(mthDesc.indexOf(")") + 1);
				return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(
						access, mthName, mthDesc, signature, exceptions),
						access, mthName, mthDesc) {

					private boolean isProcess;
					private boolean publish;
					private boolean dontPublishWhenResultIsNull;
					private String processName = "";

					private Label lTryBlockStart;
					private Label lTryBlockEnd;

					public AnnotationVisitor visitAnnotation(String desc,
							boolean visible) {
						isProcess = Type.getDescriptor(Process.class).equals(
								desc);
						if (isProcess) {
							clsInfoMap.put("hasProcess", true);
							return new AnnotationVisitor(Opcodes.ASM5, super
									.visitAnnotation(desc, visible)) {
								@Override
								public void visit(String name, Object value) {
									ListenerInfo listenerInfo = null;
									if ("publish".equals(name)
											&& Boolean.TRUE.equals(value)) {
										publish = true;
									} else if ("dontPublishWhenResultIsNull"
											.equals(name)
											&& Boolean.TRUE.equals(value)) {
										dontPublishWhenResultIsNull = true;
									} else if ("name".equals(name)) {
										processName = (String) value;
									} else if ("listening".equals(name)) {
										Type processOutputType = null;
										if (argumentTypes != null
												&& argumentTypes.length > 0) {
											processOutputType = argumentTypes[0];
										}
										listenerInfo = new ListenerInfo(
												(String) value,
												processOutputType,
												(String) clsInfoMap.get("name"),
												mthName, mthDesc);
									}
									processInfoList.add(new ProcessInfo(
											(String) clsInfoMap.get("name"),
											mthName, processName, listenerInfo,
											publish));
									super.visit(name, value);
								}
							};
						}
						return super.visitAnnotation(desc, visible);
					}

					protected void onMethodEnter() {
						if (isProcess) {

							if (publish) {
								visitInsn(Opcodes.ICONST_1);
							} else {
								visitInsn(Opcodes.ICONST_0);
							}
							visitMethodInsn(
									Opcodes.INVOKESTATIC,
									Type.getInternalName(ProcessWrapper.class),
									"setPublish",
									Type.getMethodDescriptor(
											Type.getType(void.class),
											Type.getType(boolean.class)), false);

							if (publish) {
								visitLdcInsn(clsInfoMap.get("name"));
								visitLdcInsn(mthName);
								visitLdcInsn(processName);
								visitMethodInsn(Opcodes.INVOKESTATIC, Type
										.getInternalName(ProcessWrapper.class),
										"recordProcessDesc",
										Type.getMethodDescriptor(
												Type.getType(void.class),
												Type.getType(String.class),
												Type.getType(String.class),
												Type.getType(String.class)),
										false);

								if (dontPublishWhenResultIsNull) {
									visitInsn(Opcodes.ICONST_1);
								} else {
									visitInsn(Opcodes.ICONST_0);
								}
								visitMethodInsn(Opcodes.INVOKESTATIC, Type
										.getInternalName(ProcessWrapper.class),
										"setDontPublishWhenResultIsNull",
										Type.getMethodDescriptor(
												Type.getType(void.class),
												Type.getType(boolean.class)),
										false);

								if (argumentTypes != null) {
									int localNum = 1;
									for (int argIdx = 0; argIdx < argumentTypes.length; argIdx++) {
										Type argType = argumentTypes[argIdx];
										localNum = loadLocalAndToObject(
												localNum,
												argType.getDescriptor(), this);
										visitMethodInsn(
												Opcodes.INVOKESTATIC,
												Type.getInternalName(ProcessWrapper.class),
												"recordProcessArgument",
												Type.getMethodDescriptor(
														Type.getType(void.class),
														Type.getType(Object.class)),
												false);
									}
								}

							}

							visitMethodInsn(Opcodes.INVOKESTATIC,
									Type.getInternalName(ProcessWrapper.class),
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

							visitMethodInsn(Opcodes.INVOKESTATIC,
									Type.getInternalName(ProcessWrapper.class),
									"afterProcessFaild", "()V", false);

							throwException();

						}
						super.visitMaxs(maxStack, maxLocals);
					}

					protected void onMethodExit(int opcode) {
						if (isProcess) {

							if (publish) {
								if (!Type.getDescriptor(void.class).equals(
										returnTypeDesc)) {
									dupStackTopAndToObject(returnTypeDesc, this);
									visitMethodInsn(
											Opcodes.INVOKESTATIC,
											Type.getInternalName(ProcessWrapper.class),
											"recordProcessResult",
											Type.getMethodDescriptor(
													Type.getType(void.class),
													Type.getType(Object.class)),
											false);

								}
							}

							visitMethodInsn(Opcodes.INVOKESTATIC,
									Type.getInternalName(ProcessWrapper.class),
									"afterProcessFinish", "()V", false);

						}
						super.onMethodExit(opcode);
					}

				};
			}

		}, ClassReader.EXPAND_FRAMES);
		if (Boolean.TRUE.equals(clsInfoMap.get("hasProcess"))) {
			byte[] enhancedBytes = cw.toByteArray();
			enhancedClassBytes.put((String) clsInfoMap.get("name"),
					enhancedBytes);
		}
	}

	private static void dupStackTopAndToObject(String stackTopTypeDesc,
			AdviceAdapter adviceAdapter) {
		dupStackTop(stackTopTypeDesc, adviceAdapter);
		stackTopToObject(stackTopTypeDesc, adviceAdapter);
	}

	private static int loadLocalAndToObject(int localNum, String localTypeDesc,
			AdviceAdapter adviceAdapter) {
		int newLocalNum = loadLocal(localNum, localTypeDesc, adviceAdapter);
		stackTopToObject(localTypeDesc, adviceAdapter);
		return newLocalNum;
	}

	private static int loadLocal(int localNum, String localTypeDesc,
			AdviceAdapter adviceAdapter) {
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

	private static void stackTopToObject(String stackTopTypeDesc,
			AdviceAdapter adviceAdapter) {
		if (Type.getDescriptor(byte.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Byte.class),
					"valueOf",
					Type.getMethodDescriptor(Type.getType(Byte.class),
							Type.getType(byte.class)), false);
		} else if (Type.getDescriptor(char.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type
					.getInternalName(Character.class), "valueOf", Type
					.getMethodDescriptor(Type.getType(Character.class),
							Type.getType(char.class)), false);
		} else if (Type.getDescriptor(short.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Short.class),
					"valueOf",
					Type.getMethodDescriptor(Type.getType(Short.class),
							Type.getType(short.class)), false);
		} else if (Type.getDescriptor(float.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Float.class),
					"valueOf",
					Type.getMethodDescriptor(Type.getType(Float.class),
							Type.getType(float.class)), false);
		} else if (Type.getDescriptor(int.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Integer.class),
					"valueOf",
					Type.getMethodDescriptor(Type.getType(Integer.class),
							Type.getType(int.class)), false);
		} else if (Type.getDescriptor(double.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Double.class),
					"valueOf",
					Type.getMethodDescriptor(Type.getType(Double.class),
							Type.getType(double.class)), false);
		} else if (Type.getDescriptor(long.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Long.class),
					"valueOf",
					Type.getMethodDescriptor(Type.getType(Long.class),
							Type.getType(long.class)), false);
		} else if (Type.getDescriptor(boolean.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitMethodInsn(
					Opcodes.INVOKESTATIC,
					Type.getInternalName(Boolean.class),
					"valueOf",
					Type.getMethodDescriptor(Type.getType(Boolean.class),
							Type.getType(boolean.class)), false);
		} else {
		}
	}

	private static void dupStackTop(String stackTopTypeDesc,
			AdviceAdapter adviceAdapter) {
		if (Type.getDescriptor(long.class).equals(stackTopTypeDesc)
				|| Type.getDescriptor(double.class).equals(stackTopTypeDesc)) {
			adviceAdapter.visitInsn(Opcodes.DUP2);
		} else {
			adviceAdapter.visitInsn(Opcodes.DUP);
		}
	}

}
