package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;

public class BlockEndGate extends BlockFenceGate {

    public BlockEndGate(Block model, String name) {
        super();
        this.setBlockName(name);
        // Вместо model.getTextureName(), который защищен,
        // мы используем "ручное" формирование пути к текстуре.
        // Обычно это "modid:имя_блока_модели"
        this.setBlockTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);

        // Копируем свойства модели
        this.setHardness(2.0F);
        this.setResistance(5.0F);
        this.setStepSound(model.stepSound);
    }
}