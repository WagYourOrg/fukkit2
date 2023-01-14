package xyz.wagyourtail.fukkit2.compat.fabriclifecycle.transform;

import net.fabricmc.fabric.mixin.event.lifecycle.LivingEntityMixin;
import net.lenni0451.classtransform.annotations.CTransformer;

@CTransformer(LivingEntityMixin.class)
public class LivingEntityMixinTransform {
//    private ThreadLocal<EquipmentSlot> currentSlot = new ThreadLocal<>();
//
//    @Redirect(method = "collectEquipmentChanges()Ljava/util/Map;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"))
//    public ItemStack getNewItem(LivingEntity self, EquipmentSlot slot) {
//        return self.getItemBySlot(slot);
//    }
//
//    @Redirect(method = "collectEquipmentChanges()Ljava/util/Map;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;equipmentHasChanged(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
//    public boolean onEquipmentChanged(LivingEntity self, ItemStack stack1, ItemStack stack2) {
//        if (self.equipmentHasChanged(stack1, stack2)) {
//            EquipmentSlot slot = currentSlot.get();
//            assert slot != null;
//            getEquipmentChanges(null, null, null, 0, 0, slot, stack1, stack2);
//            return true;
//        }
//        return false;
//    }
//
//    @CRemoveAnotation(
//        value = "getEquipmentChanges(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;Ljava/util/Map;[Lnet/minecraft/world/entity/EquipmentSlot;IILnet/minecraft/world/entity/EquipmentSlot;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
//        annotation = Inject.class
//    )
//    private void thisNameReallyDoesntMatter(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, Map<EquipmentSlot, ItemStack> changes, EquipmentSlot[] slots, int slotsSize, int slotIndex, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack) {
//        throw new AssertionError();
//    }
//
//    @CShadow
//    private void getEquipmentChanges(CallbackInfoReturnable<@Nullable Map<EquipmentSlot, ItemStack>> cir, Map<EquipmentSlot, ItemStack> changes, EquipmentSlot[] slots, int slotsSize, int slotIndex, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack) {}

}
