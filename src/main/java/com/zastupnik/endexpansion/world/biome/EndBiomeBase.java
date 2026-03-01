package com.zastupnik.endexpansion.world.biome;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.biome.BiomeGenEnd;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.entity.monster.EntityEnderman;

/**
 * Базовый класс для всех биомов расширенного Энда.
 */
public class EndBiomeBase extends BiomeGenEnd {

    private int skyColor;
    private int fogColor;

    public EndBiomeBase(int id, String name, int skyColor, int fogColor) {
        super(id);
        this.setBiomeName(name);
        this.skyColor = skyColor;
        this.fogColor = fogColor;

        // Очищаем стандартные списки спавна, чтобы Эндермены не были единственными жителями
        this.spawnableMonsterList.clear();
        this.spawnableCreatureList.clear();
        this.spawnableWaterCreatureList.clear();

        // Добавляем Эндермена по умолчанию (можно убрать для специфических биомов)
        this.spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(EntityEnderman.class, 10, 4, 4));
    }

    /**
     * Устанавливает кастомный цвет неба для этого биома.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public int getSkyColorByTemp(float temp) {
        return this.skyColor;
    }

    /**
     * В 1.7.10 для кастомного цвета тумана часто используются эвенты,
     * но мы подготовим переменную здесь для удобства.
     */
    @SideOnly(Side.CLIENT)
    public int getFogColor() {
        return this.fogColor;
    }

    /**
     * Хелпер для быстрой регистрации мобов.
     * @param weight Вероятность спавна
     * @param min Минимальное количество в группе
     * @param max Максимальное количество в группе
     */
    public EndBiomeBase addCustomMonster(Class entityClass, int weight, int min, int max) {
        this.spawnableMonsterList.add(new BiomeGenBase.SpawnListEntry(entityClass, weight, min, max));
        return this;
    }
}