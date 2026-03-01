package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

/**
 * Универсальный класс для всех заборов и оград мода.
 */
public class BlockEndFence extends BlockFence {

    /**
     * @param name Название блока (используется и для регистрации, и для текстуры)
     * @param mat  Материал (Material.wood для дерева, Material.rock для каменных стен)
     */
    public BlockEndFence(String name, Material mat) {
        // В 1.7.10 первый аргумент конструктора суперкласса - имя текстуры для иконки
        super("endexpansion:" + name, mat);
        this.setBlockName(name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);

        // Устанавливаем прочность в зависимости от материала
        if (mat == Material.rock) {
            this.setHardness(2.0F);
            this.setResistance(10.0F);
            this.setStepSound(soundTypeStone);
        } else {
            this.setHardness(2.0F);
            this.setResistance(5.0F);
            this.setStepSound(soundTypeWood);
        }
    }

    /**
     * Метод определяет, к каким блокам забор будет "присасываться".
     * Мы переопределяем его, чтобы наши заборы соединялись друг с другом
     * и с обычными полными блоками.
     */
    @Override
    public boolean canConnectFenceTo(IBlockAccess world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);

        // Соединяемся, если это такой же забор или любой забор из ваниллы
        if (block != this && block != Blocks.fence && block != Blocks.nether_brick_fence) {
            // Также соединяемся с любыми нашими кастомными заборами из EndExpansion
            if (block instanceof BlockEndFence) {
                return true;
            }
            // Соединяемся с полными непрозрачными блоками (стенами)
            return block.getMaterial().isOpaque() && block.renderAsNormalBlock() && block.getMaterial() != Material.plants;
        } else {
            return true;
        }
    }
}