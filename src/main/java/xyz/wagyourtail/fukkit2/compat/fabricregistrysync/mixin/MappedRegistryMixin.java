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
@Pseudo
@InterceptingMixin("net/fabricmc/fabric/mixin/registry/sync/SimpleRegistryMixin")
public class MappedRegistryMixin {

    // sneaky shadow xd
    @Unique
    private Reference2IntOpenHashMap<Object> e;

    @Unique
    private Object2IntMap<Object> toIdPatched;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.toIdPatched = new Object2IntMap<>() {
            @Override
            public int size() {
                return e.size();
            }

            @Override
            public void defaultReturnValue(int i) {
                e.defaultReturnValue(i);
            }

            @Override
            public int defaultReturnValue() {
                return e.defaultReturnValue();
            }

            @Override
            public ObjectSet<Entry<Object>> object2IntEntrySet() {
                throw new UnsupportedOperationException();
            }

            @Override
            public ObjectSet<Object> keySet() {
                return new ObjectOpenHashSet<>(e.keySet());
            }

            @Override
            public IntCollection values() {
                return e.values();
            }

            @Override
            public boolean containsKey(Object o) {
                return e.containsKey(o);
            }

            @Override
            public boolean containsValue(int i) {
                return e.containsValue(i);
            }

            @Override
            public int getInt(Object o) {
                return e.getInt(o);
            }

            @Override
            public boolean isEmpty() {
                return e.isEmpty();
            }

            @Override
            public void putAll(@NotNull Map<?, ? extends Integer> m) {
                e.putAll(m);
            }
        };
    }


}
