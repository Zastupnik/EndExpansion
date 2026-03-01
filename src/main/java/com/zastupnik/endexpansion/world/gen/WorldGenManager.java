package com.zastupnik.endexpansion.world.gen;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.biome.BiomeGenBase;
import com.zastupnik.endexpansion.world.biome.EndBiomes;
import com.zastupnik.endexpansion.handler.ConfigHandler;

import java.util.*;

public class WorldGenManager implements IWorldGenerator {

    public static final EndIslandGenerator islandGen = new EndIslandGenerator();

    // Безопасная зона вокруг спавна дракона
    private static final int SAFE_ZONE = 1000;

    // Минимальное расстояние между центрами кластеров (в чанках).
    private static final int MIN_CLUSTER_SPACING_CHUNKS = 14;

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.dimensionId == 1) {
            generateEnd(world, chunkX, chunkZ);
        }
    }

    private void generateEnd(World world, int chunkX, int chunkZ) {
        // ── Шаг 1: Определяем ячейку сетки для этого чанка ──────────────────────
        // Разбиваем мир на ячейки MIN_CLUSTER_SPACING_CHUNKS×MIN_CLUSTER_SPACING_CHUNKS чанков.
        // В каждой ячейке может быть максимум один кластер.
        // Это гарантирует расстояние между кластерами БЕЗ runtime-реестра.
        int spacing = MIN_CLUSTER_SPACING_CHUNKS;
        int cellX = Math.floorDiv(chunkX, spacing);
        int cellZ = Math.floorDiv(chunkZ, spacing);

        // Этот чанк должен быть "хозяином" своей ячейки — то есть первым чанком в ней
        int ownerChunkX = cellX * spacing;
        int ownerChunkZ = cellZ * spacing;
        if (chunkX != ownerChunkX || chunkZ != ownerChunkZ) return;

        // ── Шаг 2: Детерминированный рандом для этой ячейки ─────────────────────
        // Используем сид мира + координаты ячейки → результат одинаков при любом порядке загрузки
        Random rand = new Random(world.getSeed()
                ^ ((long) cellX * 341873128712L)
                ^ ((long) cellZ * 132897987541L));

        // ── Шаг 3: Редкость — только часть ячеек содержит острова ───────────────
        // 1 из 3 ячеек: меньше пиковых лагов от одновременной генерации тяжёлых кластеров.
        if (rand.nextInt(3) != 0) return;

        // ── Шаг 4: Позиция кластера внутри ячейки (случайно смещена) ────────────
        int offsetX = rand.nextInt(spacing * 16); // случайный сдвиг в блоках внутри ячейки
        int offsetZ = rand.nextInt(spacing * 16);
        int blockX  = ownerChunkX * 16 + offsetX;
        int blockZ  = ownerChunkZ * 16 + offsetZ;

        // Безопасная зона вокруг спавна
        if (Math.abs(blockX) < SAFE_ZONE && Math.abs(blockZ) < SAFE_ZONE) return;

        // ── Шаг 5: Выбор биома с защитой от null ────────────────────────────────
        BiomeGenBase biome = selectBiome(rand);
        if (biome == null) return; // EndBiomes ещё не проинициализированы — пропускаем

        // ── Шаг 6: Размер и количество островов ─────────────────────────────────
        // Большие архипелаги: чаще несколько островов, чтобы мир был насыщеннее.
        int islandCount;
        int countRoll = rand.nextInt(16);
        if (countRoll < 6) {
            islandCount = 2;
        } else if (countRoll < 12) {
            islandCount = 3;
        } else if (countRoll < 15) {
            islandCount = 4;
        } else {
            islandCount = 5;
        }

        islandGen.generateCluster(world, rand, blockX, blockZ, biome, islandCount, getRadius(biome, rand));
    }

    // ===== БИОМЫ =====

    /**
     * Выбор биома с явной проверкой на null.
     * Если EndBiomes не инициализированы — вернём null и пропустим генерацию.
     */
    private BiomeGenBase selectBiome(Random rand) {
        // Проверяем что хотя бы один биом инициализирован
        if (EndBiomes.biomeForest == null) return null;

        int roll = rand.nextInt(21);

        // Веса: Forest=5, Jungle=4, Desert=4, Ocean=3, Cemetery=2, Infection=2, Fortress=1
        if (roll < 5)  return safeBiome(EndBiomes.biomeForest,    EndBiomes.biomeDesert);
        if (roll < 9)  return safeBiome(EndBiomes.biomeJungle,    EndBiomes.biomeForest);
        if (roll < 13) return safeBiome(EndBiomes.biomeDesert,    EndBiomes.biomeForest);
        if (roll < 16) return safeBiome(EndBiomes.biomeOcean,     EndBiomes.biomeForest);
        if (roll < 18) return safeBiome(EndBiomes.biomeCemetery,  EndBiomes.biomeForest);
        if (roll < 20) return safeBiome(EndBiomes.biomeInfection, EndBiomes.biomeForest);
        return              safeBiome(EndBiomes.biomeFortress,  EndBiomes.biomeForest);
    }

    /** Возвращает primary если не null, иначе fallback. */
    private BiomeGenBase safeBiome(BiomeGenBase primary, BiomeGenBase fallback) {
        return primary != null ? primary : fallback;
    }

    private int getRadius(BiomeGenBase biome, Random rand) {
        if (biome == EndBiomes.biomeCemetery)  return 58 + rand.nextInt(27); // 58–84
        if (biome == EndBiomes.biomeFortress)  return 54 + rand.nextInt(23); // 54–76
        if (biome == EndBiomes.biomeJungle)    return 56 + rand.nextInt(25); // 56–80
        if (biome == EndBiomes.biomeForest)    return 54 + rand.nextInt(25); // 54–78
        if (biome == EndBiomes.biomeOcean)     return 54 + rand.nextInt(25); // 54–78
        if (biome == EndBiomes.biomeDesert)    return 56 + rand.nextInt(25); // 56–80
        if (biome == EndBiomes.biomeInfection) return 48 + rand.nextInt(21); // 48–68
        return 54 + rand.nextInt(23);
    }
}
