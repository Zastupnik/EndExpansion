package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLeaves;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class BlockEndLeaves extends BlockLeaves {

    public BlockEndLeaves(String name) {
        super();
        this.setBlockName(name);
        this.setBlockTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);
        this.setHardness(0.2F);
        this.setStepSound(soundTypeGrass);
        this.setLightOpacity(1);
    }

    @Override
    public String[] func_150125_e() {
        return new String[] { this.getUnlocalizedName() };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.blockIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.blockIcon = reg.registerIcon(this.getTextureName());
    }

    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        // Шанс дропа саженца 1/20, растёт с fortune
        if (rand.nextInt(Math.max(1, 20 >> fortune)) == 0) {
            // TODO: заменить на EndExpansion.endSapling когда будет готов
            return Item.getItemFromBlock(Blocks.sapling);
        }
        return null;
    }

    // Фиолетовый цвет листьев — энд-атмосфера
    @Override
    public int getRenderColor(int meta) {
        return 0xCC88FF;
    }

    @Override
    public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
        return 0xCC88FF;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
        return true;
    }
}