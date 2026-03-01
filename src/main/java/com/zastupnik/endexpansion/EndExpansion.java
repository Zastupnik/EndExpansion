package com.zastupnik.endexpansion;

import com.zastupnik.endexpansion.blocks.*;
import com.zastupnik.endexpansion.handler.*;
import com.zastupnik.endexpansion.items.*;
import com.zastupnik.endexpansion.proxy.CommonProxy;
import com.zastupnik.endexpansion.world.biome.EndBiomes;
import com.zastupnik.endexpansion.world.gen.WorldGenManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = EndExpansion.MODID, name = EndExpansion.NAME, version = EndExpansion.VERSION)
public class EndExpansion {

    public static final String MODID   = "endexpansion";
    public static final String NAME    = "End Expansion";
    public static final String VERSION = "1.0";

    @SidedProxy(
            clientSide = "com.zastupnik.endexpansion.proxy.ClientProxy",
            serverSide = "com.zastupnik.endexpansion.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static final CreativeTabs tabEndExpansion = new CreativeTabs("tabEndExpansion") {
        @Override public Item getTabIconItem() { return Item.getItemFromBlock(EndExpansion.deadGrass); }
    };

    // =========================================================================
    // БЛОКИ — ПОВЕРХНОСТИ (5)
    // Топовый слой каждого биомного острова
    // =========================================================================
    public static Block deadGrass;          // Кладбище — мёртвая трава
    public static Block forestMoss;         // Лес — мох
    public static Block infestedMycelium;   // Заражение — заражённый мицелий
    public static Block jungleTurf;         // Джунгли — дёрн
    public static Block endSand;            // Пустыня — эндер-песок (падает)

    // =========================================================================
    // БЛОКИ — СТРУКТУРНЫЕ КАМНИ (6)
    // Основной наполнитель тела островов
    // =========================================================================
    public static Block ashenStone;         // Пепельный камень       (Кладбище, Лес)
    public static Block oceanStone;         // Морской камень          (Океан)
    public static Block pulsingRock;        // Пульсирующая порода     (Заражение)
    public static Block sandstoneEnd;       // Эндер-песчаник          (Пустыня)
    public static Block fortressBrick;      // Кирпич крепости         (Крепость)
    public static Block fortressPillar;     // Колонна крепости        (Крепость)

    // =========================================================================
    // БЛОКИ — УНИКАЛЬНЫЕ КАМНИ (8)
    // Редкие блоки, встречаются внутри островов или как декор
    // =========================================================================
    public static Block voidCrystalOre;     // Руда кристалла пустоты  (все биомы, редко)
    public static Block endMarble;          // Эндер-мрамор            (Крепость)
    public static Block obsidianEnd;        // Эндер-обсидиан          (Крепость)
    public static Block corruptedStone;     // Поражённый камень       (Заражение)
    public static Block ashBlock;           // Блок пепла              (Кладбище)
    public static Block coralStoneEnd;      // Коралловый камень       (Океан)
    public static Block desertGlass;        // Пустынное стекло        (Пустыня, светится)
    public static Block mossyAshenStone;    // Замшелый пепельный камень(Лес)

    // =========================================================================
    // БЛОКИ — БРЁВНА (4)
    // =========================================================================
    public static Block witheredLog;        // Иссохшее бревно   (Кладбище)
    public static Block ancientLog;         // Древнее бревно    (Лес)
    public static Block infectedStalk;      // Заражённый стебель(Заражение)
    public static Block tropicalLog;        // Тропическое бревно(Джунгли)

    // =========================================================================
    // БЛОКИ — ЛИСТЬЯ / ЗАБОРЫ  (заполняются в makeWoodSet)
    // =========================================================================
    public static Block witheredLeaves, ancientLeaves, infectedLeaves, tropicalLeaves;
    public static Block witheredFence,  ancientFence,  infectedFence,  tropicalFence;

    // =========================================================================
    // БЛОКИ — ФЛОРА (12)
    // Растения, грибы, кристаллы — только ставятся на поверхности
    // =========================================================================
    public static Block spectralRose;       // Призрачная роза       (Кладбище)
    public static Block endCactus;          // Эндер-кактус          (Пустыня)
    public static Block glowshroom;         // Светогриб             (Заражение)
    public static Block seaCrystal;         // Морской кристалл      (Океан, светится)
    public static Block voidFern;           // Папоротник пустоты    (Лес)
    public static Block bloodBloom;         // Кровяной цветок       (Кладбище)
    public static Block endLily;            // Лилия Энда            (Джунгли)
    public static Block crystalFlower;      // Кристальный цветок    (Пустыня)
    public static Block parasiteVine;       // Паразитическая лоза   (Заражение)
    public static Block ghostMoss;          // Призрачный мох        (Кладбище/Лес)
    public static Block voidMoss;           // Мох пустоты           (везде, редко)
    public static Block glowCoral;          // Светящийся коралл     (Океан)

    // =========================================================================
    // БЛОКИ — ДЕКОР / ФУНКЦИОНАЛЬНЫЕ (14)
    // Ставятся в структурах или как декор на островах
    // =========================================================================
    public static Block endTorch;           // Эндер-факел           (светится)
    public static Block gravestone;         // Надгробие             (Кладбище)
    public static Block altarBlock;         // Блок алтаря           (Кладбище)
    public static Block endLantern;         // Эндер-фонарь          (светится сильно)
    public static Block voidCrystalBlock;   // Блок кристалла пустоты(крафт/декор)
    public static Block ancientPedestal;    // Древний постамент     (Лес/Крепость)
    public static Block skulkPillar;        // Жуткая колонна        (Кладбище)
    public static Block trophySkull;        // Трофейный череп       (Кладбище)
    public static Block coralFan;           // Коралловый веер       (Океан)
    public static Block sandPillar;         // Песчаная колонна      (Пустыня)
    public static Block endBell;            // Колокол Энда          (Крепость)
    public static Block bonePile;           // Груда костей          (Кладбище)
    public static Block voidResin;          // Смола пустоты         (Лес, прозрачная)
    public static Block crystalSpire;       // Кристаллический шпиль (Пустыня, светится)

    // =========================================================================
    // ПРЕДМЕТЫ — ТЕЛЕПОРТЕР (1)
    // =========================================================================
    public static Item endTeleporter;

    // =========================================================================
    // ПРЕДМЕТЫ — ЕДА (10)
    // heal = восстанавливаемые пол-сердца, sat = насыщение
    // =========================================================================
    public static Item glowshroomStew;      // heal=4  sat=0.5  Суп из светогриба
    public static Item endFruit;            // heal=6  sat=0.8  Плод Энда (с дерева)
    public static Item voidMushroom;        // heal=2  sat=0.2  Гриб пустоты (сырой)
    public static Item crystalCandy;        // heal=3  sat=1.2  Кристаллическая конфета
    public static Item spectralApple;       // heal=8  sat=2.4  Призрачное яблоко (редкое)
    public static Item driedEndCactus;      // heal=3  sat=0.3  Вяленый эндер-кактус
    public static Item coralSoup;           // heal=5  sat=0.6  Коралловый суп
    public static Item ancientBread;        // heal=7  sat=1.0  Древний хлеб
    public static Item endBerry;            // heal=2  sat=0.4  Ягода Энда (с кустов)
    public static Item voidJelly;           // heal=4  sat=0.7  Кисель пустоты (варится)

    // =========================================================================
    // ПРЕДМЕТЫ — МАТЕРИАЛЫ / КРАФТ (12)
    // =========================================================================
    public static Item voidDust;            // Пыль пустоты           (базовый крафт)
    public static Item endFiber;            // Волокно Энда           (из растений)
    public static Item purifiedEnderPearl;  // Очищенная жемчужина    (крафт)
    public static Item crystalShard;        // Осколок кристалла      (из voidCrystalOre)
    public static Item ashPowder;           // Пепельный порошок      (из ashBlock)
    public static Item infectedSpore;       // Заражённая спора       (из glowshroom)
    public static Item ancientRune;         // Древняя руна           (из структур)
    public static Item voidEssence;         // Сущность пустоты       (редкий крафт)
    public static Item coralFragment;       // Фрагмент коралла       (из Океана)
    public static Item desiccatedSeed;      // Иссушённое семя        (из Пустыни)
    public static Item endResin;            // Смола Энда             (из voidResin)
    public static Item boneDust;            // Костяная пыль          (из Кладбища)

    // =========================================================================
    // ПРЕДМЕТЫ — РАЗНЫЕ (5)
    // =========================================================================
    public static Item endTeleporter2;      // (резерв / другая версия)
    public static Item endCompass;          // Компас Энда            (навигация по биомам)
    public static Item soulJar;             // Банка душ              (Кладбище, квест)
    public static Item endKey;              // Ключ Энда              (открывает сундуки)
    public static Item voidBottle;          // Бутыль пустоты         (пустая/наполненная)

    // =========================================================================
    // INIT
    // =========================================================================

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.preInit(event);
        initItems();
        initBaseBlocks();
        generateDecorSets();
        EndBiomes.init();
    }

    // =========================================================================
    // ПРЕДМЕТЫ
    // =========================================================================

    private void initItems() {
        // Телепортер
        endTeleporter = regItem(new ItemEndTeleporter().setUnlocalizedName("end_teleporter"), "end_teleporter");

        // Еда
        glowshroomStew    = regItem(new ItemEndFood( 4, 0.5F).setUnlocalizedName("glowshroom_stew"),   "glowshroom_stew");
        endFruit          = regItem(new ItemEndFood( 6, 0.8F).setUnlocalizedName("end_fruit"),          "end_fruit");
        voidMushroom      = regItem(new ItemEndFood( 2, 0.2F).setUnlocalizedName("void_mushroom"),      "void_mushroom");
        crystalCandy      = regItem(new ItemEndFood( 3, 1.2F).setUnlocalizedName("crystal_candy"),      "crystal_candy");
        spectralApple     = regItem(new ItemEndFood( 8, 2.4F).setUnlocalizedName("spectral_apple"),     "spectral_apple");
        driedEndCactus    = regItem(new ItemEndFood( 3, 0.3F).setUnlocalizedName("dried_end_cactus"),   "dried_end_cactus");
        coralSoup         = regItem(new ItemEndFood( 5, 0.6F).setUnlocalizedName("coral_soup"),         "coral_soup");
        ancientBread      = regItem(new ItemEndFood( 7, 1.0F).setUnlocalizedName("ancient_bread"),      "ancient_bread");
        endBerry          = regItem(new ItemEndFood( 2, 0.4F).setUnlocalizedName("end_berry"),          "end_berry");
        voidJelly         = regItem(new ItemEndFood( 4, 0.7F).setUnlocalizedName("void_jelly"),         "void_jelly");

        // Материалы
        voidDust           = regItem(new ItemEndBase().setUnlocalizedName("void_dust"),            "void_dust");
        endFiber           = regItem(new ItemEndBase().setUnlocalizedName("end_fiber"),            "end_fiber");
        purifiedEnderPearl = regItem(new ItemEndBase().setUnlocalizedName("purified_ender_pearl"), "purified_ender_pearl");
        crystalShard       = regItem(new ItemEndBase().setUnlocalizedName("crystal_shard"),        "crystal_shard");
        ashPowder          = regItem(new ItemEndBase().setUnlocalizedName("ash_powder"),           "ash_powder");
        infectedSpore      = regItem(new ItemEndBase().setUnlocalizedName("infected_spore"),       "infected_spore");
        ancientRune        = regItem(new ItemEndBase().setUnlocalizedName("ancient_rune"),         "ancient_rune");
        voidEssence        = regItem(new ItemEndBase().setUnlocalizedName("void_essence"),         "void_essence");
        coralFragment      = regItem(new ItemEndBase().setUnlocalizedName("coral_fragment"),       "coral_fragment");
        desiccatedSeed     = regItem(new ItemEndBase().setUnlocalizedName("desiccated_seed"),      "desiccated_seed");
        endResin           = regItem(new ItemEndBase().setUnlocalizedName("end_resin"),            "end_resin");
        boneDust           = regItem(new ItemEndBase().setUnlocalizedName("bone_dust"),            "bone_dust");

        // Разные
        endCompass  = regItem(new ItemEndBase().setUnlocalizedName("end_compass"), "end_compass");
        soulJar     = regItem(new ItemEndBase().setUnlocalizedName("soul_jar"),    "soul_jar");
        endKey      = regItem(new ItemEndBase().setUnlocalizedName("end_key"),     "end_key");
        voidBottle  = regItem(new ItemEndBase().setUnlocalizedName("void_bottle"), "void_bottle");
    }

    // =========================================================================
    // БАЗОВЫЕ БЛОКИ
    // =========================================================================

    private void initBaseBlocks() {
        // Поверхности
        deadGrass        = new BlockEndBase(Material.grass,  "dead_grass",        Block.soundTypeGrass,  0.6F,  1.0F);
        forestMoss       = new BlockEndBase(Material.grass,  "forest_moss",       Block.soundTypeGrass,  0.6F,  1.0F);
        infestedMycelium = new BlockEndBase(Material.ground, "infested_mycelium", Block.soundTypeGravel, 0.8F,  1.0F);
        jungleTurf       = new BlockEndBase(Material.grass,  "jungle_turf",       Block.soundTypeGrass,  0.7F,  1.0F);
        endSand          = new BlockEndFalling(Material.sand,"end_sand",          Block.soundTypeSand,   0.5F,  0.5F);

        // Структурные камни
        ashenStone    = new BlockEndBase(Material.rock, "ashen_stone",    Block.soundTypeStone, 2.0F, 10.0F);
        oceanStone    = new BlockEndBase(Material.rock, "ocean_stone",    Block.soundTypeStone, 2.5F, 15.0F);
        pulsingRock   = new BlockEndBase(Material.rock, "pulsing_rock",   Block.soundTypeStone, 3.0F, 15.0F);
        sandstoneEnd  = new BlockEndBase(Material.rock, "sandstone_end",  Block.soundTypeStone, 1.5F,  5.0F);
        fortressBrick = new BlockEndBase(Material.rock, "fortress_brick", Block.soundTypeStone, 4.0F, 30.0F);
        fortressPillar= new BlockEndBase(Material.rock, "fortress_pillar",Block.soundTypeStone, 4.0F, 20.0F);

        // Уникальные камни
        voidCrystalOre  = new BlockEndBase(Material.rock,  "void_crystal_ore",   Block.soundTypeStone,  3.0F, 15.0F).setLightLevel(0.3F);
        endMarble       = new BlockEndBase(Material.rock,  "end_marble",          Block.soundTypeStone,  2.5F, 12.0F);
        obsidianEnd     = new BlockEndBase(Material.rock,  "obsidian_end",        Block.soundTypeStone,  6.0F, 2000.0F);
        corruptedStone  = new BlockEndBase(Material.rock,  "corrupted_stone",     Block.soundTypeStone,  2.0F, 10.0F);
        ashBlock        = new BlockEndBase(Material.sand,  "ash_block",           Block.soundTypeGravel, 0.8F,  1.0F);
        coralStoneEnd   = new BlockEndBase(Material.rock,  "coral_stone_end",     Block.soundTypeStone,  2.0F, 10.0F);
        desertGlass     = new BlockEndBase(Material.glass, "desert_glass",        Block.soundTypeGlass,  0.4F,  1.0F).setLightLevel(0.2F);
        mossyAshenStone = new BlockEndBase(Material.rock,  "mossy_ashen_stone",   Block.soundTypeStone,  2.0F, 10.0F);

        // Брёвна
        witheredLog   = new BlockEndLog("withered_log");
        ancientLog    = new BlockEndLog("ancient_log");
        infectedStalk = new BlockEndLog("infected_stalk");
        tropicalLog   = new BlockEndLog("tropical_log");

        // Флора
        spectralRose  = new BlockEndPlant("spectral_rose");
        endCactus     = new BlockEndPlant("end_cactus");
        glowshroom    = new BlockEndPlant("glowshroom")    .setLightLevel(0.7F);
        seaCrystal    = new BlockEndBase(Material.glass, "sea_crystal", Block.soundTypeGlass, 0.5F, 1.0F).setLightLevel(1.0F);
        voidFern      = new BlockEndPlant("void_fern");
        bloodBloom    = new BlockEndPlant("blood_bloom")   .setLightLevel(0.15F);
        endLily       = new BlockEndPlant("end_lily");
        crystalFlower = new BlockEndPlant("crystal_flower").setLightLevel(0.3F);
        parasiteVine  = new BlockEndPlant("parasite_vine");
        ghostMoss     = new BlockEndPlant("ghost_moss")    .setLightLevel(0.1F);
        voidMoss      = new BlockEndPlant("void_moss");
        glowCoral     = new BlockEndPlant("glow_coral")    .setLightLevel(0.5F);

        // Декор
        endTorch        = new BlockEndPlant("end_torch")       .setLightLevel(0.8F);
        gravestone      = new BlockEndBase(Material.rock,  "gravestone",        Block.soundTypeStone, 1.5F,  8.0F);
        altarBlock      = new BlockEndBase(Material.rock,  "altar_block",       Block.soundTypeStone, 3.0F, 20.0F);
        endLantern      = new BlockEndBase(Material.iron,  "end_lantern",       Block.soundTypeMetal, 1.5F, 10.0F).setLightLevel(1.0F);
        voidCrystalBlock= new BlockEndBase(Material.glass, "void_crystal_block",Block.soundTypeGlass, 2.0F, 10.0F).setLightLevel(0.8F);
        ancientPedestal = new BlockEndBase(Material.rock,  "ancient_pedestal",  Block.soundTypeStone, 3.0F, 20.0F);
        skulkPillar     = new BlockEndBase(Material.rock,  "skulk_pillar",      Block.soundTypeStone, 2.5F, 15.0F);
        trophySkull     = new BlockEndBase(Material.rock,  "trophy_skull",      Block.soundTypeStone, 1.0F,  5.0F);
        coralFan        = new BlockEndPlant("coral_fan")       .setLightLevel(0.4F);
        sandPillar      = new BlockEndBase(Material.rock,  "sand_pillar",       Block.soundTypeStone, 1.5F,  5.0F);
        endBell         = new BlockEndBase(Material.iron,  "end_bell",          Block.soundTypeMetal, 2.0F, 15.0F);
        bonePile        = new BlockEndBase(Material.rock,  "bone_pile",         Block.soundTypeStone, 0.8F,  2.0F);
        voidResin       = new BlockEndBase(Material.glass, "void_resin",        Block.soundTypeGlass, 0.5F,  2.0F).setLightLevel(0.15F);
        crystalSpire    = new BlockEndBase(Material.glass, "crystal_spire",     Block.soundTypeGlass, 1.5F,  5.0F).setLightLevel(0.6F);
    }

    // =========================================================================
    // НАБОРЫ
    // =========================================================================

    private void generateDecorSets() {
        // Поверхности
        reg(deadGrass); reg(forestMoss); reg(infestedMycelium); reg(jungleTurf); reg(endSand);

        // Структурные камни
        reg(ashenStone); reg(oceanStone); reg(pulsingRock); reg(sandstoneEnd);
        reg(fortressBrick); reg(fortressPillar);

        // Уникальные камни
        reg(voidCrystalOre); reg(endMarble); reg(obsidianEnd); reg(corruptedStone);
        reg(ashBlock); reg(coralStoneEnd); reg(desertGlass); reg(mossyAshenStone);

        // Брёвна
        reg(witheredLog); reg(ancientLog); reg(infectedStalk); reg(tropicalLog);

        // Флора
        reg(spectralRose); reg(endCactus); reg(glowshroom); reg(seaCrystal);
        reg(voidFern); reg(bloodBloom); reg(endLily); reg(crystalFlower);
        reg(parasiteVine); reg(ghostMoss); reg(voidMoss); reg(glowCoral);

        // Декор
        reg(endTorch); reg(gravestone); reg(altarBlock); reg(endLantern);
        reg(voidCrystalBlock); reg(ancientPedestal); reg(skulkPillar); reg(trophySkull);
        reg(coralFan); reg(sandPillar); reg(endBell); reg(bonePile); reg(voidResin); reg(crystalSpire);

        // Каменные наборы (кирпичи + ступени + стена + плита + двойная плита = 5 блоков × 7 наборов = 35)
        makeStoneSet(ashenStone,    "ashen");
        makeStoneSet(oceanStone,    "ocean");
        makeStoneSet(pulsingRock,   "pulsing");
        makeStoneSet(sandstoneEnd,  "sandstone_end");
        makeStoneSet(fortressBrick, "fortress");
        makeStoneSet(endMarble,     "end_marble");
        makeStoneSet(corruptedStone,"corrupted");

        // Деревянные наборы (доски + ступени + забор + ворота + плита + двойная плита + листья = 7 блоков × 4 = 28)
        makeWoodSet(witheredLog,   "withered");
        makeWoodSet(ancientLog,    "ancient");
        makeWoodSet(infectedStalk, "infected");
        makeWoodSet(tropicalLog,   "tropical");
    }

    // =========================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =========================================================================

    private void makeStoneSet(Block base, String name) {
        Block bricks = new BlockEndBase(Material.rock, name + "_bricks", Block.soundTypeStone, 2.0F, 10.0F);
        reg(bricks);
        reg(new BlockEndStairs(bricks, name + "_stairs"));
        reg(new BlockEndWall(bricks,   name + "_wall"));
        BlockEndSlab single = new BlockEndSlab(bricks, name + "_slab",        2.0F, 10.0F);
        BlockEndSlab dbl    = new BlockEndSlab(bricks, name + "_double_slab", 2.0F, 10.0F, single);
        reg(single);
        reg(dbl);
    }

    private void makeWoodSet(Block log, String name) {
        Block planks = new BlockEndBase(Material.wood, name + "_planks", Block.soundTypeWood, 2.0F, 5.0F);
        reg(planks);
        reg(new BlockEndStairs(planks, name + "_stairs"));

        Block fence = new BlockEndFence(name + "_fence", Material.wood);
        reg(fence);
        if (name.equals("withered")) witheredFence = fence;
        if (name.equals("ancient"))  ancientFence  = fence;
        if (name.equals("infected")) infectedFence = fence;
        if (name.equals("tropical")) tropicalFence = fence;

        reg(new BlockEndGate(planks, name + "_gate"));

        BlockEndSlab single = new BlockEndSlab(planks, name + "_slab",        2.0F, 5.0F);
        BlockEndSlab dbl    = new BlockEndSlab(planks, name + "_double_slab", 2.0F, 5.0F, single);
        reg(single);
        reg(dbl);

        Block leaves = new BlockEndLeaves(name + "_leaves");
        reg(leaves);
        if (name.equals("withered")) witheredLeaves = leaves;
        if (name.equals("ancient"))  ancientLeaves  = leaves;
        if (name.equals("infected")) infectedLeaves = leaves;
        if (name.equals("tropical")) tropicalLeaves = leaves;
    }

    private void reg(Block block) {
        if (block == null) return;
        String n = block.getUnlocalizedName().substring(5);
        GameRegistry.registerBlock(block, n);
        block.setCreativeTab(tabEndExpansion);
    }

    private Item regItem(Item item, String name) {
        GameRegistry.registerItem(item, name);
        item.setCreativeTab(tabEndExpansion);
        return item;
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new WorldGenManager(), 10);
        MinecraftForge.EVENT_BUS.register(new EndEventHandler());
        proxy.registerRenderers();
    }
}