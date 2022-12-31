package xyz.wagyourtail.fukkit2.compat.fabricentityevent;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixinPlugin;
import xyz.wagyourtail.fukkit2.util.MixinUtils;
import xyz.wagyourtail.fukkit2.util.RemappingUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class EntityEventMixinPlugin extends InterceptingMixinPlugin {

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "ServerPlayerMixin":
                String trySleep = RemappingUtils.getMethodName("class_1657", "method_7269", "(Lnet/minecraft/class_2338;)Lcom/mojang/datafixers/util/Either;");
                ClassNode thisMixin = MixinUtils.Mixin.create(mixinInfo).getClassNode();

                MethodVisitor v = targetClass.visitMethod(Opcodes.ACC_PUBLIC, trySleep, RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_2338;)Lcom/mojang/datafixers/util/Either;"), null, null);
                v.visitCode();
                v.visitEnd();
                // get the method as a method node
                for (MethodNode method : targetClass.methods) {
                    if (method.name.equals(trySleep) && method.desc.equals(RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_2338;)Lcom/mojang/datafixers/util/Either;"))) {
                        // add the code to the method
                        method.instructions.clear();
                        // copy from the mixin's class node
                        for (MethodNode mixinMethod : thisMixin.methods) {
                            if (mixinMethod.name.equals("trySleepYeet") && mixinMethod.desc.equals(RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_2338;)Lcom/mojang/datafixers/util/Either;"))) {

                                // cursed
                                for (Field f : MethodNode.class.getFields()) {
                                    if (Modifier.isFinal(f.getModifiers())) continue;
                                    try {
                                        f.set(method, f.get(mixinMethod));
                                    } catch (IllegalAccessException e) {
                                        throw new RuntimeException(e);
                                    }
                                }

                                method.name = trySleep;

                                break;
                            }
                        }
                        break;
                    }
                }

//                 dump
//                try {
//                    ClassWriter cw = new ClassWriter(0);
//                    targetClass.accept(cw);
//                    Files.write(Paths.get("ServerPlayer.class"), cw.toByteArray());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                break;
            case "LivingEntityMixin":
                // rename method
                String isDeadOrDying = RemappingUtils.getMethodName("class_1309", "method_29504", "()Z");
                String hurt = RemappingUtils.getMethodName("class_1297", "method_5643", "(Lnet/minecraft/class_1282;F)Z");
                String hurtDesc = RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_1282;F)Z");
                for (MethodNode method : targetClass.methods) {
                    switch (method.name) {
                        case "lambda$checkBedExists$8":
                            method.name = "method_18405";
                            break;
                        case "lambda$stopSleeping$10":
                            method.name = "method_18404";
                            break;
                    }
                    if (method.name.equals(hurt) && method.desc.equals(hurtDesc)) {
                        // insert isDeadOrDying in skip at front
                        InsnList list = new InsnList();
                        LabelNode skip = new LabelNode();
                        list.add(new JumpInsnNode(Opcodes.GOTO, skip));
                        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, targetClassName.replace(".", "/"), isDeadOrDying, "()Z", false));
                        list.add(skip);
                        method.instructions.insertBefore(method.instructions.getFirst(), list);
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
            case "ServerPlayerMixin":
//                String trySleep = RemappingUtils.getMethodName("class_1657", "method_7269", "(Lnet/minecraft/class_2338;)Lcom/mojang/datafixers/util/Either;");
//                targetClass.methods.removeIf(m -> (m.name.equals(trySleep) || m.name.equals("trySleepYeet")) && m.desc.equals(RemappingUtils.mapMethodDescriptor("(Lnet/minecraft/class_2338;)Lcom/mojang/datafixers/util/Either;")));
                break;
            case "LivingEntityMixin":
                // change back
                for (MethodNode method : targetClass.methods) {
                    switch (method.name) {
                        case "method_18405":
                            method.name = "lambda$checkBedExists$8";
                            break;
                        case "method_18404":
                            method.name = "lambda$stopSleeping$10";
                            break;
                    }
                }
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
