package xyz.wagyourtail.fukkit2.compat.fabricregistrysync;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ReferenceMapWrapper implements Object2IntMap<Object> {

    private final Reference2IntOpenHashMap e;

    public ReferenceMapWrapper(Reference2IntOpenHashMap e) {
        this.e = e;
    }


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
}
