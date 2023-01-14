package xyz.wagyourtail.fukkit2.compat.fabricregistrysync.transform;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.fabricmc.fabric.mixin.registry.sync.SimpleRegistryMixin;
import net.lenni0451.classtransform.annotations.CShadow;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CASM;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.fukkit2.compat.fabricregistrysync.ReferenceMapWrapper;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CAddAnnotation;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CRemoveAnotation;
import xyz.wagyourtail.fukkit2.mixinpatcher.annotations.CUnique;

import java.util.Map;

@CTransformer(SimpleRegistryMixin.class)
public class SimpleRegistryMixinTransform {


    @Shadow
    @CUnique
    private Reference2IntOpenHashMap<Object> e;

    @CShadow
    @Unique
    private Object2IntMap<Object> field_26683;

    @CRemoveAnotation(value = "field_26683:Lit/unimi/dsi/fastutil/objects/Object2IntMap;", annotation = Shadow.class)
    private Object2IntMap<Object> names_dont;


    @CAddAnnotation(value = "field_26683:Lit/unimi/dsi/fastutil/objects/Object2IntMap;", annotation = Unique.class)
    private Object matter;


    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.field_26683 = new ReferenceMapWrapper(this.e);
    }

}
