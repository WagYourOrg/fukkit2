package xyz.wagyourtail.fukkit2.compat.fabric_dimensions_v1.transform;

import net.fabricmc.fabric.mixin.dimension.EntityMixin;
import net.lenni0451.classtransform.annotations.CTransformer;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;

@CTransformer(EntityMixin.class)
public class EntityMixinTransform {

    @CRemoveAnotation(value = "stopEndSpecificBehavior()Lnet/minecraft/resources/ResourceKey;", annotation = Redirect.class)
    private static void stopEndSpecificBehavior() {
        throw new AssertionError();
    }

}
