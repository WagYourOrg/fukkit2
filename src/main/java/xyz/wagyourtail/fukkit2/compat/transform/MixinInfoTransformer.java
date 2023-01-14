package xyz.wagyourtail.fukkit2.compat.transform;

import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CASM;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@CTransformer(name = "org.spongepowered.asm.mixin.transformer.MixinInfo")
public class MixinInfoTransformer {

//    private static final Method method;
//
//    static {
//        try {
//            Class<?> mixinTransformerClass = Class.forName("xyz.wagyourtail.fukkit2.compat.MixinTransformer", true, Knot.getLauncher().getTargetClassLoader());
//            method = mixinTransformerClass.getDeclaredMethod("transform", String.class, ClassNode.class);
//        } catch (Throwable t) {
//            throw new RuntimeException("Failed to get MixinTransformer#transform method", t);
//        }
//    }
//
//    @CInject(method = "loadMixinClass", target = @CTarget("RETURN"), cancellable = true)
//    public void injectIntoMixins(String mixinClassName, InjectionCallback ic) throws InvocationTargetException, IllegalAccessException {
//        ic.setReturnValue(method.invoke(null, mixinClassName, ic.getReturnValue()));
//    }

    @CASM
    public static void transform(ClassNode clazz) {
        // modify Lorg/spongepowered/asm/mixin/transformer/MixinInfo;loadMixinClass(Ljava/lang/String;)Lorg/objectweb/asm/tree/ClassNode;
        // to call Lxyz/wagyourtail/fukkit2/compat/MixinTransformer;transform(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Lorg/objectweb/asm/tree/ClassNode;
        // before return

        for (MethodNode method : clazz.methods) {
            if (method.name.equals("loadMixinClass") && method.desc.equals("(Ljava/lang/String;)Lorg/objectweb/asm/tree/ClassNode;")) {
                // find areturn
                AbstractInsnNode prev = null;
                for (AbstractInsnNode node : method.instructions) {
                    if (node.getOpcode() == Opcodes.ARETURN) {
                        // insert before aload 2
                        method.instructions.insertBefore(prev, new VarInsnNode(Opcodes.ALOAD, 1));
                        // insert before areturn
                        method.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "xyz/wagyourtail/fukkit2/compat/MixinTransformer", "transform", "(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;)Lorg/objectweb/asm/tree/ClassNode;", false));

                        //result
                        // aload 1
                        // aload 2
                        // invokestatic

                        break;
                    }
                    prev = node;
                }
                return;
            }
        }

    }

}
