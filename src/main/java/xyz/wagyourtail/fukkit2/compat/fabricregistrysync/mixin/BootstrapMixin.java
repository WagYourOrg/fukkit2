package xyz.wagyourtail.fukkit2.compat.fabricregistrysync.mixin;

import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;

@Mixin(Bootstrap.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/registry/sync/BootstrapMixin")
public class BootstrapMixin {

}
