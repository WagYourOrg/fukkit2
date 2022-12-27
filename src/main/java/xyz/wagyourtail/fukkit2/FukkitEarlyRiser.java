package xyz.wagyourtail.fukkit2;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("removal")
public class FukkitEarlyRiser implements Runnable, PreLaunchEntrypoint {
    private final Logger LOGGER = LoggerFactory.getLogger(FukkitEarlyRiser.class);
    static AbstractBukkitPatcher patcher;

    @Override
    public void run() {
        // check if server side
        EnvType env = FabricLoader.getInstance().getEnvironmentType();
        if (env != EnvType.SERVER) return;

        Path fukkit = FabricLoader.getInstance().getConfigDir().resolve("fukkit2");
        Path temp = fukkit.resolve("temp");
        ModContainer minecraft = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow();

        String mcVersion = minecraft.getMetadata().getVersion().getFriendlyString();
        String paperVersion = "362";

        try {
            Files.createDirectories(temp);
            patcher = new PaperPatcher(mcVersion, paperVersion, fukkit, temp);
            patcher.patch();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void onPreLaunch() {
        try {
            patcher.setEntrypoint();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
