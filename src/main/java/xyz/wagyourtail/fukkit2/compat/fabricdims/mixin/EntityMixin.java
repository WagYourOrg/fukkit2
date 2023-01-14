package xyz.wagyourtail.fukkit2.compat.fabricdims.mixin;

import net.minecraft.resources.ResourceKey;
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

@Mixin({Entity.class})
public class EntityMixin {
    @Unique
    @Nullable
    protected PortalInfo customTeleportTarget;
    @Redirect(method = {"teleportTo(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/PositionImpl;)Lnet/minecraft/world/entity/Entity;"}, at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/dimension/LevelStem;END:Lnet/minecraft/resources/ResourceKey;", opcode = Opcodes.GETSTATIC), remap = false)
    private ResourceKey<LevelStem> stopEndSpecificBehavior() {
        return this.customTeleportTarget != null ? null : LevelStem.END;
    }
}
