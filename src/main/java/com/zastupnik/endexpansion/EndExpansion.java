package com.zastupnik.endexpansion;

import com.zastupnik.endexpansion.blocks.*;
import com.zastupnik.endexpansion.handler.*;
import com.zastupnik.endexpansion.items.ItemEndTeleporter;
import com.zastupnik.endexpansion.items.ItemEndFood;
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

    public static final String MODID = "endexpansion";
    public static final String NAME = "End Expansion";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "com.zastupnik.endexpansion.proxy.ClientProxy", serverSide = "com.zastupnik.endexpansion.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static final CreativeTabs tabEndExpansion = new CreativeTabs("tabEndExpansion") {
        @Override
        public Item getTabIconItem() { return Item.getItemFromBlock(EndExpansion.deadGrass); }
    };

    // --- ОБЪЯВЛЕНИЕ БЛОКОВ (Группировка по типам для масштаба) ---
// В объявлениях блоков:
    public static Block endTorch;
    public static Block gravestone;
    public static Block deadGrass, forestMoss, infestedMycelium, jungleTurf, endSand;
    public static Block ashenStone, oceanStone, pulsingRock, sandstoneEnd, fortressBrick, fortressPillar;
    public static Block witheredLog, ancientLog, infectedStalk, tropicalLog;
    public static Block witheredLeaves, ancientLeaves, infectedLeaves, tropicalLeaves; // ДОБАВЬ ЭТУ СТРОКУ
    public static Block spectralRose, endCactus, glowshroom, seaCrystal;

    // ПРЕДМЕТЫ
    public static Item endTeleporter;
    public static Item voidPear;
    public static Item gloomBerry;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ConfigHandler.preInit(event);

        endTeleporter = new ItemEndTeleporter().setUnlocalizedName("end_teleporter");
        GameRegistry.registerItem(endTeleporter, "end_teleporter");
        voidPear = new ItemEndFood("void_pear", 5, 0.6F, false);
        gloomBerry = new ItemEndFood("gloom_berry", 3, 0.4F, false);
        GameRegistry.registerItem(voidPear, "void_pear");
        GameRegistry.registerItem(gloomBerry, "gloom_berry");

        // ИНИЦИАЛИЗАЦИЯ ОСНОВЫ
        initBaseBlocks();

        // ГЕНЕРАЦИЯ ДЕКОРА (Автоматически создает сотни вариаций)
        generateDecorSets();

        EndBiomes.init();
    }

    private void initBaseBlocks() {
        // Поверхности

        deadGrass = new BlockEndBase(Material.grass, "dead_grass", Block.soundTypeGrass, 0.6F, 1.0F);
        forestMoss = new BlockEndBase(Material.grass, "forest_moss", Block.soundTypeGrass, 0.6F, 1.0F);
        infestedMycelium = new BlockEndBase(Material.ground, "infested_mycelium", Block.soundTypeGravel, 0.8F, 1.0F);
        jungleTurf = new BlockEndBase(Material.grass, "jungle_turf", Block.soundTypeGrass, 0.7F, 1.0F);
        endSand = new BlockEndFalling(Material.sand, "end_sand", Block.soundTypeSand, 0.5F, 0.5F);

        // Камни
        ashenStone = new BlockEndBase(Material.rock, "ashen_stone", Block.soundTypeStone, 2.0F, 10.0F);
        oceanStone = new BlockEndBase(Material.rock, "ocean_stone", Block.soundTypeStone, 2.5F, 15.0F);
        pulsingRock = new BlockEndBase(Material.rock, "pulsing_rock", Block.soundTypeStone, 3.0F, 15.0F);
        sandstoneEnd = new BlockEndBase(Material.rock, "sandstone_end", Block.soundTypeStone, 1.5F, 5.0F);
        fortressBrick = new BlockEndBase(Material.rock, "fortress_brick", Block.soundTypeStone, 4.0F, 30.0F);
        fortressPillar = new BlockEndBase(Material.rock, "fortress_pillar", Block.soundTypeStone, 4.0F, 20.0F);

        // Деревья

        witheredLog = new BlockEndLog("withered_log");
        ancientLog = new BlockEndLog("ancient_log");
        infectedStalk = new BlockEndLog("infected_stalk");
        tropicalLog = new BlockEndLog("tropical_log");

        // Растения
        spectralRose = new BlockEndPlant("spectral_rose");
        endCactus = new BlockEndPlant("end_cactus");
        glowshroom = new BlockEndPlant("glowshroom").setLightLevel(0.7F);
        seaCrystal = new BlockEndBase(Material.glass, "sea_crystal", Block.soundTypeGlass, 0.5F, 1.0F).setLightLevel(1.0F);

        //
        endTorch   = new BlockEndPlant("end_torch").setLightLevel(0.8F);
        gravestone = new BlockEndBase(Material.rock, "gravestone", Block.soundTypeStone, 1.5F, 8.0F);
    }

    /**
     * ВОТ ТУТ МАГИЯ 150+ БЛОКОВ
     */
    private void generateDecorSets() {
        // Регистрируем основу
        reg(deadGrass); reg(forestMoss); reg(infestedMycelium); reg(jungleTurf); reg(endSand);
        reg(ashenStone); reg(oceanStone); reg(pulsingRock); reg(sandstoneEnd); reg(fortressBrick); reg(fortressPillar);
        reg(witheredLog); reg(ancientLog); reg(infectedStalk); reg(tropicalLog);
        reg(spectralRose); reg(endCactus); reg(glowshroom); reg(seaCrystal);

        // Создаем ПОЛНЫЕ КАМЕННЫЕ НАБОРЫ (Кирпичи, Плиты, Ступени, Стены)
        makeStoneSet(ashenStone, "ashen");
        makeStoneSet(oceanStone, "ocean");
        makeStoneSet(pulsingRock, "pulsing");
        makeStoneSet(sandstoneEnd, "sandstone_end");
        makeStoneSet(fortressBrick, "fortress");

        // Создаем ПОЛНЫЕ ДЕРЕВЯННЫЕ НАБОРЫ (Доски, Плиты, Ступени, Заборы, Листья)
        makeWoodSet(witheredLog, "withered");
        makeWoodSet(ancientLog, "ancient");
        makeWoodSet(infectedStalk, "infected");
        makeWoodSet(tropicalLog, "tropical");

        //
        reg(endTorch);
        reg(gravestone);
    }

    private void makeStoneSet(Block base, String name) {
        Block bricks = new BlockEndBase(Material.rock, name + "_bricks", Block.soundTypeStone, 2.0F, 10.0F);
        reg(bricks);
        reg(new BlockEndStairs(bricks, name + "_stairs"));
        reg(new BlockEndWall(bricks, name + "_wall"));

        // Плиты:
        BlockEndSlab single = new BlockEndSlab(bricks, name + "_slab", 2.0F, 10.0F);
        BlockEndSlab doubleSlab = new BlockEndSlab(bricks, name + "_double_slab", 2.0F, 10.0F, single);
        reg(single);
        reg(doubleSlab);
    }

    private void makeWoodSet(Block log, String name) {
        Block planks = new BlockEndBase(Material.wood, name + "_planks", Block.soundTypeWood, 2.0F, 5.0F);
        reg(planks);
        reg(new BlockEndStairs(planks, name + "_stairs"));
        reg(new BlockEndFence(name + "_fence", Material.wood));
        reg(new BlockEndGate(planks, name + "_gate")); // Добавили

        // Плиты:
        BlockEndSlab single = new BlockEndSlab(planks, name + "_slab", 2.0F, 5.0F);
        BlockEndSlab doubleSlab = new BlockEndSlab(planks, name + "_double_slab", 2.0F, 5.0F, single);
        reg(single);
        reg(doubleSlab);

        Block leaves = new BlockEndLeaves(name + "_leaves");
        reg(leaves);

        if (name.equals("ancient"))  ancientLeaves  = leaves;
        if (name.equals("withered")) witheredLeaves = leaves;
        if (name.equals("infected")) infectedLeaves = leaves;
        if (name.equals("tropical")) tropicalLeaves = leaves;
    }

    private void reg(Block block) {
        if (block != null) {
            String n = block.getUnlocalizedName().substring(5);
            GameRegistry.registerBlock(block, n);
            block.setCreativeTab(tabEndExpansion);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerWorldGenerator(new WorldGenManager(), 10);
        MinecraftForge.EVENT_BUS.register(new EndEventHandler());
        proxy.registerRenderers();
    }
}
