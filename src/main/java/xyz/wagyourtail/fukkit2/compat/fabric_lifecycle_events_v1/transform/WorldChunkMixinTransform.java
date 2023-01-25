package xyz.wagyourtail.fukkit2.compat.fabric_lifecycle_events_v1.transform;

import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;

@CTransformer(name = "net.fabricmc.fabric.mixin.event.lifecycle.server.WorldChunkMixin")
public class WorldChunkMixinTransform {

    @CRemoveAnotation(value = "onRemoveBlockEntity(Lnet/minecraft/world/level/block/entity/BlockEntity;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", annotation = Inject.class)
    private static void nameDoesntMatter() {
        throw new AssertionError();
    }

    @Redirect(method = "setBlockEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;markRemoved()V"))
    private void markRemoved(BlockEntity blockEntity) {
        blockEntity.setRemoved();
        onRemoveBlockEntity(null, null, null, blockEntity);
    }

    @CShadow
    private void onRemoveBlockEntity(BlockEntity blockEntity, CallbackInfo info, BlockPos blockPos, @Nullable BlockEntity removedBlockEntity) {
        throw new AssertionError();
    }
}
