package xyz.wagyourtail.fukkit2.compat.fabric_registry_sync_v0.transform;

import net.fabricmc.fabric.mixin.registry.sync.BootstrapMixin;
import net.lenni0451.classtransform.annotations.CTransformer;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;

@CTransformer(BootstrapMixin.class)
public class BootstrapMixinTransform {
    @CRemoveAnotation(value = "initialize()V", annotation = Redirect.class)
    private static void initialize() {
        throw new AssertionError();
    }
}
