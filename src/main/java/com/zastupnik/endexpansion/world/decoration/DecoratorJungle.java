package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import com.zastupnik.endexpansion.world.gen.EndIslandGenerator;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import java.util.Random;

public class DecoratorJungle implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {
        int groundedCenterY = Math.max(40, world.getTopSolidOrLiquidBlock(centerX, centerZ));

        // 1. Сначала генерируем деревья — мосты будут между ними
        int treeCount = 6 + rand.nextInt(5);
        int[][] treePositions = new int[treeCount][3];

        for (int i = 0; i < treeCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius - 10);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            if (world.getBlock(pos[0], y - 1, pos[1]) == EndExpansion.jungleTurf) {
                EndIslandGenerator gen = new EndIslandGenerator();
                gen.generateJungleTree(world, rand, pos[0], y, pos[1]);
                treePositions[i] = new int[]{pos[0], y + 12 + rand.nextInt(6), pos[1]};
            }
        }

        // 2. Подвесные мосты между деревьями
        for (int i = 0; i < treeCount - 1; i++) {
            if (treePositions[i] == null || treePositions[i + 1] == null) continue;
            if (rand.nextInt(3) != 0) { // Не все деревья соединены
                generateSuspensionBridge(world, rand,
                        treePositions[i][0],     treePositions[i][1],     treePositions[i][2],
                        treePositions[i+1][0],   treePositions[i+1][1],   treePositions[i+1][2]);
            }
        }

        // 3. Водопады — 2-3 штуки
        int waterfallCount = 2 + rand.nextInt(2);
        for (int i = 0; i < waterfallCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius / 2);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            generateWaterfall(world, rand, pos[0], y, pos[1]);
        }

        generateJungleUndergrowth(world, rand, centerX, centerZ, radius);
    }

    private void generateJungleUndergrowth(World world, Random rand, int centerX, int centerZ, int radius) {
        int plants = 18 + rand.nextInt(12);
        for (int i = 0; i < plants; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            if (world.getBlock(pos[0], y - 1, pos[1]) != EndExpansion.jungleTurf) continue;
            if (rand.nextInt(3) == 0) {
                setIfAir(world, pos[0], y, pos[1], EndExpansion.spectralRose);
            } else {
                setIfAir(world, pos[0], y, pos[1], EndExpansion.witheredLeaves);
            }
        }
    }

    // ===== ПОДВЕСНОЙ МОСТ =====

    /**
     * Мост между двумя точками с провисающим настилом.
     * Верёвки (заборы) по бокам, настил из досок.
     */
    private void generateSuspensionBridge(World world, Random rand,
                                          int x1, int y1, int z1,
                                          int x2, int y2, int z2) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dz * dz);

        if (length > 40 || length < 5) return; // Слишком далеко или близко

        int steps = (int) length;

        // Опорные платформы на деревьях
        buildTreePlatform(world, x1, y1, z1);
        buildTreePlatform(world, x2, y2, z2);

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;

            int bx = x1 + (int)(dx * t);
            int bz = z1 + (int)(dz * t);

            // Провисание моста — параболическая кривая
            float sag = (float)(Math.sin(Math.PI * t) * length * 0.08F);
            int by = (int)(y1 + (y2 - y1) * t - sag);

            // Настил
            world.setBlock(bx, by, bz, EndExpansion.witheredLog, 0, 2);

            // Перила по бокам (перпендикулярно направлению моста)
            int perpX = (int)(-dz / length);
            int perpZ = (int)( dx / length);

            setIfAir(world, bx + perpX, by + 1, bz + perpZ, EndExpansion.ashenStone); // TODO: fence
            setIfAir(world, bx - perpX, by + 1, bz - perpZ, EndExpansion.ashenStone);

            // Верёвки (тросы) — столбики через каждые 4 блока
            if (i % 4 == 0) {
                int cableHeight = (int)(sag * 0.5F) + 2;
                for (int cy = 1; cy <= cableHeight; cy++) {
                    setIfAir(world, bx + perpX, by + cy + 1, bz + perpZ, EndExpansion.ashenStone);
                    setIfAir(world, bx - perpX, by + cy + 1, bz - perpZ, EndExpansion.ashenStone);
                }
            }

            // Факел на каждом 5-м блоке
            if (i % 5 == 0) {
                setIfAir(world, bx, by + 1, bz, EndExpansion.endTorch);
            }
        }
    }

    /**
     * Платформа на дереве — площадка где начинается мост.
     */
    private void buildTreePlatform(World world, int x, int y, int z) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setIfAir(world, x + dx, y - 1, z + dz, EndExpansion.witheredLog);
            }
        }
        // Перила платформы
        for (int dx = -1; dx <= 1; dx++) {
            setIfAir(world, x + dx, y, z - 1, EndExpansion.ashenStone);
            setIfAir(world, x + dx, y, z + 1, EndExpansion.ashenStone);
        }
        for (int dz = -1; dz <= 1; dz++) {
            setIfAir(world, x - 1, y, z + dz, EndExpansion.ashenStone);
            setIfAir(world, x + 1, y, z + dz, EndExpansion.ashenStone);
        }
        // Сундук на платформе — иногда
    }

    // ===== ВОДОПАД =====

    /**
     * Водопад — вода падает с уступа, внизу небольшой бассейн.
     * Уступ строится из камня, вода льётся вниз.
     */
    private void generateWaterfall(World world, Random rand, int x, int y, int z) {
        int height = 5 + rand.nextInt(6); // 5-10 блоков высота

        // Уступ из камня
        int ledgeW = 3 + rand.nextInt(3);
        for (int dx = 0; dx < ledgeW; dx++) {
            for (int dy = 0; dy < 3; dy++) {
                world.setBlock(x + dx, y + height + dy, z, EndExpansion.jungleTurf,  0, 2);
                world.setBlock(x + dx, y + height + dy, z - 1, EndExpansion.ashenStone, 0, 2);
                world.setBlock(x + dx, y + height + dy, z - 2, EndExpansion.ashenStone, 0, 2);
            }
            // Вода льётся с края уступа
            world.setBlock(x + dx, y + height, z + 1, Blocks.flowing_water, 0, 2);
        }

        // Поток воды вниз
        for (int dy = 0; dy < height; dy++) {
            for (int dx = 0; dx < ledgeW; dx++) {
                Block current = world.getBlock(x + dx, y + dy, z + 1);
                if (current == Blocks.air || current == EndExpansion.jungleTurf) {
                    world.setBlock(x + dx, y + dy, z + 1, Blocks.flowing_water, 0, 2);
                }
            }
        }

        // Бассейн внизу
        int poolRadius = 2 + rand.nextInt(2);
        for (int dx = -poolRadius; dx <= poolRadius; dx++) {
            for (int dz = -poolRadius; dz <= poolRadius; dz++) {
                if (dx * dx + dz * dz <= poolRadius * poolRadius) {
                    world.setBlock(x + ledgeW/2 + dx, y - 1, z + 1 + dz, Blocks.water,          0, 2);
                    world.setBlock(x + ledgeW/2 + dx, y - 2, z + 1 + dz, EndExpansion.jungleTurf, 0, 2);
                }
            }
        }

        // Растения вокруг бассейна
        for (int dx = -poolRadius - 1; dx <= poolRadius + 1; dx++) {
            for (int dz = -1; dz <= poolRadius + 2; dz++) {
                if (rand.nextInt(3) == 0) {
                    int tx = x + ledgeW/2 + dx;
                    int tz = z + 1 + dz;
                    int ty = world.getTopSolidOrLiquidBlock(tx, tz);
                    if (world.getBlock(tx, ty - 1, tz) == EndExpansion.jungleTurf) {
                        world.setBlock(tx, ty, tz, EndExpansion.spectralRose, 0, 2);
                    }
                }
            }
        }

        // Кристаллы у водопада
        for (int i = 0; i < 3; i++) {
            int cx = x + rand.nextInt(ledgeW + 2) - 1;
            int cz = z + rand.nextInt(3);
            int cy = world.getTopSolidOrLiquidBlock(cx, cz);
            setIfAir(world, cx, cy, cz, EndExpansion.seaCrystal);
        }
    }

    // ===== УТИЛИТЫ =====

    private void setIfAir(World world, int x, int y, int z, Block block) {
        if (world.isAirBlock(x, y, z)) {
            world.setBlock(x, y, z, block, 0, 2);
        }
    }

    private int[] randomPos(Random rand, int centerX, int centerZ, int radius) {
        int angle = rand.nextInt(360);
        int dist  = 5 + rand.nextInt(Math.max(1, radius - 5));
        return new int[]{
                centerX + (int)(Math.cos(Math.toRadians(angle)) * dist),
                centerZ + (int)(Math.sin(Math.toRadians(angle)) * dist)
        };
    }
}