package xyz.wagyourtail.fukkit2.compat.fabric_screen_handler_api_v1;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixinPlugin;
import xyz.wagyourtail.fukkit2.util.RemappingUtils;

public class ScreenHandleMixinPlugin extends InterceptingMixinPlugin {

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "ServerPlayerMixin":
                String containerMenu = RemappingUtils.mapFieldName("class_1657", "field_7512", "Lnet/minecraft/class_1703;");
                String inventoryMenu = RemappingUtils.mapFieldName("class_1657", "field_7498", "Lnet/minecraft/class_1723;");
                String openMenu = RemappingUtils.getMethodName("class_1657", "method_17355", "(Lnet/minecraft/class_3908;)Ljava/util/OptionalInt;");
                outer: for (MethodNode m : targetClass.methods) {
                    if (m.name.equals(openMenu) && m.desc.equals(RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_3908;)Ljava/util/OptionalInt;"))) {
                        System.out.println("Found method: " + m.name + m.desc);
                        for (AbstractInsnNode insn : m.instructions) {
                            if (insn.getType() == AbstractInsnNode.FIELD_INSN &&
                                insn.getOpcode() == Opcodes.PUTFIELD &&
                                ((FieldInsnNode) insn).name.equals(containerMenu)) {

                                InsnList extra = new InsnList();
                                // if (containerMenu != inventoryMenu) {
                                //     this.closeContainer()
                                // }
                                extra.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                extra.add(new FieldInsnNode(Opcodes.GETFIELD, RemappingUtils.getClassName("class_1657"), containerMenu, RemappingUtils.mapMethodDescriptor("Lnet/minecraft/class_1703;")));
                                extra.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                extra.add(new FieldInsnNode(Opcodes.GETFIELD, RemappingUtils.getClassName("class_1657"), inventoryMenu, RemappingUtils.mapMethodDescriptor("Lnet/minecraft/class_1723;")));
                                LabelNode ifeq = new LabelNode();
                                extra.add(new JumpInsnNode(Opcodes.IF_ACMPEQ, ifeq));
                                extra.add(new VarInsnNode(Opcodes.ALOAD, 0));
                                extra.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, RemappingUtils.getClassName("class_3222"), RemappingUtils.getMethodName("class_1657", "method_7346", "()V"), RemappingUtils.mapMethodDescriptor("()V"), false));
                                extra.add(ifeq);
                                m.instructions.insertBefore(insn, extra);
                                break outer;
                            }
                        }
                    }
                }
//                 dump
//                try {
//                    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//                    targetClass.accept(cw);
//                    Files.write(Paths.get("ServerPlayer.class"), cw.toByteArray());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "ServerPlayerMixin":
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
