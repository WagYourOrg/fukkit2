package xyz.wagyourtail.fukkit2.compat.fabricentityevent.transform;

import com.mojang.datafixers.util.Either;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;

import java.util.List;

@CTransformer(name = "net.fabricmc.fabric.mixin.entity.event.ServerPlayerEntityMixin")
public class ServerPlayerMixinTransform {

    @CRemoveAnotation(value = "redirectSleepDirection(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/properties/Property;Lnet/minecraft/core/BlockPos;)Ljava/lang/Comparable;", annotation = Redirect.class)
    private Comparable<?> redirectSleepDirection(BlockState state, Property<?> property, BlockPos pos) {
        throw new AssertionError();
    }

    @CRemoveAnotation(value = "onTrySleepDirectionCheck(Lnet/minecraft/core/BlockPos;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;Lnet/minecraft/core/Direction;)V", annotation = Inject.class)
    private void onTrySleepDirectionCheck(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info, @Nullable Direction sleepingDirection) {
        throw new AssertionError();
    }

    @CRemoveAnotation(value = "onSetSpawnPoint(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZ)V", annotation = Redirect.class)
    private void onSetSpawnPoint(ServerPlayer player, ResourceKey<Level> dimension, BlockPos pos, float angle, boolean spawnPointSet, boolean sendMessage) {
        throw new AssertionError();
    }

    @CRemoveAnotation(value = "hasNoMonstersNearby(Ljava/util/List;Lnet/minecraft/core/BlockPos;)Z", annotation = Redirect.class)
    private boolean hasNoMonstersNearby(List<Monster> monsters, BlockPos pos) {
        throw new AssertionError();
    }

    @CRemoveAnotation(value = "redirectDaySleepCheck(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z", annotation = Redirect.class)
    private boolean redirectDaySleepCheck(Level world, BlockPos pos) {
        throw new AssertionError();
    }

}