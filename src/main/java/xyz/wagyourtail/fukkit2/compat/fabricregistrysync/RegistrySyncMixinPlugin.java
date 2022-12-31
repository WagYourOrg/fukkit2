package xyz.wagyourtail.fukkit2.compat.fabricregistrysync;

import net.fabricmc.mapping.util.AsmRemapperFactory;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import org.spongepowered.asm.util.Annotations;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixinPlugin;
import xyz.wagyourtail.fukkit2.util.MixinUtils;
import xyz.wagyourtail.fukkit2.util.RemappingUtils;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;

public class RegistrySyncMixinPlugin extends InterceptingMixinPlugin {

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "BootstrapMixin":
                String initialize = RemappingUtils.getMethodName("class_2966", "method_12851", "()V");
                for (MethodNode m : targetClass.methods) {
                    if (m.name.equals(initialize) && m.desc.equals("()V")) {
                        for (AbstractInsnNode insn : m.instructions) {
                            if (insn.getType() == AbstractInsnNode.METHOD_INSN &&
                                    insn.getOpcode() == Opcodes.INVOKESTATIC &&
                                    ((MethodInsnNode) insn).owner.equals(RemappingUtils.getClassName("class_7923")) &&
                                    ((MethodInsnNode) insn).name.equals("bootStrap") &&
                                    ((MethodInsnNode) insn).desc.equals("(Ljava/lang/Runnable;)V")) {

                                LabelNode skip = new LabelNode();
                                InsnList extra = new InsnList();
                                extra.add(new JumpInsnNode(Opcodes.GOTO, skip));
                                extra.add(new MethodInsnNode(Opcodes.INVOKESTATIC, RemappingUtils.getClassName("class_7923"), RemappingUtils.getMethodName("class_7923", "method_47476", "()V"), "()V", false));
                                extra.add(skip);

                                m.instructions.insertBefore(insn, extra);
                                break;
                            }
                        }
                    }
                }
                break;
            case "BuiltInRegistriesMixin":
                break;
            case "MappedRegistryMixin":
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "BootstrapMixin":
                break;
            case "BuiltInRegistriesMixin":
                break;
            case "MappedRegistryMixin":
                // replace references to Object2IntMap toId with Reference2IntOpenHashMap e;
                String toId = RemappingUtils.mapFieldName("class_2370", "field_26683", "Lit/unimi/dsi/fastutil/objects/Object2IntMap;");
                ClassNode thisMixin = MixinUtils.Mixin.create(mixinInfo).getClassNode();
                AnnotationNode interception = Annotations.getInvisible(thisMixin, InterceptingMixin.class);
                MixinUtils.Mixin interceptionMixin = findMixin(targetClassName, Annotations.getValue(interception, "value", true));
                outer:
                for (ClassInfo.Method method : interceptionMixin.getMethods()) {
                    for (MethodNode m : targetClass.methods) {
                        if (m.name.equals(method.getName()) && m.desc.equals(method.getDesc())) {
                            System.out.println("Replacing toId in " + method.getName() + method.getDesc());
                            for (AbstractInsnNode insn : m.instructions) {
                                // replace with toIdPatched
                                if (insn.getType() == AbstractInsnNode.FIELD_INSN &&
                                        insn.getOpcode() == Opcodes.GETFIELD &&
                                        ((FieldInsnNode) insn).owner.equals(RemappingUtils.getClassName("class_2370")) &&
                                        ((FieldInsnNode) insn).name.equals(toId) &&
                                        ((FieldInsnNode) insn).desc.equals("Lit/unimi/dsi/fastutil/objects/Object2IntMap;")) {
                                    ((FieldInsnNode) insn).name = "toIdPatched";
                                    ((FieldInsnNode) insn).desc = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;";
                                }
                            }
                            continue outer;
                        }
                    }
                }
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    private int getArgs(String desc) {
        int args = 0;
        for (int i = 1; i < desc.length(); i++) {
            char c = desc.charAt(i);
            if (c == ')') {
                break;
            }
            if (c == 'L') {
                args++;
                while (c != ';') {
                    i++;
                    c = desc.charAt(i);
                }
            } else if (c == 'D' || c == 'J') {
                args += 2;
            } else if (c != '(') {
                args++;
            }
        }
        System.err.print(desc);
        System.err.println(" Args: " + args);
        return args;
    }
}
