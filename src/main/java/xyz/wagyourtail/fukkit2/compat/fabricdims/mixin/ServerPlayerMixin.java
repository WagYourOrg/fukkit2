package xyz.wagyourtail.fukkit2.compat.fabricdims.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.portal.PortalInfo;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;

@Mixin({ServerPlayer.class})
public class ServerPlayerMixin {
    @Unique
    @Nullable
    protected PortalInfo customTeleportTarget;
    @Redirect(method = {"changeDimension(Lnet/minecraft/server/level/ServerLevel;Lorg/bukkit/event/player/PlayerTeleportEvent$TeleportCause;)Lnet/minecraft/world/entity/Entity;"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/LevelStem;END:Lnet/minecraft/resources/ResourceKey;", opcode = Opcodes.GETSTATIC), remap = false)
    private ResourceKey<LevelStem> stopEndSpecificBehavior() {
        return this.customTeleportTarget != null ? null : LevelStem.END;
    }
}
