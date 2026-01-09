package dev.gimme.adventurezones.domain.config;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public abstract class ServerConfig {

    public static ServerConfig INSTANCE;

    public abstract int getZoneRadius();

    public abstract List<ResourceLocation> getBlockWhitelist();

    public abstract List<ResourceLocation> getStructureWhitelist();

    public abstract List<ResourceLocation> getStructureBlacklist();

    public abstract boolean displayZoneText();
}
