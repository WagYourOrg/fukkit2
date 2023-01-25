package xyz.wagyourtail.fukkit2.compat.fabric_entity_event_v1.mixin;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.fabricmc.fabric.api.entity.event.v1.EntitySleepEvents;
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
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;
import xyz.wagyourtail.fukkit2.compat.Shim;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

@Mixin(ServerPlayer.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/entity/event/ServerPlayerEntityMixin")
public abstract class ServerPlayerMixin extends Player {

    @Shadow public abstract void setRespawnPosition(ResourceKey<Level> resourceKey, @Nullable BlockPos blockPos, float f, boolean bl, boolean bl2);

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Shim
    private Comparable<?> redirectSleepDirection(BlockState state, Property<?> property, BlockPos pos) {
        throw new AssertionError();
    }

    @Shim
    private void onTrySleepDirectionCheck(BlockPos pos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info, @Nullable Direction sleepingDirection) {
        throw new AssertionError();
    }

    @Shim
    private void onSetSpawnPoint(ServerPlayer player, ResourceKey<Level> dimension, BlockPos pos, float angle, boolean spawnPointSet, boolean sendMessage) {
        throw new AssertionError();
    }

    @Shim
    private boolean hasNoMonstersNearby(List<Monster> monsters, BlockPos pos) {
        throw new AssertionError();
    }

    @Shim
    private boolean redirectDaySleepCheck(Level world, BlockPos pos) {
        throw new AssertionError();
    }

    @Unique
    ThreadLocal<Boolean> cancelSleep = ThreadLocal.withInitial(() -> false);

    @Redirect(method = "startSleepInBed(Lnet/minecraft/core/BlockPos;Z)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;"))
    private Comparable<?> redirectSleepDirection2(BlockState state, Property<?> property, BlockPos pos) {
        Direction d = (Direction) redirectSleepDirection(state, property, pos);
        cancelSleep.set(d == null);
        return d;
    }

    @Inject(method = "startSleepInBed(Lnet/minecraft/core/BlockPos;Z)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getValue(Lnet/minecraft/world/level/block/state/properties/Property;)Ljava/lang/Comparable;", shift = At.Shift.AFTER), cancellable = true)
    private void onTrySleepDirectionCheck2(BlockPos pos, boolean force, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> info) {
        if (cancelSleep.get()) {
            info.setReturnValue(Either.left(BedSleepingProblem.NOT_POSSIBLE_HERE));
        }
    }

    @Redirect(method = "getBedResult(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/core/BlockPos;FZZLcom/destroystokyo/paper/event/player/PlayerSetSpawnEvent$Cause;)Z"))
    private boolean onSetSpawnPoint2(ServerPlayer player, ResourceKey<Level> dimension, BlockPos pos, float angle, boolean spawnPointSet, boolean sendMessage, PlayerSetSpawnEvent.Cause cause) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if (EntitySleepEvents.ALLOW_SETTING_SPAWN.invoker().allowSettingSpawn(player, pos)) {
            MethodUtils.invokeMethod(this, true, "setRespawnPosition", dimension, pos, angle, spawnPointSet, sendMessage, cause);
        }
        return false;
    }

    @Redirect(method = "getBedResult(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", remap = false))
    private boolean hasNoMonstersNearby2(List<Monster> monsters, BlockPos pos) {
        return hasNoMonstersNearby(monsters, pos);
    }

    @Redirect(method = "getBedResult(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;isDay()Z"))
    private boolean redirectDaySleepCheck2(Level world, BlockPos pos) {
        return redirectDaySleepCheck(world, pos);
    }
}
