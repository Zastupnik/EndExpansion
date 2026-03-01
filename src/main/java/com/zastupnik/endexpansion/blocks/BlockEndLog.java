package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLog;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class BlockEndLog extends BlockLog {

    @SideOnly(Side.CLIENT)
    private IIcon sideIcon;
    @SideOnly(Side.CLIENT)
    private IIcon topIcon;

    public BlockEndLog(String name) {
        super();
        this.setBlockName(name);
        this.setBlockTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);
        this.setHardness(2.0F);
        this.setStepSound(soundTypeWood);
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getSideIcon(int meta) {
        // meta & 3 — тип дерева, meta >> 2 — ориентация
        // У нас один тип, поэтому просто возвращаем sideIcon
        return this.sideIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    protected IIcon getTopIcon(int meta) {
        return this.topIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        // Текстуры должны называться например: "withered_log_side" и "withered_log_top"
        this.sideIcon = reg.registerIcon(this.getTextureName() + "_side");
        this.topIcon = reg.registerIcon(this.getTextureName() + "_top");
    }
}