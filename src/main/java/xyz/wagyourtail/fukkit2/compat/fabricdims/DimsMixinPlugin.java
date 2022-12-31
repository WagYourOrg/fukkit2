package xyz.wagyourtail.fukkit2.compat.fabricdims;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixinPlugin;
import xyz.wagyourtail.fukkit2.util.RemappingUtils;

public class DimsMixinPlugin extends InterceptingMixinPlugin {

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "EntityMixin":
            case "ServerPlayerMixin":
                String levelEnd = RemappingUtils.mapFieldName("class_1937", "field_25181", "Lnet/minecraft/class_5321;");
                String levelStemEnd = RemappingUtils.mapFieldName("class_5363", "field_25414", "Lnet/minecraft/class_5321;");
                String changeDimension = RemappingUtils.getMethodName("class_1297", "method_5731", "(Lnet/minecraft/class_3218;)Lnet/minecraft/class_1297;");
//                String changeDimensions = RemappingUtils.mapMethod("", "method_14263", "(Lnet/minecraft/class_5321;Lnet/minecraft/class_5321;)Lnet/minecraft/class_3222;");
                for (MethodNode m : targetClass.methods) {
//                    System.out.println(m.name + m.desc);
                    if (m.name.equals(changeDimension) && !m.desc.contains("TeleportCause")) {
                        System.out.println("Found method: " + m.name + m.desc);
                        AbstractInsnNode first = m.instructions.getFirst();
                        LabelNode skip = new LabelNode();
                        InsnList extra = new InsnList();
                        extra.add(new JumpInsnNode(Opcodes.GOTO, skip));
                        extra.add(new FieldInsnNode(Opcodes.GETSTATIC, RemappingUtils.getClassName("class_1937"), levelEnd, RemappingUtils.mapMethodDescriptor("Lnet/minecraft/class_5321;")));
                        extra.add(skip);
                        m.instructions.insertBefore(first, extra);
                        break;
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
        switch (mixinInfo.getName()) {
            case "EntityMixin":
            case "ServerPlayerMixin":
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
