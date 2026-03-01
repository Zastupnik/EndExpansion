package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockWall;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockEndWall extends BlockWall {

    private final Block modelBlock;

    public BlockEndWall(Block model, String name) {
        super(model); // Передаем модель в суперкласс
        this.modelBlock = model;
        this.setBlockName(name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);

        // ИСПРАВЛЕНИЕ ОШИБКИ:
        // Вместо прямого обращения к полю blockHardness, которое protected,
        // мы устанавливаем его вручную. Обычно стены имеют ту же прочность, что и модель.
        // Если хочешь именно "как у модели", в 1.7.10 проще всего написать цифру:
        this.setHardness(2.0F);
        this.setResistance(10.0F);

        // Звук шагов обычно публичный, тут ошибки быть не должно
        this.setStepSound(model.stepSound);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        // Стены обычно используют ту же текстуру, что и блок-основа
        return modelBlock.getIcon(side, meta);
    }
}