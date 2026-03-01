package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;

/**
 * Класс для сыпучих блоков (Эндо-песок, гравий Заражения).
 */
public class BlockEndFalling extends BlockFalling {

    public BlockEndFalling(Material mat, String name, SoundType sound, float hardness, float resist) {
        super(mat);
        this.setBlockName(name);
        this.setBlockTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);
        this.setStepSound(sound);
        this.setHardness(hardness);
        this.setResistance(resist);

        // Для песка всегда лопата
        this.setHarvestLevel("shovel", 0);
    }

    /**
     * Можно добавить частицы при падении, если захочешь кастомный эффект
     */
    @Override
    public void onBlockAdded(net.minecraft.world.World world, int x, int y, int z) {
        super.onBlockAdded(world, x, y, z);
    }
}