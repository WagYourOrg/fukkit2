package xyz.wagyourtail.fukkit2;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixins;

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
        if (env != EnvType.SERVER) {
            LOGGER.warn("Fukkit is not intended to be used on the client side. This is not supported. disabling...");
            return;
        }

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

        forMods();
    }

    @Override
    public void onPreLaunch() {
        try {
            patcher.setEntrypoint();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void forMods() {
        for (ModContainer allMod : FabricLoader.getInstance().getAllMods()) {
            switch (allMod.getMetadata().getId()) {
//                case "fabric-gametest-api-v1":
//                    LOGGER.info("Fukkit2: Patching fabric-gametest-api-v1");
//                    Mixins.addConfiguration("fukkit.mixins.fabricgametest.json");
//                    break;
                case "fabric-data-generation-api-v1":
                    LOGGER.info("Fukkit2: Patching fabric-data-generation-api-v1");
                    Mixins.addConfiguration("fukkit2.compat.fabricdatagen.mixins.json");
                    break;
                case "fabric-registry-sync-v0":
                    LOGGER.info("Fukkit2: Patching fabric-registry-sync-v0");
                    Mixins.addConfiguration("fukkit2.compat.fabricregsync.mixins.json");
                    break;
                case "fabric-dimensions-v1":
                    LOGGER.info("Fukkit2: Patching fabric-dimensions-v1");
                    Mixins.addConfiguration("fukkit2.compat.fabricdims.mixins.json");
                    break;
                case "fabric-screen-handler-api-v1":
                    LOGGER.info("Fukkit2: Patching fabric-screen-handler-api-v1");
                    Mixins.addConfiguration("fukkit2.compat.fabricscreenhandler.mixins.json");
                    break;
                case "fabric-entity-events-v1":
                    LOGGER.info("Fukkit2: Patching fabric-entity-events-v1");
                    Mixins.addConfiguration("fukkit2.compat.fabricentityevent.mixins.json");
                    break;
                case "fabric-lifecycle-events-v1":
                    LOGGER.info("Fukkit2: Patching fabric-lifecycle-events-v1");
                    Mixins.addConfiguration("fukkit2.compat.fabriclifecycle.mixins.json");
                    break;
            }
        }
    }
}
