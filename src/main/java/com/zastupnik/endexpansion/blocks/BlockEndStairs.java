package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;

public class BlockEndStairs extends BlockStairs {

    /**
     * @param model Блок-образец (например, доски или кирпичи)
     * @param name  Имя блока
     */
    public BlockEndStairs(Block model, String name) {
        // Вызываем конструктор суперкласса.
        // BlockStairs в 1.7.10 САМ забирает прочность и звук у модели.
        super(model, 0);

        this.setBlockName(name);

        // В 1.7.10 ступеньки по умолчанию непрозрачные,
        // поэтому фиксим освещение соседа
        this.useNeighborBrightness = true;

        // Добавляем во вкладку мода
        this.setCreativeTab(EndExpansion.tabEndExpansion);
    }
}