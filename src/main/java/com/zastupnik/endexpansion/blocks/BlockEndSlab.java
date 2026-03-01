package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.Random;

public class BlockEndSlab extends BlockSlab {

    private final Block modelBlock;
    private final BlockEndSlab singleSlab; // null если это одиночная плита

    // Конструктор одиночной плиты
    public BlockEndSlab(Block model, String name, float hardness, float resist) {
        super(false, model.getMaterial());
        this.modelBlock = model;
        this.singleSlab = null;
        init(model, name, hardness, resist, true);
    }

    // Конструктор двойной плиты
    public BlockEndSlab(Block model, String name, float hardness, float resist, BlockEndSlab single) {
        super(true, model.getMaterial());
        this.modelBlock = model;
        this.singleSlab = single;
        init(model, name, hardness, resist, false);
    }

    private void init(Block model, String name, float hardness, float resist, boolean addToTab) {
        this.setBlockName(name);
        this.setHardness(hardness);
        this.setResistance(resist);
        this.setStepSound(model.stepSound);
        this.useNeighborBrightness = true;
        if (addToTab) this.setCreativeTab(EndExpansion.tabEndExpansion);
    }

    @Override
    public String func_150002_b(int meta) {
        return super.getUnlocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getItem(World world, int x, int y, int z) {
        // Всегда возвращаем одиночную плиту
        return singleSlab != null ? Item.getItemFromBlock(singleSlab) : Item.getItemFromBlock(this);
    }

    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        // Двойная плита дропает одиночную
        return singleSlab != null ? Item.getItemFromBlock(singleSlab) : Item.getItemFromBlock(this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return modelBlock.getIcon(side, meta & 7);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(net.minecraft.client.renderer.texture.IIconRegister reg) {
        // Используем иконки модели
    }
}