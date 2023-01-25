package xyz.wagyourtail.fukkit2.compat.fabric_screen_handler_api_v1.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;

@Mixin(ServerPlayer.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/screenhandler/ServerPlayerEntityMixin")
public class ServerPlayerMixin {
}
