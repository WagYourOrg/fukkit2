package xyz.wagyourtail.fukkit2.compat;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.mappings.AMapper;
import net.lenni0451.classtransform.mappings.MapperConfig;
import net.lenni0451.classtransform.transformer.HandlerPosition;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.classtransform.utils.tree.IClassProvider;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import xyz.wagyourtail.fukkit2.FukkitEarlyRiser;
import xyz.wagyourtail.fukkit2.mixinpatcher.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MixinTransformer {

    private static final IClassProvider classProvider = new BasicClassProvider(Knot.getLauncher().getTargetClassLoader());
    private static final TransformerManager transformerManager = new TransformerManager(classProvider, new FabricMapper(MapperConfig.create().fillSuperMappings(true).remapTransformer(true)));

    static {
        try {
            addMixinTransformers(transformerManager);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        forMods(transformerManager);
    }

    private static final Path DEBUG_DUMP_PATH = Paths.get("./classtransform.out");

    public static ClassNode transform(final String mixinClassName, final ClassNode mixin) {
        final byte[] newBytes = transformerManager.transform(mixinClassName, toBytes(mixin));
        if (newBytes == null) return mixin;
        if (Boolean.getBoolean("transform.debug")) {
            try {
                Path p = DEBUG_DUMP_PATH.resolve(mixinClassName.replace('.', '/') + ".class");
                Files.createDirectories(p.getParent());
                Files.write(p, newBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return fromBytes(newBytes);
    }

    private static byte[] toBytes(final ClassNode node) {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        node.accept(writer);
        return writer.toByteArray();
    }

    private static ClassNode fromBytes(final byte[] bytes) {
        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);
        return classNode;
    }

    private static void addMixinTransformers(TransformerManager transformer) throws IllegalAccessException {
        transformer.addCustomAnnotationHandler(new CAnotationRemover(), HandlerPosition.POST);
        transformer.addCustomAnnotationHandler(new CAnnotationAdder(), HandlerPosition.POST);
        transformer.addCustomAnnotationHandler(new CArgsReplacer(), HandlerPosition.POST);
        transformer.addCustomAnnotationHandler(new CMethodChanger(), HandlerPosition.POST);
        transformer.addCustomAnnotationHandler(new CUniqueAdder(), HandlerPosition.POST);
    }

    public static void forMods(TransformerManager transformer) {
        for (ModContainer allMod : FabricLoader.getInstance().getAllMods()) {
            switch (allMod.getMetadata().getId()) {
                case "fabric-data-generation-api-v1":
                    System.out.println("Fukkit2: Patching fabric-data-generation-api-v1");
                    addTransformerList(transformer, "fabric-data-generation-api-v1.classtransform");
                    break;
                case "fabric-registry-sync-v0":
                    System.out.println("Fukkit2: Patching fabric-registry-sync-v0");
                    addTransformerList(transformer, "fabric-registry-sync-v0.classtransform");
                    break;
                case "fabric-dimensions-v1":
                    System.out.println("Fukkit2: Patching fabric-dimensions-v1");
                    addTransformerList(transformer, "fabric-dimensions-v1.classtransform");
                    break;
                case "fabric-entity-events-v1":
                    System.out.println("Fukkit2: Patching fabric-entity-events-v1");
                    addTransformerList(transformer, "fabric-entity-events-v1.classtransform");
                    break;
                case "fabric-lifecycle-events-v1":
                    System.out.println("Fukkit2: Patching fabric-lifecycle-events-v1");
                    addTransformerList(transformer, "fabric-lifecycle-events-v1.classtransform");
                    break;
                case "fabric-transfer-api-v1":
                    System.out.println("Fukkit2: Patching fabric-transfer-api-v1");
                    addTransformerList(transformer, "fabric-transfer-api-v1.classtransform");
                    break;
                case "fabric-item-api-v1":
                    System.out.println("Fukkit2: Patching fabric-item-api-v1");
                    addTransformerList(transformer, "fabric-item-api-v1.classtransform");
                    break;
            }
        }
    }

    private static void addTransformerList(TransformerManager transformer, String fileName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(FukkitEarlyRiser.class.getResourceAsStream("/" + fileName))))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("#")) line = line.substring(0, line.indexOf('#'));
                line = line.trim();
                if (line.isEmpty()) continue;
                transformer.addTransformer(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void install() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        ByteBuddyAgent.install();

        TransformerManager transformer = new TransformerManager(new BasicClassProvider());
        Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();
        transformer.addTransformer("xyz.wagyourtail.fukkit2.compat.transform.MixinInfoTransformer");
        transformer.hookInstrumentation(instrumentation);

        findAlreadyLoaded();
    }

    private static void findAlreadyLoaded() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
            List<IMixinConfig> configs = goFish();
            List<IMixinInfo> infos = new ArrayList<>();
            for (IMixinConfig config : configs) {
                infos.addAll(spearFish(config));
            }
            for (IMixinInfo info : infos) {
                restitch(info);
            }
        }

    private static List<IMixinConfig> goFish() throws IllegalAccessException {
        Object transformer = MixinEnvironment.getCurrentEnvironment().getActiveTransformer();
        if (transformer == null) throw new IllegalStateException("No active transformer");

        Object processor = FieldUtils.readDeclaredField(transformer, "processor", true);
        assert processor != null;

        Object configs = FieldUtils.readDeclaredField(processor, "configs", true);
        assert configs != null;
        Object pendingConfigs = FieldUtils.readDeclaredField(processor, "pendingConfigs", true);
        assert pendingConfigs != null;

        List<IMixinConfig> result = new ArrayList<>();
        result.addAll((List) configs);
        result.addAll((List) pendingConfigs);

        return result;
    }

    private static List<IMixinInfo> spearFish(IMixinConfig config) throws IllegalAccessException {
        Object mixins = FieldUtils.readDeclaredField(config, "mixins", true);
        assert mixins != null;

        return (List) mixins;
    }

    private static void restitch(IMixinInfo info) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        try {
            ClassNode node = info.getClassNode(0);
            node = transform(info.getClassName(), node);
            Object pendingState = FieldUtils.readDeclaredField(info, "pendingState", true);
            Class<?> pendingStateType = FieldUtils.getDeclaredField(info.getClass(), "pendingState", true).getType();

            Map<String, ClassInfo> cache = (Map) FieldUtils.getDeclaredField(ClassInfo.class, "cache", true).get(null);
            cache.remove(node.name);

            Constructor<?> constr = pendingStateType.getDeclaredConstructor(info.getClass(), ClassNode.class);
            constr.setAccessible(true);
            Object newPendingState = constr.newInstance(info, node);

            FieldUtils.writeDeclaredField(info, "pendingState", newPendingState, true);
            Object newInfo = MethodUtils.invokeMethod(newPendingState, true, "getClassInfo");
            FieldUtils.writeDeclaredField(info, "info", newInfo, true);
            Object newPriority = MethodUtils.invokeMethod(info, true, "readPriority", node);
            FieldUtils.writeDeclaredField(info, "priority", newPriority, true);
            Object validationClassNode = MethodUtils.invokeMethod(newPendingState, true, "getValidationClassNode");
            Object newVirtual = MethodUtils.invokeMethod(info, true, "readPseudo", validationClassNode);
            FieldUtils.writeDeclaredField(info, "virtual", newVirtual, true);
            Object newDeclaredTargets = MethodUtils.invokeMethod(
                info,
                true,
                "readDeclaredTargets",
                validationClassNode,
                false
            );
            FieldUtils.writeDeclaredField(info, "declaredTargets", newDeclaredTargets, true);
            if (pendingState == null) {
                MethodUtils.invokeMethod(info, true, "validate");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public static class FabricMapper extends AMapper {
        private static final MappingResolver RESOLVER = FabricLoader.getInstance().getMappingResolver();

        public FabricMapper(MapperConfig config) {
            super(config);
        }

        @Override
        protected void init() throws Throwable {
            if (RESOLVER.getCurrentRuntimeNamespace().equals("intermediary")) return;
            //TODO
        }
    }
}

