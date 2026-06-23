package dev.gimme.structureprotection.fabric;

import dev.gimme.structureprotection.Main;
import dev.gimme.structureprotection.domain.config.ServerConfig;
import dev.gimme.structureprotection.domain.util.Constants;
import dev.gimme.structureprotection.infrastructure.FcapServerConfig;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.neoforged.fml.config.ModConfig;

public class FabricMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.COMMON, FcapServerConfig.SPEC, FcapServerConfig.FILE_NAME);
        ServerConfig.INSTANCE = new FcapServerConfig();

        // Protection is stateless and driven by mixins that read Main.INSTANCE, so wire it once at mod load.
        Main.init();
    }
}
