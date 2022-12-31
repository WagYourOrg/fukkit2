package xyz.wagyourtail.fukkit2.compat;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.refmap.IReferenceMapper;
import org.spongepowered.asm.mixin.transformer.ClassInfo.Method;
import org.spongepowered.asm.util.Annotations;
import xyz.wagyourtail.fukkit2.util.MixinUtils;
import xyz.wagyourtail.fukkit2.util.RemappingUtils;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class InterceptingMixinPlugin extends EmptyMixinPlugin {
	private @interface From {
		String method();
	}

	protected Method findShim(MixinUtils.Mixin mixin, MethodNode shim, boolean isSurrogate) {
		for (Method realMethod : mixin.getMethods()) {
			if (realMethod.getOriginalName().equals(shim.name) &&
					(isSurrogate || realMethod.getOriginalDesc().equals(shim.desc))) {
				return realMethod;
			}
		}
		throw new IllegalStateException("Cannot find original Mixin method for shim " + shim.name + shim.desc + " in " + mixin);
	}

	protected Map<String, Object> getAnnotationValues(AnnotationNode node) {
		Map<String, Object> values = new HashMap<>();
		if (node.values != null) {
			for (int i = 0; i < node.values.size(); i += 2) {
				values.put((String) node.values.get(i), node.values.get(i + 1));
			}
		}
		return values;
	}

	protected MethodNode getMethodNode(Method method, ClassNode mixinClass) {
		for (MethodNode methodNode : mixinClass.methods) {
			if (methodNode.name.equals(method.getOriginalName()) && methodNode.desc.equals(method.getOriginalDesc())) {
				return methodNode;
			}
		}
		throw new IllegalStateException("Cannot find Mixin method for " + method + " in " + mixinClass + " WHAT?");
	}

	protected String remap(MixinUtils.Mixin mixin, String ref) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		IReferenceMapper mapper = (IReferenceMapper) MethodUtils.invokeMethod(mixin.getInfo().getConfig(), true,"getReferenceMapper");
		return mapper.remap(mixin.getName(), ref);
	}

	protected List<MethodNode> getTargetNodes(List<String> method, MixinUtils.Mixin interceptingMixin, ClassNode targetClass, @Nullable String desc) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		List<MethodNode> nodes = new ArrayList<>();
		List<String> methods = new ArrayList<>();
		for (String m : method) {
		    methods.add(remap(interceptingMixin, m));
		}
		if (desc != null) {
			desc = desc.replaceAll("L" + CallbackInfoReturnable.class.getCanonicalName().replace(".", "/") + ";.+$", "");
			desc = desc.replaceAll("L" + CallbackInfo.class.getCanonicalName().replace(".", "/") + ";.+$", "");
		}
		methods.addAll(method);
		for (String m : methods) {
			if (m.startsWith("L")) {
				m = m.substring(m.indexOf(';') + 1);
			}
			String[] parts = m.split("[(*]");
			String methodName = parts[0];
			String methodDesc = parts.length > 1 ? "(" + parts[1] : desc;

			for (MethodNode methodNode : targetClass.methods) {
				if (methodNode.name.equals(methodName) && (methodDesc == null || methodNode.desc.startsWith(methodDesc))) {
					nodes.add(methodNode);
				}
			}
		}
		return nodes;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		ClassNode thisMixin = MixinUtils.Mixin.create(mixinInfo).getClassNode();

		AnnotationNode interception = Annotations.getInvisible(thisMixin, InterceptingMixin.class);
		if (interception == null) return; //Nothing to do for this particular Mixin

		MixinUtils.Mixin interceptionMixin = findMixin(targetClassName, Annotations.getValue(interception, "value", true));

		for (MethodNode method : thisMixin.methods) {
			AnnotationNode surrogateNode = Annotations.getInvisible(method, PlacatingSurrogate.class);
			AnnotationNode poundSandNode = Annotations.getInvisible(method, PoundSand.class);

			if (surrogateNode != null || poundSandNode != null) {
				Method realMethod = findShim(interceptionMixin, method, surrogateNode != null);
				if (surrogateNode != null) {
					Annotations.setInvisible(method, From.class, "method", method.name.concat(method.desc));
					method.name = realMethod.getName(); //Mangle name to whatever Mixin is using for the real injection
					method.invisibleAnnotations.remove(surrogateNode);
					Annotations.setVisible(method, Surrogate.class);

					String coercedDesc = coerceDesc(method);
					if (coercedDesc != null) method.desc = coercedDesc;

					assert Modifier.isStatic(method.access) == realMethod.isStatic();
					targetClass.methods.add(method);
				}

				if (poundSandNode != null) {
					try {
						doPoundSand(getMethodNode(realMethod, interceptionMixin.getClassNode()), interceptionMixin, targetClass);
					} catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	protected void doPoundSand(MethodNode realMethodNode, MixinUtils.Mixin interceptingMixin, ClassNode targetClass) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
		throw new UnsupportedOperationException("PoundSand is not yet implemented");
//		// get the target method
//		AnnotationNode redirectNode = Annotations.getVisible(realMethodNode, Redirect.class);
//		AnnotationNode injectNode = Annotations.getVisible(realMethodNode, Inject.class);
//
//		if (redirectNode == null && injectNode == null) {
//			throw new IllegalStateException("Cannot find Redirect or Inject annotation for PoundSand method " + realMethodNode.name + realMethodNode.desc);
//		}
//
//		if (redirectNode != null) {
//			Map<String, Object> redirectValues = getAnnotationValues(redirectNode);
//			List<String> methodValues = (List) redirectValues.get("method");
//			List<MethodNode> targetNodes = getTargetNodes(methodValues, interceptingMixin, targetClass, null);
//			for (MethodNode targetNode : targetNodes) {
//				System.out.println("PoundSand: " + targetNode.name + targetNode.desc);
//			}
//		} else if (injectNode != null) {
//			Map<String, Object> injectValues = getAnnotationValues(injectNode);
//			List<String> methodValues = (List) injectValues.get("method");
//			List<MethodNode> targetNodes = getTargetNodes(methodValues, interceptingMixin, targetClass, realMethodNode.desc);
//			for (MethodNode targetNode : targetNodes) {
//				System.out.println("PoundSand: " + targetNode.name + targetNode.desc);
//			}
//		}

	}

	protected static MixinUtils.Mixin findMixin(String targetClass, Collection<String> mixinTargets) {
		mixinTargets = ImmutableSet.copyOf(mixinTargets);

		for (MixinUtils.Mixin mixin : MixinUtils.getMixinsFor(targetClass)) {
			if (mixinTargets.contains(mixin.getName())) {
				return mixin;
			}
		}

		throw new IllegalArgumentException("Can't find Mixin class" + (mixinTargets.size() != 1 ? "es " : ' ') + String.join(", ", mixinTargets) + " targetting " + targetClass);
	}

	protected static String coerceDesc(MethodNode method) {
		if (method.invisibleParameterAnnotations != null) {
			Type[] arguments = Type.getArgumentTypes(method.desc);
			boolean madeChange = false;

			for (int i = 0, end = arguments.length; i < end; i++) {
				AnnotationNode coercionNode = Annotations.getInvisibleParameter(method, LoudCoerce.class, i);

				if (coercionNode != null) {
					String type = Annotations.getValue(coercionNode);

					if (Annotations.<Boolean>getValue(coercionNode, "remap") != Boolean.FALSE) {
						type = RemappingUtils.getClassName(type);
					}

					arguments[i] = Type.getObjectType(type);
					madeChange = true;
				}
			}

			if (madeChange) return Type.getMethodDescriptor(Type.getReturnType(method.desc), arguments);
		}

		return null;
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		ClassNode thisMixin = MixinUtils.Mixin.create(mixinInfo).getClassNode();

		AnnotationNode interception = Annotations.getInvisible(thisMixin, InterceptingMixin.class);
		if (interception == null) return; //Nothing to do for this particular Mixin

		MixinUtils.Mixin interceptionMixin = findMixin(targetClassName, Annotations.getValue(interception, "value", true));
		Map<String, Method> shims = thisMixin.methods.stream().filter(method -> Annotations.getInvisible(method, Shim.class) != null).collect(Collectors.toMap(method -> method.name.concat(method.desc), method -> {
			Method realMethod = interceptionMixin.getMethod(method.name, MoreObjects.firstNonNull(coerceDesc(method), method.desc));

			if (realMethod == null) {
				throw new IllegalStateException("Cannot find shim method " + method.name + method.desc + " in " + interceptionMixin);
			}

			assert method.name.equals(realMethod.getOriginalName());
			assert Modifier.isStatic(method.access) == realMethod.isStatic();
			return realMethod;
		}));
		if (shims.isEmpty()) return; //Nothing to do

		Map<String, Consumer<MethodNode>> surrogates = new HashMap<>();
		targetClassName = targetClassName.replace('.', '/');

		for (Iterator<MethodNode> it = targetClass.methods.iterator(); it.hasNext();) {
			MethodNode method = it.next();

			AnnotationNode from;
			if (shims.containsKey(method.name.concat(method.desc))) {
				it.remove(); //Don't want to keep the shim methods
			} else if ((from = Annotations.getInvisible(method, From.class)) != null) {
				String origin = Annotations.getValue(from, "method");

				Consumer<MethodNode> copier = surrogates.remove(origin);
				if (copier != null) {
					copier.accept(method);
				} else {
					surrogates.put(origin, placatingSurrogate -> {
						method.instructions = placatingSurrogate.instructions;
						method.invisibleAnnotations.remove(from);
					});
				}
			} else {
				method.desc = StringUtils.replace(method.desc, "Lnull;", "Ljava/lang/Object;");

				for (AbstractInsnNode insn : method.instructions) {
					if (insn.getType() == AbstractInsnNode.METHOD_INSN) {
						MethodInsnNode methodInsn = (MethodInsnNode) insn;

						Method replacedMethod = shims.get(methodInsn.name.concat(methodInsn.desc));
						if (replacedMethod != null && targetClassName.equals(methodInsn.owner)) {
							methodInsn.name = replacedMethod.getName();

							if (!methodInsn.desc.equals(replacedMethod.getDesc())) {
								Type[] existingArgs = Type.getArgumentTypes(methodInsn.desc);
								Type[] replacementArgs = Type.getArgumentTypes(replacedMethod.getDesc());

								for (int index = 0, end = existingArgs.length; index < end; index++) {
									if (!existingArgs[index].equals(replacementArgs[index])) {
										AbstractInsnNode target = insn;

										for (int i = end - 1; i > index; i--) {
											do {//If target is ever null the method underflowed instructions => would fail verification anyway
												target = Objects.requireNonNull(target.getPrevious());
											} while (target.getType() == AbstractInsnNode.LINE || target.getType() == AbstractInsnNode.LABEL);
										}

										if (target.getType() != AbstractInsnNode.VAR_INSN || target.getOpcode() != existingArgs[index].getOpcode(Opcodes.ILOAD)) {
											//Be under no illusions that passing this is necessarily safe, just it's more probably save than entering here
											throw new UnsupportedOperationException("Unexpectedly complex stack unwinding requested");
										}

										method.instructions.insertBefore(target, new TypeInsnNode(Opcodes.CHECKCAST, replacementArgs[index].getInternalName()));
									}
								}

								methodInsn.desc = replacedMethod.getDesc();
							}
						}
					}
				}

				if (Annotations.getInvisible(method, PlacatingSurrogate.class) != null) {
					it.remove(); //Don't actually need the method itself, just its contents
					String origin = method.name.concat(method.desc);

					Consumer<MethodNode> copier = surrogates.remove(origin);
					if (copier != null) {
						copier.accept(method);
					} else {
						surrogates.put(origin, realSurrogate -> {
							realSurrogate.instructions = method.instructions;
							realSurrogate.invisibleAnnotations.remove(Annotations.getInvisible(realSurrogate, From.class));
						});
					}
				}
			}
		}
	}
}