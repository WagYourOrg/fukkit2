package xyz.wagyourtail.fukkit2.compat.fabricdatagen.transform;

import joptsimple.OptionSet;
import net.fabricmc.fabric.mixin.datagen.server.MainMixin;
import net.lenni0451.classtransform.annotations.CTransformer;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CReplaceArgs;

@CTransformer(MainMixin.class)
public class MainMixinTransform {

    @CReplaceArgs(value = "main([Ljava/lang/String;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
    private static void main(OptionSet options, CallbackInfo ci) {
        throw new AssertionError();
    }

}
