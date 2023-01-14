package xyz.wagyourtail.fukkit2;

import com.chocohead.mm.api.ClassTinkerers;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.minecraft.MinecraftGameProvider;
import net.fabricmc.loader.impl.util.Arguments;
import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.format.CsrgReader;
import net.fabricmc.mappingio.format.Tiny2Writer;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.tinyremapper.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipOutputStream;

public abstract class AbstractBukkitPatcher {
    protected Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected Set<String> patchedClasses = new HashSet<>();

    protected final String mcVersion;
    protected final String patchVersion;

    protected final Path fukkitDir;
    protected final Path tempDir;

    protected Path remappedBukkit;
    protected Path remappedPlugins;

    private MemoryMappingTree mappings;

    protected String entrypoint;

    public AbstractBukkitPatcher(String mcVersion, String patchVersion, Path fukkitDir, Path tempDir) {
        this.mcVersion = mcVersion;
        this.patchVersion = patchVersion;
        this.fukkitDir = fukkitDir;
        this.tempDir = tempDir;
    }

    public void setEntrypoint() throws NoSuchFieldException, IllegalAccessException {
        if (this.entrypoint == null) return;
        Field entrypoint = MinecraftGameProvider.class.getDeclaredField("entrypoint");
        entrypoint.setAccessible(true);
        MinecraftGameProvider provider = (MinecraftGameProvider) FabricLoaderImpl.INSTANCE.getGameProvider();
        entrypoint.set(provider, this.entrypoint);
        Field arguments = MinecraftGameProvider.class.getDeclaredField("arguments");
        arguments.setAccessible(true);
        Arguments args = (Arguments) arguments.get(provider);
        args.addExtraArg("--plugins");
        args.addExtraArg(remappedPlugins.toAbsolutePath().toString());
        LOGGER.info("Set entrypoint to {}", this.entrypoint);
    }

    public abstract void patch() throws Exception;

    protected void patchRuntime() throws IOException {
        try (FileSystem fs = openZipFileSystem(remappedBukkit)) {
            Files.walkFileTree(fs.getPath(""), new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                };

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (file.toString().endsWith(".class")) {
                        if (FukkitEarlyRiser.class.getClassLoader().getResourceAsStream(file.toString()) != null) {
                            // idk why skipping this makes it work
                            if (file.toString().endsWith("EntityTypeTest.class")) return FileVisitResult.CONTINUE;
                            ClassTinkerers.addReplacement(file.toString().replace(".class", ""), (classNode) -> {
                                patchedClasses.add(classNode.name);

                                try (FileSystem fs = openZipFileSystem(remappedBukkit)) {
                                    var reader = new ClassReader(Files.readAllBytes(fs.getPath(file.toString())));
                                    var writer = new ClassNode();
                                    reader.accept(writer, 0);

                                    // cursed
                                    for (Field f : ClassNode.class.getFields()) {
                                        if (Modifier.isFinal(f.getModifiers())) continue;
                                        f.set(classNode, f.get(writer));
                                    }

                                    // dump to file
//                                    if (Boolean.getBoolean("fukkit2.dump")) {
//                                        var cw = new ClassWriter(0);
//                                        classNode.accept(cw);
//                                        var path = tempDir.resolve("dump").resolve(file.toString());
//                                        Files.createDirectories(path.getParent());
//                                        Files.write(path, cw.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
//                                    }
                                } catch (IOException | IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        } else {
                            ClassTinkerers.define(file.toString().replace(".class", ""), Files.readAllBytes(file));
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    protected void createServerExtra() throws IOException {
        Path extra = fukkitDir.resolve("server-extra-" + mcVersion + "-" + patchVersion + ".jar");
        if (Files.exists(extra)) {
            ClassTinkerers.addURL(extra.toUri().toURL());
            return;
        }
        try (FileSystem out = openZipFileSystem(extra, Map.of("create", true))) {
            try (FileSystem in = openZipFileSystem(remappedBukkit)) {
                Files.walkFileTree(in.getPath(""), new FileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.toString().endsWith(".class")) {
                            try (InputStream is = FukkitEarlyRiser.class.getClassLoader().getResourceAsStream(file.toString())) {
                                if (is == null) {
                                    LOGGER.debug("Copying {}", file);
                                    if (file.getNameCount() > 1)
                                        Files.createDirectories(out.getPath(file.toString()).getParent());
                                    Files.copy(file, out.getPath(file.toString()), StandardCopyOption.REPLACE_EXISTING);
                                }
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }
        ClassTinkerers.addURL(extra.toUri().toURL());
    }

    protected void addLibraries(Path libraryRoot, List<String> libraryPath) throws IOException {
        // get current classpath
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);

        // add libraries to classpath
        for (String library : libraryPath) {
            Path path = libraryRoot.resolve(library);
            try (FileSystem fs = openZipFileSystem(path)) {
                // find first class entry
                Optional<Path> p;
                try (Stream<Path> st = Files.walk(fs.getPath(""))) {
                    p = st.filter(e -> e.toString().endsWith(".class")).findFirst();
                }
                if (p.isPresent()) {
                    try (InputStream s = AbstractBukkitPatcher.class.getClassLoader().getResourceAsStream(p.get().toString())) {
                        if (s == null) {
                            ClassTinkerers.addURL(path.toUri().toURL());
                        }
                    }
                }
            }
        }
    }

    public CompletableFuture<Integer> runJarInSubprocess(
            Path jar,
            String[] args,
            String mainClass,
            Path workingDir,
            Map<String, String> env,
            boolean wait,
            List<String> jvmArgs) throws IOException {

        String javaHome = System.getProperty("java.home");
        Path javaBin = Paths.get(javaHome, "bin", "java");
        if (!Files.exists(javaBin)) {
            javaBin = Paths.get(javaHome, "bin", "java.exe");
            if (!Files.exists(javaBin)) {
                throw new RuntimeException("Could not find java executable " + javaBin);
            }
        }
        String[] processArgs;
        if (mainClass == null) {
            processArgs = new String[]{"-jar", jar.toString()};
        } else {
            processArgs = new String[]{"-cp", jar.toString(), mainClass};
        }
        List<String> a = new ArrayList<>();
        a.add(javaBin.toString());
        a.addAll(jvmArgs);
        a.addAll(Arrays.asList(processArgs));
        a.addAll(Arrays.asList(args));
        ProcessBuilder processBuilder = new ProcessBuilder(a);

        processBuilder.directory(workingDir.toFile());
        processBuilder.environment().putAll(env);

        LOGGER.info("Running: " + String.join(" ", processBuilder.command()));
        Process process = processBuilder.start();

        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();

        Thread outputThread = new Thread(() -> {
            try {
                inputStream.transferTo(new OutputStream() {
                    // buffer and write lines
                    private String line;

                    @Override
                    public void write(int b) {
                        if (b == '\r') {
                            return;
                        }
                        if (b == '\n') {
                            LOGGER.info(line);
                            line = null;
                        } else {
                            line = (line == null ? "" : line) + (char) b;
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Thread errorThread = new Thread(() -> {
            try {
                errorStream.transferTo(new OutputStream() {
                    // buffer and write lines
                    private String line;

                    @Override
                    public void write(int b) {
                        if (b == '\r') {
                            return;
                        }
                        if (b == '\n') {
                            LOGGER.error(line);
                            line = null;
                        } else {
                            line = (line == null ? "" : line) + (char) b;
                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        outputThread.start();
        errorThread.start();

        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                return process.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        if (wait) {
            future.join();
        }
        return future;
    }

    public static FileSystem openZipFileSystem(Path path, Map<String, ?> args) throws IOException {
        if (!Files.exists(path) && args.get("create") == Boolean.TRUE) {
            try (ZipOutputStream stream = new ZipOutputStream(Files.newOutputStream(path))) {
                stream.closeEntry();
            }
        }
        return FileSystems.newFileSystem(URI.create("jar:" + path.toUri()), args, null);
    }

    public static FileSystem openZipFileSystem(Path path) throws IOException {
        return openZipFileSystem(path, Collections.emptyMap());
    }

    protected void remap(Path inputJar, Path pluginFolderIn) throws IOException {
        LOGGER.info("remapping patches");
        String target = "intermediary";
        boolean isDev = FabricLoader.getInstance().isDevelopmentEnvironment();
        Path intermediaryJar = fukkitDir.resolve("patched-" + mcVersion + "-" + patchVersion + "-intermediary.jar");

        TinyRemapper remapper = remapJar(inputJar, intermediaryJar, "spigot", "official", "intermediary", "intermediary", null);

        // TODO: remap plugins to intermediary
        Path intermediaryPlugins = fukkitDir.resolve("patched-" + mcVersion + "-intermediary-plugins");
        Files.createDirectories(intermediaryPlugins);
        // delete old plugins
        try (Stream<Path> st = Files.list(intermediaryPlugins)) {
            st.forEach(e -> {
                try {
                    Files.delete(e);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        Files.createDirectories(pluginFolderIn);

        try (Stream<Path> st = Files.list(pluginFolderIn)) {
            for (Path plugin : st.collect(Collectors.toList())) {
                if (plugin.endsWith(".jar")) {
                    Path intermediaryPlugin = intermediaryPlugins.resolve(plugin.getFileName());
                    remapJar(plugin, intermediaryPlugin, "spigot", "official", "intermediary", "intermediary", remapper);
                }
            }
        }

        remapper.finish();


        if (isDev) {
            LOGGER.info("remapping intermediary to named");
            Path namedJar = fukkitDir.resolve("patched-" + mcVersion + "-" + patchVersion + "-named.jar");
            remapper = remapJar(intermediaryJar, namedJar, "intermediary", "official", "intermediary", "named", null);

            // TODO: remap plugins to named
            Path namedPlugins = fukkitDir.resolve("patched-" + mcVersion + "-named-plugins");
            Files.createDirectories(namedPlugins);
            // delete old plugins
            try (Stream<Path> st = Files.list(namedPlugins)) {
                st.forEach(e -> {
                    try {
                        Files.delete(e);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });
            }

            try (Stream<Path> st = Files.list(intermediaryPlugins)) {
                for (Path plugin : st.collect(Collectors.toList())) {
                    Path namedPlugin = namedPlugins.resolve(plugin.getFileName());
                    remapJar(plugin, namedPlugin, "intermediary", "official", "intermediary", "named", remapper);
                }
            }

            // overwrite the plugins folder
            try (Stream<Path> st = Files.list(namedPlugins)) {
                for (Path plugin : st.collect(Collectors.toList())) {
                    Files.copy(plugin, pluginFolderIn.resolve(plugin.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            remapper.finish();

            remappedBukkit = namedJar;
            remappedPlugins = pluginFolderIn;
        } else {
            // overwrite the plugins folder
            try (Stream<Path> st = Files.list(intermediaryPlugins)) {
                for (Path plugin : st.collect(Collectors.toList())) {
                    Files.copy(plugin, pluginFolderIn.resolve(plugin.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
            }


            remappedBukkit = intermediaryJar;
            remappedPlugins = pluginFolderIn;
        }
    }

    protected void editMappings(MemoryMappingTree mappings) {}

    private MemoryMappingTree resolveMappings() throws IOException {
        if (mappings != null) return mappings;
        var mappings = new MemoryMappingTree();
        try (InputStream is = FukkitEarlyRiser.class.getClassLoader().getResourceAsStream("mappings/mappings.tiny")) {
            MappingReader.read(new InputStreamReader(is), mappings);
        }
        // read csrg from spigot
        URL spigotManifest = URI.create("https://hub.spigotmc.org/versions/" + mcVersion + ".json").toURL();
        String csrg;
        try (InputStream is = spigotManifest.openStream()) {
            csrg = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject().getAsJsonObject("refs").get("BuildData").getAsString();
        }
        URL csrgUrl = URI.create("https://hub.spigotmc.org/stash/rest/api/latest/projects/SPIGOT/repos/builddata/archive?at=" + csrg + "&format=zip").toURL();
        try (InputStream is = csrgUrl.openStream()) {
            // write to temp zip
            Path t = tempDir.resolve("bukkit-mappings-" + mcVersion + ".zip");
            Files.write(t, is.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            try (FileSystem fs = openZipFileSystem(t)) {
                try (InputStream csrgStream = Files.newInputStream(fs.getPath("mappings", "bukkit-" + mcVersion + "-cl.csrg"))) {
                    CsrgReader.readClasses(new InputStreamReader(csrgStream), "official", "spigot", mappings);
                }
            }
        }
        editMappings(mappings);
        this.mappings = mappings;
        // write mappings to file for debugging
        try (OutputStreamWriter os = new OutputStreamWriter(Files.newOutputStream(tempDir.resolve("mappings.tiny"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {
            Tiny2Writer w = new Tiny2Writer(os, false);
            mappings.accept(w);
        }
        return mappings;
    }

    private TinyRemapper remapJar(Path inputJar, Path outputJar, String fNs, String ffNs, String ftNs, String tNs, TinyRemapper tr) throws IOException {
        IMappingProvider provider = getMappingProvider(fNs, ffNs, ftNs, tNs);
        if (tr == null) {
            tr = TinyRemapper.newRemapper()
                    .withMappings(provider)
                    .renameInvalidLocals(true)
                    .resolveMissing(true)
                    .skipLocalVariableMapping(true)
                    .checkPackageAccess(true)
                    .fixPackageAccess(true)
                    .threads(Runtime.getRuntime().availableProcessors())
                    .resolveMissing(true)
                    .ignoreConflicts(true)
                    .keepInputData(true)
                    .build();

            // get current classpath
            String classpath = System.getProperty("java.class.path");
            String[] classpathEntries = classpath.split(File.pathSeparator);
            tr.readClassPathAsync(Arrays.stream(classpathEntries).map(Paths::get).toArray(Path[]::new));

            if (Files.exists(outputJar)) {
                LOGGER.info("skipping remap for {}, already exists", outputJar);
                tr.readClassPathAsync(inputJar);
                return tr;
            }
        } else {
            if (Files.exists(outputJar)) {
                LOGGER.info("skipping remap for {}, already exists", outputJar);
                return tr;
            }
        }
        InputTag tag = tr.createInputTag();
        tr.readInputs(tag, inputJar);
        try (OutputConsumerPath it = new OutputConsumerPath.Builder(outputJar).build()) {
            it.addNonClassFiles(inputJar, NonClassCopyMode.FIX_META_INF, tr);
            tr.apply(it, tag);
        } catch (Exception e) {
            LOGGER.warn("Failed to remap jar " + inputJar, e);
            Files.deleteIfExists(outputJar);
            throw new RuntimeException(e);
        }
        return tr;
    }

    private IMappingProvider.Member memberOf(String className, String memberName, String memberDesc) {
        return new IMappingProvider.Member(className, memberName, memberDesc);
    }

    private String fixInnerClassName(MappingTree mappings, int fromId, int fallbackSrcId, int fallbackTargetId, int toId, MappingTree.ClassMapping clazz, String fromClassName, String toClassName) {
        var outerClass = fromClassName.substring(0, fromClassName.lastIndexOf('$'));
        var outerClassDef = Optional.ofNullable((MappingTree.ClassMapping) mappings.getClass(outerClass, fromId)).orElse(mappings.getClass(outerClass, fallbackSrcId));
        if (outerClassDef != null) {
            var outerFromClassName = Optional.ofNullable(outerClassDef.getName(fromId)).orElse(outerClassDef.getName(fallbackSrcId));
            var outerToClassName = Optional.ofNullable(outerClassDef.getName(toId)).orElse(outerClassDef.getName(fallbackTargetId));
            if (outerFromClassName != null && outerFromClassName.contains("$")) {
                outerToClassName = fixInnerClassName(mappings, fromId, fallbackSrcId, fallbackTargetId, toId, outerClassDef, outerFromClassName, outerToClassName);
            }
            var innerClassName = Optional.ofNullable(toClassName).map(s -> s.substring(s.lastIndexOf('$'))).orElse(fromClassName.substring(fromClassName.lastIndexOf('$')));
            if (outerToClassName != null && (toClassName == null || !toClassName.startsWith(outerToClassName))) {
                toClassName = outerToClassName + "$" + innerClassName;
                LOGGER.warn("Detected missing inner class, replacing with: {} -> {}", fromClassName, toClassName);
            }
        }
        return toClassName;
    }

    private IMappingProvider getMappingProvider(String srcName, String fallbackSrc, String fallbackTarget, String targetName) throws IOException {
        MappingTree tree = resolveMappings();
        return acceptor -> {
            var fromId = tree.getNamespaceId(srcName);
            var fallbackSrcId = tree.getNamespaceId(fallbackSrc);
            var fallbackTargetId = tree.getNamespaceId(fallbackTarget);
            var toId = tree.getNamespaceId(targetName);

            if (fromId == MappingTreeView.NULL_NAMESPACE_ID) {
                throw new IllegalArgumentException("Unknown namespace: " + srcName);
            }

            if (toId == MappingTreeView.NULL_NAMESPACE_ID) {
                throw new IllegalArgumentException("Unknown namespace: " + targetName);
            }

            if (fallbackTargetId == MappingTreeView.NULL_NAMESPACE_ID) {
                LOGGER.warn("Unknown namespace: " + fallbackTarget + ", falling back to " + srcName);
                fallbackTargetId = fromId;
            }

            if (fallbackSrcId == MappingTreeView.NULL_NAMESPACE_ID) {
                LOGGER.warn("Unknown namespace: " + fallbackSrc + ", falling back to " + srcName);
                fallbackSrcId = fromId;
            }

            LOGGER.info("Remapping " + srcName + " to " + targetName + " with fallbacks " + fallbackSrc + " and " + fallbackTarget);

            for (MappingTree.ClassMapping classDef : mappings.getClasses()) {
                var fromClassName = Optional.ofNullable(classDef.getName(fromId)).orElse(classDef.getName(fallbackSrcId));
                var toClassName = Optional.ofNullable(classDef.getName(toId)).orElse(classDef.getName(fallbackTargetId));

                if (fromClassName != null && fromClassName.contains("$")) {
                    toClassName = fixInnerClassName(mappings, fromId, fallbackSrcId, fallbackTargetId, toId, classDef, fromClassName, toClassName);
                }

                if (toClassName == null) {
                    LOGGER.debug("Found no target name for " + classDef);
                    toClassName = fromClassName;
                }

                if (fromClassName == null) {
                    throw new IllegalStateException("Found no source name for " + classDef);
                }

                LOGGER.debug("Remapping class " + fromClassName + " to " + toClassName);
                acceptor.acceptClass(fromClassName, toClassName);

                for (MappingTree.FieldMapping field : classDef.getFields()) {
                    var fromFieldName = Optional.ofNullable(field.getName(fromId)).orElse(field.getName(fallbackSrcId));
                    var toFieldName = Optional.ofNullable(field.getName(toId)).orElse(field.getName(fallbackTargetId));

                    if (fromFieldName == null) {
                        LOGGER.debug("Found no source name for " + field);
                        fromFieldName = toFieldName;
                    }

                    if (toFieldName == null) {
                        LOGGER.debug("Found no target name for field " + field);
                        toFieldName = fromFieldName;
                    }

                    if (fromFieldName == null) {
                        LOGGER.error("Found no source name for field " + field);
                    }

                    if (fromFieldName != null) {
                        LOGGER.debug("Remapping field " + fromFieldName + " to " + toFieldName);
                        acceptor.acceptField(memberOf(fromClassName, fromFieldName, field.getDesc(fromId)), toFieldName);
                    }
                    if (fromClassName.endsWith("ColorableAgeableListModel")) {
                        LOGGER.info("Remapping field " + fromFieldName + " to " + toFieldName);
                    }
                }

                for (MappingTree.MethodMapping method : classDef.getMethods()) {
                    var fromMethodName = Optional.ofNullable(method.getName(fromId)).orElse(method.getName(fallbackSrcId));
                    var toMethodName = Optional.ofNullable(method.getName(toId)).orElse(method.getName(fallbackTargetId));
                    var fromMethodDesc = Optional.ofNullable(method.getDesc(fromId)).orElse(method.getDesc(fallbackSrcId));

                    if (fromMethodName == null) {
                        LOGGER.debug("Found no source name for " + method);
                        fromMethodName = toMethodName;
                    }

                    if (toMethodName == null) {
                        LOGGER.debug("Found no target name for method " + method);
                        toMethodName = fromMethodName;
                    }

                    if (fromMethodName == null) {
                        LOGGER.error("Found no source name for method " + method);
                    }

                    if (fromMethodName != null) {
                        LOGGER.debug("Remapping method " + fromMethodName + " to " + toMethodName);
                        acceptor.acceptMethod(memberOf(fromClassName, fromMethodName, fromMethodDesc), toMethodName);
                    }
                }
            }

        };
    }

    public Set<String> getPatchedClasses() {
        return Set.copyOf(patchedClasses);
    }
}
