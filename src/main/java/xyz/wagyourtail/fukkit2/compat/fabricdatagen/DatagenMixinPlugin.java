package xyz.wagyourtail.fukkit2.compat.fabricdatagen;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixinPlugin;
import xyz.wagyourtail.fukkit2.util.RemappingUtils;

public class DatagenMixinPlugin extends InterceptingMixinPlugin {

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "MainMixin":
                MethodVisitor v = targetClass.visitMethod(Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC, "main", "([Ljava/lang/String;)V", null, null);
                // public static void main(String[] args) {
                // new class_3807();
                // }
                v.visitCode();
                v.visitTypeInsn(Opcodes.NEW, RemappingUtils.getClassName("class_3807"));
                v.visitMaxs(0, 0);
                v.visitEnd();
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.preApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        switch (mixinInfo.getName()) {
            case "MainMixin":
                targetClass.methods.removeIf(m -> m.name.equals("main") && m.desc.equals("([Ljava/lang/String;)V"));
                break;
            default:
                System.err.println("Unknown mixin: " + mixinInfo.getName());
        }
        super.postApply(targetClassName, targetClass, mixinClassName, mixinInfo);
    }
}
