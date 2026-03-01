package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import java.util.Random;

public class DecoratorOcean implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {
        int groundedCenterY = Math.max(40, world.getTopSolidOrLiquidBlock(centerX, centerZ));

        // 1. Пирс с домиком — 1-2 штуки
        int pierCount = 1 + rand.nextInt(2);
        for (int i = 0; i < pierCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius / 2);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            generatePierWithHouse(world, rand, pos[0], y, pos[1]);
        }

        // 2. Затонувшие корабли — 1-3 штуки
        int shipCount = 1 + rand.nextInt(3);
        for (int i = 0; i < shipCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = findWaterSurface(world, pos[0], pos[1]);
            if (y > 0) {
                generateSunkenShip(world, rand, pos[0], y, pos[1]);
            }
        }
    }

    // ===== ПИРС С ДОМИКОМ =====

    private void generatePierWithHouse(World world, Random rand, int x, int y, int z) {
        int pierLength = 8 + rand.nextInt(7); // 8-14 блоков

        // Направление пирса
        int dirX = rand.nextBoolean() ? 1 : 0;
        int dirZ = dirX == 1 ? 0 : 1;

        // Сваи и настил пирса
        for (int i = 0; i < pierLength; i++) {
            int px = x + dirX * i;
            int pz = z + dirZ * i;
            int py = world.getTopSolidOrLiquidBlock(px, pz);

            // Настил
            world.setBlock(px, py, pz, EndExpansion.witheredLog, 0, 2);

            // Перила по бокам
            if (dirX == 1) {
                setIfAir(world, px, py + 1, pz - 1, EndExpansion.ashenStone); // TODO: fence
                setIfAir(world, px, py + 1, pz + 1, EndExpansion.ashenStone);
            } else {
                setIfAir(world, px - 1, py + 1, pz, EndExpansion.ashenStone);
                setIfAir(world, px + 1, py + 1, pz, EndExpansion.ashenStone);
            }

            // Сваи вниз до дна
            int depth = py - 1;
            while (depth > py - 8 && world.getBlock(px, depth, pz) == Blocks.water) {
                world.setBlock(px, depth, pz, EndExpansion.witheredLog, 0, 2);
                depth--;
            }

            // Фонарь на каждом 3-м блоке
            if (i % 3 == 0) {
                setIfAir(world, px, py + 2, pz, EndExpansion.endTorch);
            }
        }

        // Домик на конце пирса
        int houseX = x + dirX * pierLength;
        int houseZ = z + dirZ * pierLength;
        int houseY = world.getTopSolidOrLiquidBlock(houseX, houseZ);
        generatePierHouse(world, rand, houseX, houseY, houseZ);
    }

    private void generatePierHouse(World world, Random rand, int x, int y, int z) {
        int w = 5, h = 4, d = 5;

        // Пол
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(x + dx, y - 1, z + dz, EndExpansion.witheredLog, 0, 2);
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

        // Крыша — двускатная из плит
        for (int dx = -1; dx <= w; dx++) {
            setIfAir(world, x + dx, y + h,     z + d / 2, EndExpansion.oceanStone);
            setIfAir(world, x + dx, y + h + 1, z + d / 2, EndExpansion.oceanStone);
        }
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                if (world.isAirBlock(x + dx, y + h, z + dz)) {
                    world.setBlock(x + dx, y + h, z + dz, EndExpansion.oceanStone, 0, 2);
                }
            }
        }

        // Интерьер
        setIfAir(world, x + 1,     y + 2, z + 1, EndExpansion.endTorch);
        setIfAir(world, x + w - 2, y + 2, z + 1, EndExpansion.endTorch);

        // Сундук
        world.setBlock(x + w / 2, y + 1, z + d - 2, Blocks.chest, 0, 2);
        fillOceanChest(world, rand, x + w / 2, y + 1, z + d - 2);

        // Кристаллы на крыше
        if (rand.nextInt(2) == 0) {
            setIfAir(world, x + w / 2, y + h + 2, z + d / 2, EndExpansion.seaCrystal);
        }
    }

    // ===== ЗАТОНУВШИЙ КОРАБЛЬ =====

    /**
     * Корабль лежит на боку или под наклоном на дне/в воде.
     * Корпус из брёвен, мачта, трюм с лутом.
     */
    private void generateSunkenShip(World world, Random rand, int x, int y, int z) {
        int length = 12 + rand.nextInt(7); // 12-18
        int width  = 4;
        int tilt   = rand.nextInt(3) - 1;  // -1, 0, 1 — наклон

        // Корпус корабля
        for (int i = 0; i < length; i++) {
            int shipY = y - 2 + (i < length / 3 ? 1 : 0) // Нос чуть выше
                    + (i > length * 2 / 3 ? -1 : 0); // Корма чуть ниже

            for (int w = -width / 2; w <= width / 2; w++) {
                // Дно корабля
                world.setBlock(x + i, shipY - 1, z + w, EndExpansion.witheredLog, 0, 2);

                // Борта
                world.setBlock(x + i, shipY,     z - width / 2, EndExpansion.witheredLog, 0, 2);
                world.setBlock(x + i, shipY,     z + width / 2, EndExpansion.witheredLog, 0, 2);
                world.setBlock(x + i, shipY + 1, z - width / 2, EndExpansion.witheredLog, 0, 2);
                world.setBlock(x + i, shipY + 1, z + width / 2, EndExpansion.witheredLog, 0, 2);
            }

            // Нос и корма
            if (i == 0 || i == length - 1) {
                for (int dy = -1; dy <= 2; dy++) {
                    for (int w = -width / 2; w <= width / 2; w++) {
                        setIfAir(world, x + i, shipY + dy, z + w, EndExpansion.witheredLog);
                    }
                }
            }
        }

        // Мачта — сломанная, под углом
        int mastX = x + length / 3;
        int mastY = y;
        int mastHeight = 5 + rand.nextInt(4);
        for (int i = 0; i < mastHeight; i++) {
            setIfAir(world, mastX + (i > mastHeight / 2 ? 1 : 0),
                    mastY + i,
                    z, EndExpansion.ancientLog);
        }

        // Остатки паруса — листья на мачте
        for (int lx = -1; lx <= 2; lx++) {
            for (int ly = mastHeight - 3; ly <= mastHeight; ly++) {
                if (rand.nextInt(3) != 0) {
                    setIfAir(world, mastX + lx, mastY + ly, z, EndExpansion.witheredLeaves);
                }
            }
        }

        // Трюм — сундуки внутри корабля
        int trunkX = x + length / 2;
        int trunkY = y - 1;
        world.setBlock(trunkX, trunkY, z, Blocks.chest, 0, 2);
        fillShipChest(world, rand, trunkX, trunkY, z);

        if (rand.nextInt(2) == 0) {
            world.setBlock(trunkX + 2, trunkY, z, Blocks.chest, 0, 2);
            fillShipChest(world, rand, trunkX + 2, trunkY, z);
        }

        // Кристаллы на обломках
        for (int i = 0; i < 3 + rand.nextInt(3); i++) {
            int cx = x + rand.nextInt(length);
            int cz = z + rand.nextInt(width) - width / 2;
            int cy = world.getTopSolidOrLiquidBlock(cx, cz);
            setIfAir(world, cx, cy, cz, EndExpansion.seaCrystal);
        }
    }

    // ===== УТИЛИТЫ =====

    /**
     * Ищет уровень воды в данной точке.
     * Возвращает -1 если воды нет.
     */
    private int findWaterSurface(World world, int x, int z) {
        for (int y = 80; y > 30; y--) {
            if (world.getBlock(x, y, z) == Blocks.water) return y;
        }
        return -1;
    }

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

    private void fillOceanChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.fishing_rod,  1));
        chest.setInventorySlotContents(1, new ItemStack(Items.fish,         2 + rand.nextInt(4)));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl,  1 + rand.nextInt(2)));
        chest.setInventorySlotContents(3, new ItemStack(EndExpansion.seaCrystal, 1 + rand.nextInt(3)));
    }

    private void fillShipChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.gold_ingot,   1 + rand.nextInt(4)));
        chest.setInventorySlotContents(1, new ItemStack(Items.diamond,      rand.nextInt(2)));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl,  1 + rand.nextInt(3)));
        chest.setInventorySlotContents(3, new ItemStack(Items.compass,      1));
        chest.setInventorySlotContents(4, new ItemStack(Items.map,          1));
        // TODO: добавить кастомные предметы океана
    }
}