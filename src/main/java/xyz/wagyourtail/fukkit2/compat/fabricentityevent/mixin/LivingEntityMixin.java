package xyz.wagyourtail.fukkit2.compat.fabricentityevent.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;

@Mixin(LivingEntity.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/entity/event/LivingEntityMixin")
public class LivingEntityMixin {

}
