package dev.gimme.adventurezones.infrastructure;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Test-only handles to package-private {@link FcapServerConfig} values, exposed so game tests can
 * read and modify them.
 */
public final class ConfigTestSupport {

    public static final ModConfigSpec.IntValue COMBAT_MODE_SECONDS = FcapServerConfig.COMBAT_MODE_SECONDS;
    public static final ModConfigSpec.BooleanValue DISPLAY_MODE_TEXT = FcapServerConfig.DISPLAY_MODE_TEXT;

    private ConfigTestSupport() {
    }
}
