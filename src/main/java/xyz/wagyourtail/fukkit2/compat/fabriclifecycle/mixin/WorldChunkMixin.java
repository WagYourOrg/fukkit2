package xyz.wagyourtail.fukkit2.compat.fabriclifecycle.mixin;

import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;

@Mixin(LevelChunk.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/event/lifecycle/server/WorldChunkMixin")
public class WorldChunkMixin {
}
