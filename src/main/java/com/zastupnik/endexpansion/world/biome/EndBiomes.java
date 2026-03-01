package com.zastupnik.endexpansion.world.biome;

import com.zastupnik.endexpansion.handler.ConfigHandler;
import net.minecraft.world.biome.BiomeGenBase;

public class EndBiomes {

    public static BiomeGenBase biomeCemetery;
    public static BiomeGenBase biomeDesert;
    public static BiomeGenBase biomeForest;
    public static BiomeGenBase biomeInfection;
    public static BiomeGenBase biomeJungle;
    public static BiomeGenBase biomeOcean;
    public static BiomeGenBase biomeFortress;

    public static void init() {
        // (skyColor, fogColor) — цвета атмосферы биома в Энде
        biomeCemetery = new EndBiomeBase(ConfigHandler.biomeCemeteryID, "Cemetery",  0x2F2F2F, 0x1A1A1A);
        biomeDesert   = new EndBiomeBase(ConfigHandler.biomeDesertID,   "EndDesert", 0xFFD700, 0xC2B280);
        biomeForest   = new EndBiomeBase(ConfigHandler.biomeForestID,   "MadForest", 0x4B0082, 0x2E0854);
        biomeInfection= new EndBiomeBase(ConfigHandler.biomeInfectionID,"Infection", 0x32CD32, 0x006400);
        biomeJungle   = new EndBiomeBase(ConfigHandler.biomeJungleID,   "EndJungle", 0xFF1493, 0x8B008B);
        biomeOcean    = new EndBiomeBase(ConfigHandler.biomeOceanID,    "EndOcean",  0x00008B, 0x000033);
        biomeFortress = new EndBiomeBase(ConfigHandler.biomeFortressID, "EndFortress",0x8B0000, 0x330000);

        registerBiome(biomeCemetery);
        registerBiome(biomeDesert);
        registerBiome(biomeForest);
        registerBiome(biomeInfection);
        registerBiome(biomeJungle);
        registerBiome(biomeOcean);
        registerBiome(biomeFortress);
    }

    private static void registerBiome(BiomeGenBase biome) {
        if (biome == null) return;

        if (BiomeGenBase.getBiomeGenArray()[biome.biomeID] != null) {
            System.err.println("[EndExpansion] Biome ID conflict at " + biome.biomeID
                    + " — already used by: " + BiomeGenBase.getBiomeGenArray()[biome.biomeID].biomeName);
            return;
        }

        BiomeGenBase.getBiomeGenArray()[biome.biomeID] = biome;
    }
}