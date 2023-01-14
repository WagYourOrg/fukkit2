package xyz.wagyourtail.fukkit2.compat.fabricentityevent.transform;

import net.lenni0451.classtransform.annotations.CTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CChangeMethods;

import java.util.Optional;

@CTransformer(name = "net.fabricmc.fabric.mixin.entity.event.LivingEntityMixin")
public class LivingEntityMixinTransform {

    @CChangeMethods(
        value = "onIsSleepingInBed(Lnet/minecraft/core/BlockPos;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;)V",
        target = "lambda$checkBedExists$8"
    )
    private void onIsSleepingInBed(BlockPos sleepingPos, CallbackInfoReturnable<Boolean> info) {
        throw new AssertionError();
    }

    @CChangeMethods(
        value = "modifyBedForOccupiedState(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
        target = "lambda$stopSleeping$10(Lnet/minecraft/core/BlockPos;)V"
    )
    private BlockState modifyBedForOccupiedState(BlockState state, BlockPos sleepingPos) {
        throw new AssertionError();
    }

    @CChangeMethods(
        value = "setOccupiedState(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z",
        target = "lambda$stopSleeping$10(Lnet/minecraft/core/BlockPos;)V"
    )
    private boolean setOccupiedState(Level world, BlockPos pos, BlockState state, int flags) {
        throw new AssertionError();
    }

    @CChangeMethods(
        value = "modifyWakeUpPosition(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/CollisionGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;F)Ljava/util/Optional;",
        target = "lambda$stopSleeping$10(Lnet/minecraft/core/BlockPos;)V"
    )
    private Optional<Vec3> modifyWakeUpPosition(EntityType<?> type, CollisionGetter world, BlockPos pos, Direction direction, float yaw) {
        throw new AssertionError();
    }

}
