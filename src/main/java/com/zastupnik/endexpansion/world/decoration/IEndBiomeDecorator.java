package com.zastupnik.endexpansion.world.decoration;

import net.minecraft.world.World;
import java.util.Random;

public interface IEndBiomeDecorator {
    /**
     * Вызывается после генерации острова.
     * @param world   Мир
     * @param rand    Рандом
     * @param centerX Центр острова X
     * @param centerY Поверхность острова Y
     * @param centerZ Центр острова Z
     * @param radius  Радиус острова
     */
    void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius);
}