package xyz.wagyourtail.fukkit2;

import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PaperPatcher extends AbstractBukkitPatcher {

    Path patched;
    public PaperPatcher(String mcVersion, String patchVersion, Path fukkitDir, Path tempDir) {
        super(mcVersion, patchVersion, fukkitDir, tempDir);
    }

    @Override
    public void patch() throws Exception {
        // download the jar
        Path paperclip = downloadPaperclip(tempDir, mcVersion, patchVersion).toAbsolutePath();
        runPaperclip(paperclip);
        setEntrypoint(paperclip);
        // get patched jar
        patched = tempDir.resolve("versions/" + mcVersion + "/paper-" + mcVersion + ".jar");
        remap(patched, null);
        createServerExtra();
        patchRuntime();
        addLibraries(paperclip);
    }

    @Override
    protected void editMappings(MemoryMappingTree mappings) {
        if (!mappings.getDstNamespaces().contains("named")) {
            //TODO: runtime download and apply mojmap
        }

        //TODO: grab mappings from patched

        // copy named to missing spigot
        int named = mappings.getNamespaceId("named");
        int spigot = mappings.getNamespaceId("spigot");

        for (MappingTree.ClassMapping classDef : mappings.getClasses()) {
            if (classDef.getDstName(spigot) == null) {
                classDef.setDstName(classDef.getDstName(named), spigot);
            }
        }
    }

    private void runPaperclip(Path paperclip) throws Exception {

        LOGGER.info("Starting paperclip...");

        CompletableFuture<Integer> future = runJarInSubprocess(paperclip, new String[]{}, null, tempDir, Map.of(), true, List.of("-Dpaperclip.patchonly=true"));

        if (future.get() != 0) {
            LOGGER.error("Paperclip failed to start!");
            System.exit(1);
        }

        LOGGER.info("Paperclip finished.");
    }

    private Path downloadPaperclip(Path dir, String mcVersion, String build) throws IOException {
        Path jar = dir.resolve("paperclip-" + mcVersion + "-" + build + ".jar");
        if (Files.exists(jar)) return jar;

        URL url = URI.create("https://api.papermc.io/v2/projects/paper/versions/" + mcVersion + "/builds/" + build + "/downloads/paper-" + mcVersion + "-" + build + ".jar").toURL();
        try (OutputStream stream = Files.newOutputStream(jar, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            try (InputStream in = url.openStream()) {
                in.transferTo(stream);
                stream.flush();
            }
        }
        return jar;
    }

    protected void setEntrypoint(Path paperclip) throws IOException {
        try (FileSystem fs = openZipFileSystem(paperclip)) {
            try (InputStream stream = Files.newInputStream(fs.getPath("META-INF/main-class"))) {
                String mainClass = new String(stream.readAllBytes());
                entrypoint = mainClass;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void addLibraries(Path paperclip) throws IOException {
        try (FileSystem fs = openZipFileSystem(paperclip)) {
            try (InputStream stream = Files.newInputStream(fs.getPath("META-INF/libraries.list"))) {
                String[] libs = new String(stream.readAllBytes()).split("\n");
                List<String> libraries = new ArrayList<>();
                for (String lib : libs) {
                    String[] split = lib.split("\t");
                    libraries.add(split[split.length - 1]);
                }
                addLibraries(tempDir.resolve("libraries"), libraries);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
