package xyz.wagyourtail.fukkit2.compat.fabricregistrysync.mixin;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {

    @Inject(method = "bootStrap(Ljava/lang/Runnable;)V", remap = false, cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/core/registries/BuiltInRegistries;freeze()V"))
    private static void beforeFreeze(Runnable runnable, CallbackInfo ci) {
        ci.cancel();
    }
}
