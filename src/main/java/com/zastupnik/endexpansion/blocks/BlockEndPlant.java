package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Универсальный класс для всей мелкой растительности (цветы, грибы, кусты).
 */
public class BlockEndPlant extends BlockBush {

    public BlockEndPlant(String name) {
        super(Material.plants);
        this.setBlockName(name);
        this.setBlockTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);
        this.setStepSound(soundTypeGrass);
        // Свечение убрали — задаём через setLightLevel() снаружи если нужно
    }

    /**
     * Проверка: на чем может расти это растение?
     * По умолчанию BlockBush растет только на обычной земле/траве.
     * Мы добавим сюда наши кастомные блоки почвы.
     */
    @Override
    protected boolean canPlaceBlockOn(Block ground) {
        return ground == Blocks.end_stone ||
                ground == EndExpansion.deadGrass ||
                ground == EndExpansion.forestMoss ||
                ground == EndExpansion.jungleTurf ||
                ground == EndExpansion.infestedMycelium ||
                ground == EndExpansion.endSand;
    }

    /**
     * Метод для обновления состояния (например, если блок под растением исчез)
     */
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        super.onNeighborBlockChange(world, x, y, z, neighbor);
        this.checkAndDropBlock(world, x, y, z);
    }
}