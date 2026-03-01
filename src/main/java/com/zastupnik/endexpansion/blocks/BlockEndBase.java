package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;

import java.util.List;
import java.util.Random;

public class BlockEndBase extends Block {

    private boolean glowing = false;         // Светится ли блок
    private boolean unbreakable = false;     // Нельзя сломать (для структур)
    private int customLight = 0;             // Уровень свечения 0-15
    private String customDrop = null;        // Кастомный дроп (имя блока/предмета)

    public BlockEndBase(Material mat, String name, SoundType sound, float hardness, float resist) {
        super(mat);
        this.setBlockName(name);
        this.setBlockTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);
        this.setStepSound(sound);
        this.setHardness(hardness);
        this.setResistance(resist);
        configureMiningLevel(mat);
    }

    // ===== BUILDER-МЕТОДЫ для удобной настройки =====

    /**
     * Делает блок светящимся
     * Использование: new BlockEndBase(...).setGlowing(8)
     */
    public BlockEndBase setGlowing(int lightLevel) {
        this.customLight = Math.min(15, Math.max(0, lightLevel));
        this.glowing = true;
        return this;
    }

    /**
     * Блок нельзя сломать — для основ структур, боссовых арен
     */
    public BlockEndBase setUnbreakable() {
        this.unbreakable = true;
        this.setHardness(-1.0F); // -1 = неломаемый в Minecraft
        return this;
    }

    /**
     * Кастомный дроп (например, заражённый блок дропает слизь)
     */
    public BlockEndBase setCustomDrop(String dropName) {
        this.customDrop = dropName;
        return this;
    }

    /**
     * Блок не попадает в инвентарь в creative (для технических блоков структур)
     */
    public BlockEndBase hideFromCreative() {
        this.setCreativeTab(null);
        return this;
    }

    // ===== ПЕРЕОПРЕДЕЛЕНИЯ =====

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return glowing ? customLight : super.getLightValue(world, x, y, z);
    }

    @Override
    public int getLightValue() {
        return glowing ? customLight : super.getLightValue();
    }

    /**
     * Если задан кастомный дроп — дропаем его
     * Пример: заражённый камень -> споры заражения
     */
    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        if (customDrop != null) {
            Block dropBlock = Block.getBlockFromName(customDrop);
            if (dropBlock != null) {
                return Item.getItemFromBlock(dropBlock);
            }
        }
        return super.getItemDropped(meta, rand, fortune);
    }

    @Override
    public boolean isOpaqueCube() {
        return true;
    }

    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

    // ===== УТИЛИТЫ =====

    private void configureMiningLevel(Material mat) {
        if (mat == Material.rock) {
            this.setHarvestLevel("pickaxe", 1);
        } else if (mat == Material.wood) {
            this.setHarvestLevel("axe", 0);
        } else if (mat == Material.grass || mat == Material.ground || mat == Material.sand) {
            this.setHarvestLevel("shovel", 0);
        } else if (mat == Material.iron) {
            this.setHarvestLevel("pickaxe", 2);
        } else if (mat == Material.glass) {
            this.setHarvestLevel("pickaxe", 0);
        }
    }

    /**
     * Удобный метод для регистрации — используем в BlockRegistry
     */
    public String getUnlocalizedName() {
        return super.getUnlocalizedName();
    }
}