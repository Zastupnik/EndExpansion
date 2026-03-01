package com.zastupnik.endexpansion.handler;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.config.Configuration;
import com.zastupnik.endexpansion.world.biome.EndBiomes;

import java.io.File;

public class ConfigHandler {

    // ===== СПАВН ОСТРОВОВ =====

    public static int minIslandGap = 80; // Минимальный зазор между краями островов (блоки)

    // ===== ID БИОМОВ =====
    public static int biomeCemeteryID  = 240;
    public static int biomeDesertID    = 241;
    public static int biomeForestID    = 242;
    public static int biomeInfectionID = 243;
    public static int biomeJungleID    = 244;
    public static int biomeOceanID     = 245;
    public static int biomeFortressID  = 246;

    // Шанс спавна (1 из N чанков). Больше = реже.
    public static int forestSpawnChance    = 18;
    public static int jungleSpawnChance    = 22;
    public static int desertSpawnChance    = 22;
    public static int oceanSpawnChance     = 30;
    public static int cemeterySpawnChance  = 55;
    public static int infectionSpawnChance = 65;
    public static int fortressSpawnChance  = 90;

    // ===== РАЗМЕРЫ ОСТРОВОВ =====

    public static int cemeteryRadiusMin  = 60;  public static int cemeteryRadiusMax  = 120;
    public static int fortressRadiusMin  = 50;  public static int fortressRadiusMax  = 90;
    public static int jungleRadiusMin    = 35;  public static int jungleRadiusMax    = 65;
    public static int forestRadiusMin    = 25;  public static int forestRadiusMax    = 55;
    public static int oceanRadiusMin     = 40;  public static int oceanRadiusMax     = 90;
    public static int desertRadiusMin    = 20;  public static int desertRadiusMax    = 45;
    public static int infectionRadiusMin = 15;  public static int infectionRadiusMax = 35;

    // ===== ВЫСОТА ОСТРОВОВ (Y) =====

    public static int fortressYMin  = 65; public static int fortressYMax  = 95;
    public static int oceanYMin     = 38; public static int oceanYMax     = 52;
    public static int infectionYMin = 42; public static int infectionYMax = 62;
    public static int cemeteryYMin  = 48; public static int cemeteryYMax  = 72;
    public static int desertYMin    = 45; public static int desertYMax    = 70;
    public static int jungleYMin    = 50; public static int jungleYMax    = 75;
    public static int forestYMin    = 48; public static int forestYMax    = 73;

    // ===== ЛУТ: КЛАДБИЩЕ =====

    public static int cemeteryChestBoneMin        = 2; public static int cemeteryChestBoneMax        = 8;
    public static int cemeteryChestEnderPearlMin   = 1; public static int cemeteryChestEnderPearlMax   = 4;
    public static int cemeteryChestGoldIngotMin    = 0; public static int cemeteryChestGoldIngotMax    = 3;
    public static int cemeteryChestRottenFleshMin  = 3; public static int cemeteryChestRottenFleshMax  = 10;
    public static int cemeteryChestDiamondMin      = 0; public static int cemeteryChestDiamondMax      = 1;

    // ===== ЛУТ: ПУСТЫНЯ =====

    public static int desertChestGoldMin           = 1; public static int desertChestGoldMax           = 5;
    public static int desertChestEnderPearlMin     = 2; public static int desertChestEnderPearlMax     = 6;
    public static int desertChestBoneMin           = 2; public static int desertChestBoneMax           = 6;
    public static int desertChestEmeraldMin        = 0; public static int desertChestEmeraldMax        = 2;
    public static int desertChestBreadMin          = 1; public static int desertChestBreadMax          = 4;
    public static int desertChestLeatherMin        = 1; public static int desertChestLeatherMax        = 3;

    // ===== ЛУТ: ОКЕАН =====

    public static int oceanChestFishingRodMin      = 0; public static int oceanChestFishingRodMax      = 1;
    public static int oceanChestFishMin            = 2; public static int oceanChestFishMax            = 6;
    public static int oceanChestGoldMin            = 0; public static int oceanChestGoldMax            = 3;
    public static int oceanChestDiamondMin         = 0; public static int oceanChestDiamondMax         = 1;
    public static int oceanChestCompassMin         = 0; public static int oceanChestCompassMax         = 1;
    public static int oceanChestEnderPearlMin      = 1; public static int oceanChestEnderPearlMax      = 3;

    // ===== ЛУТ: ЗАРАЖЕНИЕ =====

    public static int infectionChestRottenFleshMin = 3; public static int infectionChestRottenFleshMax = 8;
    public static int infectionChestSpiderEyeMin   = 1; public static int infectionChestSpiderEyeMax   = 4;
    public static int infectionChestFermentedMin   = 0; public static int infectionChestFermentedMax   = 2;
    public static int infectionChestStringMin      = 2; public static int infectionChestStringMax      = 6;
    public static int infectionChestEnderPearlMin  = 1; public static int infectionChestEnderPearlMax  = 4;
    public static int infectionChestGlowshroomMin  = 1; public static int infectionChestGlowshroomMax  = 3;

    // ===== ЛУТ: КРЕПОСТЬ =====

    public static int fortressChestIronMin         = 2; public static int fortressChestIronMax         = 6;
    public static int fortressChestArrowMin        = 5; public static int fortressChestArrowMax        = 20;
    public static int fortressChestSwordMin        = 0; public static int fortressChestSwordMax        = 1;
    public static int fortressChestArmorMin        = 0; public static int fortressChestArmorMax        = 1;
    public static int fortressChestDiamondMin      = 0; public static int fortressChestDiamondMax      = 2;
    public static int fortressChestGoldenAppleMin  = 0; public static int fortressChestGoldenAppleMax  = 1;
    public static int fortressChestEnderPearlMin   = 2; public static int fortressChestEnderPearlMax   = 5;

    // ===== МОСТЫ =====

    public static boolean enableBridges        = true;
    public static int     bridgeMaxDistance    = 150; // Максимальное расстояние для строительства моста
    public static boolean bridgeWitheredPlanks = true; // Тип материала мостов

    // ===== ПРОЧЕЕ =====

    public static boolean spawnEndermen = true;
    public static int     endermenPerIsland = 3; // Максимум эндерменов на остров
    public static boolean enableCustomSky       = true;
    public static boolean disableDraconicRitual = true;
    // ===== ИНИЦИАЛИЗАЦИЯ =====

    private static Configuration config;

    public static void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(new File(event.getModConfigurationDirectory(), "endexpansion.cfg"));
        config.load();

        loadWorldGenSettings();
        loadLootSettings();
        loadMiscSettings();

        if (config.hasChanged()) config.save();
    }

    private static void loadWorldGenSettings() {
        String cat = "worldgen";

        String biomesCat = "biomes";
        biomeCemeteryID  = config.getInt("biomeCemeteryID",  biomesCat, 240, 10, 254, "Biome ID for Cemetery. Change if conflicts with another mod.");
        biomeDesertID    = config.getInt("biomeDesertID",    biomesCat, 241, 10, 254, "");
        biomeForestID    = config.getInt("biomeForestID",    biomesCat, 242, 10, 254, "");
        biomeInfectionID = config.getInt("biomeInfectionID", biomesCat, 243, 10, 254, "");
        biomeJungleID    = config.getInt("biomeJungleID",    biomesCat, 244, 10, 254, "");
        biomeOceanID     = config.getInt("biomeOceanID",     biomesCat, 245, 10, 254, "");
        biomeFortressID  = config.getInt("biomeFortressID",  biomesCat, 246, 10, 254, "");

        minIslandGap = config.getInt("minIslandGap", cat, 80, 20, 500,
                "Minimum gap between island edges in blocks. Increase to spread islands apart.");

        forestSpawnChance    = config.getInt("forestSpawnChance",    cat, 18, 1, 200, "1 in N chunks. Higher = rarer.");
        jungleSpawnChance    = config.getInt("jungleSpawnChance",    cat, 22, 1, 200, "1 in N chunks.");
        desertSpawnChance    = config.getInt("desertSpawnChance",    cat, 22, 1, 200, "1 in N chunks.");
        oceanSpawnChance     = config.getInt("oceanSpawnChance",     cat, 30, 1, 200, "1 in N chunks.");
        cemeterySpawnChance  = config.getInt("cemeterySpawnChance",  cat, 55, 1, 200, "1 in N chunks.");
        infectionSpawnChance = config.getInt("infectionSpawnChance", cat, 65, 1, 200, "1 in N chunks.");
        fortressSpawnChance  = config.getInt("fortressSpawnChance",  cat, 90, 1, 200, "1 in N chunks.");

        // Радиусы
        cemeteryRadiusMin  = config.getInt("cemeteryRadiusMin",  cat, 60,  10, 500, "");
        cemeteryRadiusMax  = config.getInt("cemeteryRadiusMax",  cat, 120, 10, 500, "");
        fortressRadiusMin  = config.getInt("fortressRadiusMin",  cat, 50,  10, 500, "");
        fortressRadiusMax  = config.getInt("fortressRadiusMax",  cat, 90,  10, 500, "");
        jungleRadiusMin    = config.getInt("jungleRadiusMin",    cat, 35,  10, 500, "");
        jungleRadiusMax    = config.getInt("jungleRadiusMax",    cat, 65,  10, 500, "");
        forestRadiusMin    = config.getInt("forestRadiusMin",    cat, 25,  10, 500, "");
        forestRadiusMax    = config.getInt("forestRadiusMax",    cat, 55,  10, 500, "");
        oceanRadiusMin     = config.getInt("oceanRadiusMin",     cat, 40,  10, 500, "");
        oceanRadiusMax     = config.getInt("oceanRadiusMax",     cat, 90,  10, 500, "");
        desertRadiusMin    = config.getInt("desertRadiusMin",    cat, 20,  10, 500, "");
        desertRadiusMax    = config.getInt("desertRadiusMax",    cat, 45,  10, 500, "");
        infectionRadiusMin = config.getInt("infectionRadiusMin", cat, 15,  10, 500, "");
        infectionRadiusMax = config.getInt("infectionRadiusMax", cat, 35,  10, 500, "");

        // Y-координаты
        fortressYMin  = config.getInt("fortressYMin",  cat, 65, 10, 200, "");
        fortressYMax  = config.getInt("fortressYMax",  cat, 95, 10, 200, "");
        oceanYMin     = config.getInt("oceanYMin",     cat, 38, 10, 200, "");
        oceanYMax     = config.getInt("oceanYMax",     cat, 52, 10, 200, "");
        infectionYMin = config.getInt("infectionYMin", cat, 42, 10, 200, "");
        infectionYMax = config.getInt("infectionYMax", cat, 62, 10, 200, "");
        cemeteryYMin  = config.getInt("cemeteryYMin",  cat, 48, 10, 200, "");
        cemeteryYMax  = config.getInt("cemeteryYMax",  cat, 72, 10, 200, "");
        desertYMin    = config.getInt("desertYMin",    cat, 45, 10, 200, "");
        desertYMax    = config.getInt("desertYMax",    cat, 70, 10, 200, "");
        jungleYMin    = config.getInt("jungleYMin",    cat, 50, 10, 200, "");
        jungleYMax    = config.getInt("jungleYMax",    cat, 75, 10, 200, "");
        forestYMin    = config.getInt("forestYMin",    cat, 48, 10, 200, "");
        forestYMax    = config.getInt("forestYMax",    cat, 73, 10, 200, "");
    }

    private static void loadLootSettings() {
        String cat = "loot";

        // Кладбище
        cemeteryChestBoneMin       = config.getInt("cemeteryChestBoneMin",       cat, 2, 0, 64, "");
        cemeteryChestBoneMax       = config.getInt("cemeteryChestBoneMax",       cat, 8, 0, 64, "");
        cemeteryChestEnderPearlMin = config.getInt("cemeteryChestEnderPearlMin", cat, 1, 0, 64, "");
        cemeteryChestEnderPearlMax = config.getInt("cemeteryChestEnderPearlMax", cat, 4, 0, 64, "");
        cemeteryChestGoldIngotMin  = config.getInt("cemeteryChestGoldIngotMin",  cat, 0, 0, 64, "");
        cemeteryChestGoldIngotMax  = config.getInt("cemeteryChestGoldIngotMax",  cat, 3, 0, 64, "");
        cemeteryChestRottenFleshMin= config.getInt("cemeteryChestRottenFleshMin",cat, 3, 0, 64, "");
        cemeteryChestRottenFleshMax= config.getInt("cemeteryChestRottenFleshMax",cat, 10,0, 64, "");
        cemeteryChestDiamondMin    = config.getInt("cemeteryChestDiamondMin",    cat, 0, 0, 64, "");
        cemeteryChestDiamondMax    = config.getInt("cemeteryChestDiamondMax",    cat, 1, 0, 64, "");

        // Пустыня
        desertChestGoldMin         = config.getInt("desertChestGoldMin",         cat, 1, 0, 64, "");
        desertChestGoldMax         = config.getInt("desertChestGoldMax",         cat, 5, 0, 64, "");
        desertChestEnderPearlMin   = config.getInt("desertChestEnderPearlMin",   cat, 2, 0, 64, "");
        desertChestEnderPearlMax   = config.getInt("desertChestEnderPearlMax",   cat, 6, 0, 64, "");
        desertChestBoneMin         = config.getInt("desertChestBoneMin",         cat, 2, 0, 64, "");
        desertChestBoneMax         = config.getInt("desertChestBoneMax",         cat, 6, 0, 64, "");
        desertChestEmeraldMin      = config.getInt("desertChestEmeraldMin",      cat, 0, 0, 64, "");
        desertChestEmeraldMax      = config.getInt("desertChestEmeraldMax",      cat, 2, 0, 64, "");
        desertChestBreadMin        = config.getInt("desertChestBreadMin",        cat, 1, 0, 64, "");
        desertChestBreadMax        = config.getInt("desertChestBreadMax",        cat, 4, 0, 64, "");
        desertChestLeatherMin      = config.getInt("desertChestLeatherMin",      cat, 1, 0, 64, "");
        desertChestLeatherMax      = config.getInt("desertChestLeatherMax",      cat, 3, 0, 64, "");

        // Океан
        oceanChestFishingRodMin    = config.getInt("oceanChestFishingRodMin",    cat, 0, 0, 64, "");
        oceanChestFishingRodMax    = config.getInt("oceanChestFishingRodMax",    cat, 1, 0, 64, "");
        oceanChestFishMin          = config.getInt("oceanChestFishMin",          cat, 2, 0, 64, "");
        oceanChestFishMax          = config.getInt("oceanChestFishMax",          cat, 6, 0, 64, "");
        oceanChestGoldMin          = config.getInt("oceanChestGoldMin",          cat, 0, 0, 64, "");
        oceanChestGoldMax          = config.getInt("oceanChestGoldMax",          cat, 3, 0, 64, "");
        oceanChestDiamondMin       = config.getInt("oceanChestDiamondMin",       cat, 0, 0, 64, "");
        oceanChestDiamondMax       = config.getInt("oceanChestDiamondMax",       cat, 1, 0, 64, "");
        oceanChestCompassMin       = config.getInt("oceanChestCompassMin",       cat, 0, 0, 64, "");
        oceanChestCompassMax       = config.getInt("oceanChestCompassMax",       cat, 1, 0, 64, "");
        oceanChestEnderPearlMin    = config.getInt("oceanChestEnderPearlMin",    cat, 1, 0, 64, "");
        oceanChestEnderPearlMax    = config.getInt("oceanChestEnderPearlMax",    cat, 3, 0, 64, "");

        // Заражение
        infectionChestRottenFleshMin= config.getInt("infectionChestRottenFleshMin",cat, 3, 0, 64, "");
        infectionChestRottenFleshMax= config.getInt("infectionChestRottenFleshMax",cat, 8, 0, 64, "");
        infectionChestSpiderEyeMin  = config.getInt("infectionChestSpiderEyeMin",  cat, 1, 0, 64, "");
        infectionChestSpiderEyeMax  = config.getInt("infectionChestSpiderEyeMax",  cat, 4, 0, 64, "");
        infectionChestFermentedMin  = config.getInt("infectionChestFermentedMin",  cat, 0, 0, 64, "");
        infectionChestFermentedMax  = config.getInt("infectionChestFermentedMax",  cat, 2, 0, 64, "");
        infectionChestStringMin     = config.getInt("infectionChestStringMin",     cat, 2, 0, 64, "");
        infectionChestStringMax     = config.getInt("infectionChestStringMax",     cat, 6, 0, 64, "");
        infectionChestEnderPearlMin = config.getInt("infectionChestEnderPearlMin", cat, 1, 0, 64, "");
        infectionChestEnderPearlMax = config.getInt("infectionChestEnderPearlMax", cat, 4, 0, 64, "");
        infectionChestGlowshroomMin = config.getInt("infectionChestGlowshroomMin", cat, 1, 0, 64, "");
        infectionChestGlowshroomMax = config.getInt("infectionChestGlowshroomMax", cat, 3, 0, 64, "");

        // Крепость
        fortressChestIronMin       = config.getInt("fortressChestIronMin",       cat, 2, 0, 64, "");
        fortressChestIronMax       = config.getInt("fortressChestIronMax",       cat, 6, 0, 64, "");
        fortressChestArrowMin      = config.getInt("fortressChestArrowMin",      cat, 5, 0, 64, "");
        fortressChestArrowMax      = config.getInt("fortressChestArrowMax",      cat, 20,0, 64, "");
        fortressChestSwordMin      = config.getInt("fortressChestSwordMin",      cat, 0, 0, 64, "");
        fortressChestSwordMax      = config.getInt("fortressChestSwordMax",      cat, 1, 0, 64, "");
        fortressChestArmorMin      = config.getInt("fortressChestArmorMin",      cat, 0, 0, 64, "");
        fortressChestArmorMax      = config.getInt("fortressChestArmorMax",      cat, 1, 0, 64, "");
        fortressChestDiamondMin    = config.getInt("fortressChestDiamondMin",    cat, 0, 0, 64, "");
        fortressChestDiamondMax    = config.getInt("fortressChestDiamondMax",    cat, 2, 0, 64, "");
        fortressChestGoldenAppleMin= config.getInt("fortressChestGoldenAppleMin",cat, 0, 0, 64, "");
        fortressChestGoldenAppleMax= config.getInt("fortressChestGoldenAppleMax",cat, 1, 0, 64, "");
        fortressChestEnderPearlMin = config.getInt("fortressChestEnderPearlMin", cat, 2, 0, 64, "");
        fortressChestEnderPearlMax = config.getInt("fortressChestEnderPearlMax", cat, 5, 0, 64, "");
    }

    private static void loadMiscSettings() {
        String cat = "misc";
        enableBridges       = config.getBoolean("enableBridges",       cat, true,  "Build bridges between nearby islands");
        bridgeMaxDistance   = config.getInt    ("bridgeMaxDistance",   cat, 150, 20, 500, "Max distance between island edges to build a bridge");
        bridgeWitheredPlanks= config.getBoolean("bridgeWitheredPlanks",cat, true, "Use withered planks for bridges (false = end stone)");
        spawnEndermen       = config.getBoolean("spawnEndermen",       cat, true, "Spawn endermen on islands during generation");
        endermenPerIsland   = config.getInt    ("endermenPerIsland",   cat, 3, 0, 20, "Max endermen spawned per island");
    }

    // ===== УДОБНЫЙ МЕТОД ДЛЯ WorldGenManager =====

    public static int getSpawnChance(net.minecraft.world.biome.BiomeGenBase biome) {
        if (biome == EndBiomes.biomeFortress)  return fortressSpawnChance;
        if (biome == EndBiomes.biomeInfection) return infectionSpawnChance;
        if (biome == EndBiomes.biomeCemetery)  return cemeterySpawnChance;
        if (biome == EndBiomes.biomeOcean)     return oceanSpawnChance;
        if (biome == EndBiomes.biomeDesert)    return desertSpawnChance;
        if (biome == EndBiomes.biomeJungle)    return jungleSpawnChance;
        if (biome == EndBiomes.biomeForest)    return forestSpawnChance;
        return 25;
    }
}