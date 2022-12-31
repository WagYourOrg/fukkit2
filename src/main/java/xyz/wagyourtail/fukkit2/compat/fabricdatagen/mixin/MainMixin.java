package xyz.wagyourtail.fukkit2.compat.fabricdatagen.mixin;

import joptsimple.OptionSet;
import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;
import xyz.wagyourtail.fukkit2.compat.Shim;

@Mixin(value = Main.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/datagen/server/MainMixin")
public class MainMixin {

    @Inject(method = "main(Ljoptsimple/OptionSet;)V", at = @At(value = "NEW", target = "net/minecraft/server/dedicated/DedicatedServerSettings"), cancellable = true, remap = false)
    private static void main(OptionSet options, CallbackInfo ci) {
        main(new String[] {}, ci);
    }

    @Shim
    private static void main(String[] args, CallbackInfo ci) {
        throw new AssertionError();
    }

//    public static void main(String[] args) {
//        new net.minecraft.server.dedicated.DedicatedServerProperties(new Properties());
//    }
}
