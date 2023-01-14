package xyz.wagyourtail.fukkit2.compat.fabricregistrysync.mixin;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.MappedRegistry;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.wagyourtail.fukkit2.compat.InterceptingMixin;

import java.util.Map;

@Mixin(MappedRegistry.class)
@InterceptingMixin("net/fabricmc/fabric/mixin/registry/sync/SimpleRegistryMixin")
public class MappedRegistryMixin {

    // sneaky shadow xd
    @Unique
    private Reference2IntOpenHashMap<Object> e;

    @Unique
    private Object2IntMap<Object> field_26683;


}
