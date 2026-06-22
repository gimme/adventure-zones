package dev.gimme.adventurezones.gametest;

import dev.gimme.adventurezones.domain.config.ServerConfig;
import dev.gimme.adventurezones.infrastructure.ConfigTestSupport;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Loader-agnostic game test bodies. Each {@code static void(GameTestHelper)} method is one test;
 * a test passes by calling {@link GameTestHelper#succeed()} and fails by throwing.
 *
 * <p>To add a test: write the method here, then wire it into {@code FabricGameTests} and
 * {@code NeoForgeGameTests}.
 */
public final class AdventureZonesGameTests {

    private AdventureZonesGameTests() {
    }

    /**
     * The shared {@link ServerConfig} is initialized by the loader and exposes its documented defaults,
     * exercising the cross-loader Forge Config API Port + night-config TOML parsing path on either loader.
     */
    public static void configDefaultsLoaded(GameTestHelper helper) {
        ServerConfig config = ServerConfig.INSTANCE;
        helper.assertTrue(config != null, "ServerConfig should be initialized by the loader");
        helper.assertTrue(config.getCombatModeSeconds() == 10,
                "default combatModeSeconds should be 10 but was " + config.getCombatModeSeconds());
        helper.assertFalse(config.getZoneConfigs().isEmpty(),
                "the default config should define at least one adventure zone");
        helper.assertTrue(config.getMaxZoneRadius() == 16,
                "max zone radius across the defaults should be 16 but was " + config.getMaxZoneRadius());
        helper.succeed();
    }

    /**
     * Config values are live: a change to {@code combatModeSeconds} is reflected through {@link ServerConfig},
     * confirming the test-support handles bind to the loaded spec on either loader.
     */
    public static void combatSecondsConfigurable(GameTestHelper helper) {
        int original = ServerConfig.INSTANCE.getCombatModeSeconds();
        try {
            ConfigTestSupport.COMBAT_MODE_SECONDS.set(30);
            helper.assertTrue(ServerConfig.INSTANCE.getCombatModeSeconds() == 30,
                    "combatModeSeconds should follow the configured value of 30 but was "
                            + ServerConfig.INSTANCE.getCombatModeSeconds());
        } finally {
            ConfigTestSupport.COMBAT_MODE_SECONDS.set(original);
        }
        helper.succeed();
    }
}
