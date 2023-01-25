package xyz.wagyourtail.fukkit2.compat.fabric_transfer_api_v1.transform;

import net.fabricmc.fabric.mixin.transfer.AbstractFurnaceBlockEntityMixin;
import net.lenni0451.classtransform.InjectionCallback;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CInject;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CUnique;

import javax.annotation.Nullable;

@CTransformer(AbstractFurnaceBlockEntityMixin.class)
public class AbstractFurnaceBlockEntityMixinTransform {

    @CUnique
    @Shadow
    @Final
    public RecipeType<? extends AbstractCookingRecipe> recipeType;

    @CUnique
    @Shadow
    public double cookSpeedMultiplier;

    @CInject(method = "method_17029(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I", target = @CTarget("HEAD"))
    private static void onGetCookTime(Level world, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity, InjectionCallback callback) throws IllegalAccessException {
        RecipeType<? extends AbstractCookingRecipe> recipeType = (RecipeType<? extends AbstractCookingRecipe>) FieldUtils.readDeclaredField(abstractFurnaceBlockEntity.getClass(), "recipeType", true);
        double cookSpeedMultiplier = (double) FieldUtils.readDeclaredField(abstractFurnaceBlockEntity.getClass(), "cookSpeedMultiplier", true);
        getTotalCookTime(world, recipeType, abstractFurnaceBlockEntity, cookSpeedMultiplier);
    }

    @CRemoveAnotation(value = "method_17029(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;)I", annotation = Shadow.class)
    private static int doesntMatter(Level world, RecipeType<? extends AbstractCookingRecipe> recipeType, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity, double cookSpeedMultiplier) {
        throw new AssertionError();
    }

    @Shadow(aliases = "getTotalCookTime")
    public static int getTotalCookTime(
        @Nullable
        Level world, RecipeType<? extends AbstractCookingRecipe> recipeType, AbstractFurnaceBlockEntity furnace, double cookSpeedMultiplier
    ) {
        throw new AssertionError();
    }
}
