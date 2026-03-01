package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import com.zastupnik.endexpansion.handler.ConfigHandler;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import java.util.Random;

public class DecoratorForest implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {

        // 1. Руины — 2-4 штуки на остров
        int ruinCount = 2 + rand.nextInt(3);
        for (int i = 0; i < ruinCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            generateRuin(world, rand, pos[0], y, pos[1]);
        }

        // 2. Полые стволы — 3-5 штук
        int hollowCount = 3 + rand.nextInt(3);
        for (int i = 0; i < hollowCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            if (world.getBlock(pos[0], y - 1, pos[1]) == EndExpansion.forestMoss) {
                generateHollowLog(world, rand, pos[0], y, pos[1]);
            }
        }

        // 3. Хижины из пней — 1-2 штуки
        int hutCount = 1 + rand.nextInt(2);
        for (int i = 0; i < hutCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius / 2);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            if (world.getBlock(pos[0], y - 1, pos[1]) == EndExpansion.forestMoss) {
                generateStumpHut(world, rand, pos[0], y, pos[1]);
            }
        }
    }

    // ===== РУИНЫ =====

    /**
     * Остатки старой постройки — частично разрушенные стены, без крыши.
     * Размер случайный, стены неполные (дыры от времени).
     */
    private void generateRuin(World world, Random rand, int x, int y, int z) {
        int w = 5 + rand.nextInt(5); // 5-9
        int h = 2 + rand.nextInt(3); // 2-4
        int d = 5 + rand.nextInt(5);

        // Пол — частично сохранился
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                if (rand.nextInt(4) != 0) { // 75% блоков пола сохранилось
                    world.setBlock(x + dx, y - 1, z + dz, EndExpansion.ashenStone, 0, 2);
                }
            }
        }

        // Стены с дырами
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                // Передняя и задняя стены
                placeRuinBlock(world, rand, x + dx, y + dy, z,         EndExpansion.ashenStone);
                placeRuinBlock(world, rand, x + dx, y + dy, z + d - 1, EndExpansion.ashenStone);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                // Боковые стены
                placeRuinBlock(world, rand, x,         y + dy, z + dz, EndExpansion.ashenStone);
                placeRuinBlock(world, rand, x + w - 1, y + dy, z + dz, EndExpansion.ashenStone);
            }
        }

        // Мох и трава на руинах — заражение временем
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                if (rand.nextInt(5) == 0) {
                    int topY = world.getTopSolidOrLiquidBlock(x + dx, z + dz);
                    if (world.getBlock(x + dx, topY - 1, z + dz) == EndExpansion.ashenStone) {
                        world.setBlock(x + dx, topY, z + dz, EndExpansion.spectralRose, 0, 2);
                    }
                }
            }
        }

        // Иногда внутри руин — сундук
        if (rand.nextInt(3) == 0) {
            int cx = x + w / 2;
            int cz = z + d / 2;
            int cy = world.getTopSolidOrLiquidBlock(cx, cz);
            world.setBlock(cx, cy, cz, Blocks.chest, 0, 2);
            fillForestChest(world, rand, cx, cy, cz);
        }
    }

    /**
     * Ставит блок с шансом — чем выше стена, тем больше дыр.
     */
    private void placeRuinBlock(World world, Random rand, int x, int y, int z, Block block) {
        // Нижние блоки почти всегда есть, верхние — чаще отсутствуют
        int chance = 3 + y % 3; // Чем выше — тем больше шанс пропуска
        if (rand.nextInt(chance) != 0) {
            if (world.isAirBlock(x, y, z)) {
                world.setBlock(x, y, z, block, 0, 2);
            }
        }
    }

    // ===== ПОЛЫЙ СТВОЛ =====

    /**
     * Огромный лежащий полый ствол с сундуком внутри.
     */
    private void generateHollowLog(World world, Random rand, int x, int y, int z) {
        int length = 5 + rand.nextInt(5); // 5-9 блоков
        int dirX   = rand.nextBoolean() ? 1 : 0; // Направление ствола
        int dirZ   = dirX == 1 ? 0 : 1;

        // Внешняя оболочка ствола
        for (int i = 0; i < length; i++) {
            int bx = x + dirX * i;
            int bz = z + dirZ * i;

            // Ствол 2 блока высотой
            world.setBlock(bx, y,     bz, EndExpansion.ancientLog, 0, 2);
            world.setBlock(bx, y + 1, bz, EndExpansion.ancientLog, 0, 2);

            // Стенки ствола по бокам
            if (dirX == 1) {
                world.setBlock(bx, y, bz - 1, EndExpansion.ancientLog, 0, 2);
                world.setBlock(bx, y, bz + 1, EndExpansion.ancientLog, 0, 2);
            } else {
                world.setBlock(bx - 1, y, bz, EndExpansion.ancientLog, 0, 2);
                world.setBlock(bx + 1, y, bz, EndExpansion.ancientLog, 0, 2);
            }
        }

        // Полость внутри — воздух
        for (int i = 1; i < length - 1; i++) {
            world.setBlock(x + dirX * i, y + 1, z + dirZ * i, Blocks.air, 0, 2);
        }

        // Сундук внутри ствола
        int mid    = length / 2;
        int chestX = x + dirX * mid;
        int chestZ = z + dirZ * mid;
        world.setBlock(chestX, y + 1, chestZ, Blocks.chest, 0, 2);
        fillForestChest(world, rand, chestX, y + 1, chestZ);

        // Грибы на стволе
        for (int i = 0; i < length; i++) {
            if (rand.nextInt(3) == 0) {
                int fx = x + dirX * i;
                int fz = z + dirZ * i;
                if (world.getBlock(fx, y + 1, fz) == EndExpansion.forestMoss || world.getBlock(fx, y + 1, fz) == EndExpansion.deadGrass || world.getBlock(fx, y + 1, fz) == net.minecraft.init.Blocks.end_stone) {
                    world.setBlock(fx, y + 2, fz, EndExpansion.glowshroom, 0, 2);
                }
            }
        }
    }

    // ===== ХИЖИНА ИЗ ПНЕЙ =====

    /**
     * Маленькая хижина — стены из пней (bревно), крыша из листьев.
     * Внутри — сундук и факел.
     */
    private void generateStumpHut(World world, Random rand, int x, int y, int z) {
        int w = 5, h = 3, d = 5;

        // Пол из мха
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(x + dx, y - 1, z + dz, EndExpansion.forestMoss, 0, 2);
            }
        }

        // Стены из брёвен
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                setIfAir(world, x + dx, y + dy, z,         EndExpansion.witheredLog);
                setIfAir(world, x + dx, y + dy, z + d - 1, EndExpansion.witheredLog);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                setIfAir(world, x,         y + dy, z + dz, EndExpansion.witheredLog);
                setIfAir(world, x + w - 1, y + dy, z + dz, EndExpansion.witheredLog);
            }
        }

        // Дверной проём
        world.setBlock(x + w / 2, y,     z, Blocks.air, 0, 2);
        world.setBlock(x + w / 2, y + 1, z, Blocks.air, 0, 2);

        // Крыша из листьев (нависает на 1 блок)
        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                setIfAir(world, x + dx, y + h, z + dz, EndExpansion.ancientLeaves);
            }
        }

        // Факел внутри
        setIfAir(world, x + 1,     y + 2, z + 1, EndExpansion.endTorch);
        setIfAir(world, x + w - 2, y + 2, z + 1, EndExpansion.endTorch);

        // Сундук
        world.setBlock(x + w / 2, y + 1, z + d - 2, Blocks.chest, 0, 2);
        fillForestChest(world, rand, x + w / 2, y + 1, z + d - 2);

        // Пни вокруг хижины — атмосфера
        int stumpCount = 3 + rand.nextInt(4);
        for (int i = 0; i < stumpCount; i++) {
            int sx = x + rand.nextInt(w + 4) - 2;
            int sz = z + rand.nextInt(d + 4) - 2;
            int sy = world.getTopSolidOrLiquidBlock(sx, sz);
            setIfAir(world, sx, sy, sz, EndExpansion.ancientLog);
            // Иногда пень в 2 блока
            if (rand.nextBoolean()) {
                setIfAir(world, sx, sy + 1, sz, EndExpansion.ancientLog);
            }
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

    private void fillForestChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;

        chest.setInventorySlotContents(0,
                new ItemStack(Items.ender_pearl,
                        ConfigHandler.getRandomAmount(rand, ConfigHandler.forestChestEnderPearlMin, ConfigHandler.forestChestEnderPearlMax)));
        chest.setInventorySlotContents(1,
                new ItemStack(EndExpansion.ancientLog,
                        ConfigHandler.getRandomAmount(rand, ConfigHandler.forestChestAncientLogMin, ConfigHandler.forestChestAncientLogMax)));
        chest.setInventorySlotContents(2,
                new ItemStack(Items.bone,
                        ConfigHandler.getRandomAmount(rand, ConfigHandler.forestChestBoneMin, ConfigHandler.forestChestBoneMax)));
        chest.setInventorySlotContents(3,
                new ItemStack(EndExpansion.voidPear,
                        ConfigHandler.getRandomAmount(rand, ConfigHandler.forestChestVoidPearMin, ConfigHandler.forestChestVoidPearMax)));
    }
}