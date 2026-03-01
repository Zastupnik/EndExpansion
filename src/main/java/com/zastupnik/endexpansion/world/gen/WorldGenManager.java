package com.zastupnik.endexpansion.world.gen;

import cpw.mods.fml.common.IWorldGenerator;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.biome.BiomeGenBase;
import com.zastupnik.endexpansion.world.biome.EndBiomes;
import com.zastupnik.endexpansion.handler.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import java.util.*;

public class WorldGenManager implements IWorldGenerator {

    public static final EndIslandGenerator islandGen = new EndIslandGenerator();

    private static final int SAFE_ZONE = 1000;

    // Реестр размещённых островов для проверки пересечений.
    // Ключ: регион (128×128 блоков), значение: список [x, z, radius].
    private static final Map<Long, List<int[]>> placedIslands = new HashMap<>();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world,
                         IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        if (world.provider.dimensionId == 1) {
            generateEnd(world, chunkX, chunkZ);
        }
    }

    private void generateEnd(World world, int chunkX, int chunkZ) {
        int blockX = chunkX * 16 + 8;
        int blockZ = chunkZ * 16 + 8;

        if (Math.abs(blockX) < SAFE_ZONE && Math.abs(blockZ) < SAFE_ZONE) return;

        // Детерминированный рандом: одинаковый при любом порядке загрузки чанков
        Random rand = new Random(world.getSeed()
                ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L));

        BiomeGenBase biome = selectBiome(rand);
        if (rand.nextInt(ConfigHandler.getSpawnChance(biome)) != 0) return;

        // 30% шанс — кластер (2–4 острова рядом), 70% — одиночный
        if (rand.nextInt(10) < 3) {
            generateCluster(world, rand, blockX, blockZ, biome);
        } else {
            tryPlaceIsland(world, rand, blockX, blockZ, biome);
        }
    }

    // ===== КЛАСТЕР =====

    /**
     * Группа из 2–4 островов одного биома в радиусе 100–250 блоков.
     * Между ними потенциально будут мосты (если DecoratorBridge будет добавлен).
     */
    private void generateCluster(World world, Random rand, int cx, int cz, BiomeGenBase biome) {
        int count = 2 + rand.nextInt(3); // 2–4
        int clusterRadius = 100 + rand.nextInt(150); // 100–250 блоков

        List<int[]> clusterIslands = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            float angle = rand.nextFloat() * (float)(Math.PI * 2);
            float dist  = rand.nextFloat() * clusterRadius;
            int ix = cx + (int)(Math.cos(angle) * dist);
            int iz = cz + (int)(Math.sin(angle) * dist);

            int[] placed = tryPlaceIsland(world, rand, ix, iz, biome);
            if (placed != null) clusterIslands.add(placed);
        }

        buildBridges(world, rand, clusterIslands);
    }


    private void buildBridges(World world, Random rand, List<int[]> clusterIslands) {
        if (!ConfigHandler.enableBridges || clusterIslands.size() < 2) return;

        for (int i = 0; i < clusterIslands.size(); i++) {
            int[] a = clusterIslands.get(i);
            for (int j = i + 1; j < clusterIslands.size(); j++) {
                int[] b = clusterIslands.get(j);

                int dx = b[0] - a[0];
                int dz = b[1] - a[1];
                int centerDist = (int) Math.sqrt(dx * dx + dz * dz);
                int edgeDist = centerDist - a[2] - b[2];
                if (edgeDist < 8 || edgeDist > ConfigHandler.bridgeMaxDistance) continue;
                if (rand.nextInt(100) > 55) continue;

                int startX = a[0] + (int) (dx / (double) centerDist * (a[2] - 3));
                int startZ = a[1] + (int) (dz / (double) centerDist * (a[2] - 3));
                int endX = b[0] - (int) (dx / (double) centerDist * (b[2] - 3));
                int endZ = b[1] - (int) (dz / (double) centerDist * (b[2] - 3));
                int y = Math.max(35, Math.min(140, (int) ((world.getTopSolidOrLiquidBlock(startX, startZ) + world.getTopSolidOrLiquidBlock(endX, endZ)) / 2.0)));
                placeBridge(world, startX, startZ, endX, endZ, y);
            }
        }
    }

    private void placeBridge(World world, int x1, int z1, int x2, int z2, int y) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        int steps = Math.max(Math.abs(dx), Math.abs(dz));
        if (steps <= 0) return;

        Block floor = ConfigHandler.bridgeWitheredPlanks ? com.zastupnik.endexpansion.EndExpansion.witheredLog : Blocks.end_stone;

        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int bx = x1 + Math.round(dx * t);
            int bz = z1 + Math.round(dz * t);
            int by = y + (int) (Math.sin(t * Math.PI) * 2);

            world.setBlock(bx, by, bz, floor, 0, 2);
            world.setBlock(bx, by - 1, bz, floor, 0, 2);

            if (i % 4 == 0) {
                world.setBlock(bx + 1, by + 1, bz, floor, 0, 2);
                world.setBlock(bx - 1, by + 1, bz, floor, 0, 2);
            }
        }
    }

    /**
     * Пытается разместить один остров. Возвращает [x, z, radius] если успешно, null если нет.
     */
    private int[] tryPlaceIsland(World world, Random rand, int x, int z, BiomeGenBase biome) {
        int radius = getRadius(biome, rand);

        // Небольшое случайное смещение внутри чанка
        x += rand.nextInt(32) - 16;
        z += rand.nextInt(32) - 16;

        int minGap = ConfigHandler.minIslandGap;
        if (overlaps(x, z, radius, minGap)) return null;

        registerIsland(x, z, radius);
        islandGen.generateIsland(world, rand, x, z, biome, radius);
        return new int[]{x, z, radius};
    }

    // ===== ПРОВЕРКА ПЕРЕСЕЧЕНИЙ =====

    private boolean overlaps(int x, int z, int radius, int minGap) {
        int regionX = x >> 7;
        int regionZ = z >> 7;

        // Проверяем 5×5 соседних регионов — покрывает крупные острова
        for (int rx = regionX - 2; rx <= regionX + 2; rx++) {
            for (int rz = regionZ - 2; rz <= regionZ + 2; rz++) {
                List<int[]> islands = placedIslands.get(regionKey(rx, rz));
                if (islands == null) continue;
                for (int[] island : islands) {
                    int dx = island[0] - x;
                    int dz = island[1] - z;
                    int dist = (int)Math.sqrt(dx * dx + dz * dz);
                    if (dist < island[2] + radius + minGap) return true;
                }
            }
        }
        return false;
    }

    private void registerIsland(int x, int z, int radius) {
        long key = regionKey(x >> 7, z >> 7);
        placedIslands.computeIfAbsent(key, k -> new ArrayList<>()).add(new int[]{x, z, radius});
    }

    private long regionKey(int rx, int rz) {
        return ((long)(rx + 30000)) << 32 | ((rz + 30000) & 0xFFFFFFFFL);
    }

    // ===== БИОМЫ И РАЗМЕРЫ =====

    /**
     * Радиус — это половина наименьшего измерения острова.
     * Формы вытягиваются в generateShape через scaleX/scaleZ,
     * поэтому реальный размер может быть 2–3× от radius.
     * Пустыня 300×200 = radius ~100–150 при scaleX=2.0, scaleZ=1.3.
     */
    private int getRadius(BiomeGenBase biome, Random rand) {
        if (biome == EndBiomes.biomeCemetery)  return ConfigHandler.getRandomAmount(rand, ConfigHandler.cemeteryRadiusMin, Math.min(150, ConfigHandler.cemeteryRadiusMax));
        if (biome == EndBiomes.biomeFortress)  return ConfigHandler.getRandomAmount(rand, ConfigHandler.fortressRadiusMin, ConfigHandler.fortressRadiusMax);
        if (biome == EndBiomes.biomeJungle)    return ConfigHandler.getRandomAmount(rand, ConfigHandler.jungleRadiusMin, ConfigHandler.jungleRadiusMax);
        if (biome == EndBiomes.biomeForest)    return ConfigHandler.getRandomAmount(rand, ConfigHandler.forestRadiusMin, ConfigHandler.forestRadiusMax);
        if (biome == EndBiomes.biomeOcean)     return ConfigHandler.getRandomAmount(rand, ConfigHandler.oceanRadiusMin, ConfigHandler.oceanRadiusMax);
        if (biome == EndBiomes.biomeDesert)    return ConfigHandler.getRandomAmount(rand, ConfigHandler.desertRadiusMin, ConfigHandler.desertRadiusMax);
        if (biome == EndBiomes.biomeInfection) return ConfigHandler.getRandomAmount(rand, ConfigHandler.infectionRadiusMin, ConfigHandler.infectionRadiusMax);
        return 40 + rand.nextInt(60);
    }

    private BiomeGenBase selectBiome(Random rand) {
        // Веса: Forest=5, Jungle=4, Desert=4, Ocean=3, Cemetery=2, Infection=2, Fortress=1
        int roll = rand.nextInt(21);
        if (roll < 5)       return EndBiomes.biomeForest;
        else if (roll < 9)  return EndBiomes.biomeJungle;
        else if (roll < 13) return EndBiomes.biomeDesert;
        else if (roll < 16) return EndBiomes.biomeOcean;
        else if (roll < 18) return EndBiomes.biomeCemetery;
        else if (roll < 20) return EndBiomes.biomeInfection;
        else                return EndBiomes.biomeFortress;
    }
}