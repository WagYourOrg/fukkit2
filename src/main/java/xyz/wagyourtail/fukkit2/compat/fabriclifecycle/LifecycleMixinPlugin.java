package xyz.wagyourtail.fukkit2.compat.fabriclifecycle;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixinPlugin;
import xyz.wagyourtail.fukkit2.util.RemappingUtils;

public class LifecycleMixinPlugin extends InterceptingMixinPlugin {
    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "LivingEntityMixin":
                break;
            case "WorldChunkMixin":
                String setBlockEntity = RemappingUtils.getMethodName("class_2791", "method_12007", "(Lnet/minecraft/class_2586;)V");
                String setBlockEntityDesc=  RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_2586;)V");
                String setRemoved = RemappingUtils.getMethodName("class_2586", "method_11012", "()V");
                for (MethodNode meth : targetClass.methods) {
                    if (meth.name.equals(setBlockEntity) && meth.desc.equals(setBlockEntityDesc)) {
                        // find setRemoved
                        for (AbstractInsnNode node : meth.instructions) {
                            if (node instanceof MethodInsnNode) {
                                MethodInsnNode mnode = (MethodInsnNode) node;
                                if (mnode.name.equals(setRemoved) && mnode.desc.equals("()V")) {
                                    // copy node and insert it before
                                    meth.instructions.insertBefore(node, mnode.clone(null));
                                    // insert label before
                                    meth.instructions.insertBefore(node, new LabelNode());
                                    // remove original
                                    meth.instructions.remove(node);
                                    break;
                                }
                            }
                        }
                    }
                }
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
