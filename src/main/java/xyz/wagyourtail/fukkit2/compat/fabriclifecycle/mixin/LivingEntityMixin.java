package xyz.wagyourtail.fukkit2.compat.fabriclifecycle.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;
import xyz.wagyourtail.fukkit2.compat.Shim;

import java.util.Map;

@Mixin(LivingEntity.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/event/lifecycle/LivingEntityMixin")
public class LivingEntityMixin {
    @Shim
    private void getEquipmentChanges(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, Map map, EquipmentSlot[] var2, int var3, int var4, EquipmentSlot equipmentSlot, ItemStack itemStack, ItemStack itemStack2) {
        throw new AssertionError();
    }

    private void getEquipmentChanges(CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir, Map map, EquipmentSlot[] var2, int var3, EquipmentSlot equipmentSlot, ItemStack itemStack) {
        throw new AssertionError();
    }

//    @Inject(method = "collectEquipmentChanges", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), locals = LocalCapture.CAPTURE_FAILHARD)
//    private void getEquipmentChanges(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, Map<EquipmentSlot, ItemStack> changes, EquipmentSlot[] slots, int slotsSize, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack) {
//        ServerEntityEvents.EQUIPMENT_CHANGE.invoker().onChange((LivingEntity) (Object) this, equipmentSlot, previousStack, currentStack);
//    }
}
