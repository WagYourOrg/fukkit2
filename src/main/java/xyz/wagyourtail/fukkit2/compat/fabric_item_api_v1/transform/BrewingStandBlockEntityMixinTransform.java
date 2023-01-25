package xyz.wagyourtail.fukkit2.compat.fabric_item_api_v1.transform;

import net.fabricmc.fabric.mixin.item.BrewingStandBlockEntityMixin;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;

@CTransformer(BrewingStandBlockEntityMixin.class)
public class BrewingStandBlockEntityMixinTransform {

    @Redirect(method = "craft", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;decrement(I)V"))
    private static void decrement(ItemStack stack, int count) {
        stack.shrink(count);
        captureItemStack(null, null, null, null, stack);
    }

    @CRemoveAnotation(value = "captureItemStack(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;Lnet/minecraft/world/item/ItemStack;)V", annotation = Inject.class)
    private static void nameDoesntMatter() {
        throw new AssertionError();
    }

    @CShadow
    private static void captureItemStack(Level world, BlockPos pos, NonNullList<ItemStack> slots, CallbackInfo ci, ItemStack itemStack) {
        throw new AssertionError();
    }
}
