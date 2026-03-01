package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import java.util.Random;

public class DecoratorInfection implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {
        int groundedCenterY = Math.max(40, world.getTopSolidOrLiquidBlock(centerX, centerZ));

        generateFearSpikes(world, rand, centerX, centerZ, Math.max(12, radius - 4));

        // 1. Гигантские грибы — 4-7 штук, доминируют над биомом
        int mushroomCount = 4 + rand.nextInt(4);
        for (int i = 0; i < mushroomCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            if (world.getBlock(pos[0], y - 1, pos[1]) == EndExpansion.infestedMycelium) {
                generateGiantMushroom(world, rand, pos[0], y, pos[1]);
            }
        }

        // 2. Пульсирующие нарывы — 4-7 штук
        int pustuleCount = 4 + rand.nextInt(4);
        for (int i = 0; i < pustuleCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            if (world.getBlock(pos[0], y - 1, pos[1]) == EndExpansion.infestedMycelium) {
                generatePulsatingPustule(world, rand, pos[0], y, pos[1]);
            }
        }

        // 3. Коконы-паутины — 3-5 штук
        int cocoonCount = 3 + rand.nextInt(3);
        for (int i = 0; i < cocoonCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            generateCocoon(world, rand, pos[0], y, pos[1]);
        }

        // 4. Заражённые руины — 2-3 штуки
        int ruinCount = 2 + rand.nextInt(2);
        for (int i = 0; i < ruinCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            generateInfectedRuin(world, rand, pos[0], y, pos[1]);
        }
    }

    private void generateFearSpikes(World world, Random rand, int centerX, int centerZ, int radius) {
        int spikeCount = 10 + rand.nextInt(8);
        for (int i = 0; i < spikeCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            if (world.getBlock(pos[0], y - 1, pos[1]) != EndExpansion.infestedMycelium) continue;
            int h = 3 + rand.nextInt(5);
            for (int dy = 0; dy < h; dy++) {
                world.setBlock(pos[0], y + dy, pos[1], EndExpansion.pulsingRock, 0, 2);
            }
            if (rand.nextBoolean()) {
                setIfAir(world, pos[0], y + h, pos[1], EndExpansion.endTorch);
            }
        }
    }

    // ===== ГИГАНТСКИЙ ГРИБ =====

    /**
     * Огромный светящийся гриб с широкой шляпкой.
     * Ножка из infectedStalk, шляпка из glowshroom.
     * Под шляпкой — свисающие нити.
     */
    private void generateGiantMushroom(World world, Random rand, int x, int y, int z) {
        int stemHeight = 8 + rand.nextInt(7);  // 8-14
        int capRadius  = 4 + rand.nextInt(4);  // 4-7

        // Ножка — иногда изогнутая
        int sx = x, sz = z;
        for (int i = 0; i < stemHeight; i++) {
            // Толстая ножка 2x2
            world.setBlock(sx,     y + i, sz,     EndExpansion.infectedStalk, 0, 2);
            world.setBlock(sx + 1, y + i, sz,     EndExpansion.infectedStalk, 0, 2);
            world.setBlock(sx,     y + i, sz + 1, EndExpansion.infectedStalk, 0, 2);
            world.setBlock(sx + 1, y + i, sz + 1, EndExpansion.infectedStalk, 0, 2);

            // Лёгкий изгиб ножки
            if (i > stemHeight / 2 && rand.nextInt(5) == 0) {
                sx += rand.nextInt(3) - 1;
                sz += rand.nextInt(3) - 1;
            }
        }

        int capY = y + stemHeight;

        // Шляпка — плоский диск с неровными краями
        for (int dx = -capRadius; dx <= capRadius; dx++) {
            for (int dz = -capRadius; dz <= capRadius; dz++) {
                float dist = dx * dx + dz * dz;
                float rSq  = capRadius * capRadius;

                if (dist <= rSq) {
                    // Верхний слой шляпки
                    setIfAir(world, sx + dx, capY + 1, sz + dz, EndExpansion.glowshroom);
                    setIfAir(world, sx + dx, capY,     sz + dz, EndExpansion.glowshroom);

                    // Неровные края — нижний слой с дырками
                    if (dist > rSq * 0.5F) {
                        if (rand.nextInt(3) != 0) {
                            setIfAir(world, sx + dx, capY - 1, sz + dz, EndExpansion.glowshroom);
                        }
                    } else {
                        setIfAir(world, sx + dx, capY - 1, sz + dz, EndExpansion.glowshroom);
                    }
                }
            }
        }

        // Свисающие нити под шляпкой
        for (int dx = -capRadius + 1; dx <= capRadius - 1; dx++) {
            for (int dz = -capRadius + 1; dz <= capRadius - 1; dz++) {
                if (rand.nextInt(4) == 0) {
                    int threadLen = 1 + rand.nextInt(4);
                    for (int ty = 0; ty < threadLen; ty++) {
                        setIfAir(world, sx + dx, capY - 2 - ty, sz + dz, EndExpansion.infectedStalk);
                    }
                    // На конце нити — светящийся шарик
                    setIfAir(world, sx + dx, capY - 2 - threadLen, sz + dz, EndExpansion.glowshroom);
                }
            }
        }

        // Мицелий вокруг основания
        for (int dx = -2; dx <= 3; dx++) {
            for (int dz = -2; dz <= 3; dz++) {
                if (rand.nextInt(3) != 0) {
                    world.setBlock(x + dx, y - 1, z + dz, EndExpansion.infestedMycelium, 0, 2);
                }
            }
        }
    }

    // ===== ПУЛЬСИРУЮЩИЙ НАРЫВ =====

    /**
     * Выпуклость на земле — купол из pulsingRock со светящимся ядром.
     * Выглядит как живая опухоль.
     */
    private void generatePulsatingPustule(World world, Random rand, int x, int y, int z) {
        int radius = 3 + rand.nextInt(3); // 3-5

        // Купол из pulsingRock
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy <= radius; dy++) {
                    float dist = dx * dx + dz * dz + dy * dy * 1.5F;
                    if (dist <= radius * radius) {
                        setIfAir(world, x + dx, y + dy, z + dz, EndExpansion.pulsingRock);
                    }
                }
            }
        }

        // Светящееся ядро внутри
        world.setBlock(x,     y + 1, z,     EndExpansion.glowshroom, 0, 2);
        world.setBlock(x + 1, y + 1, z,     EndExpansion.glowshroom, 0, 2);
        world.setBlock(x,     y + 1, z + 1, EndExpansion.glowshroom, 0, 2);
        world.setBlock(x,     y + 2, z,     EndExpansion.glowshroom, 0, 2);

        // Трещины в нарыве — случайные дыры
        for (int dx = -radius + 1; dx <= radius - 1; dx++) {
            for (int dz = -radius + 1; dz <= radius - 1; dz++) {
                if (rand.nextInt(6) == 0) {
                    world.setBlock(x + dx, y + radius - 1, z + dz, Blocks.air, 0, 2);
                }
            }
        }

        // Грибы вокруг нарыва
        for (int i = 0; i < 3 + rand.nextInt(3); i++) {
            int mx = x + rand.nextInt(radius * 2 + 2) - radius - 1;
            int mz = z + rand.nextInt(radius * 2 + 2) - radius - 1;
            int my = world.getTopSolidOrLiquidBlock(mx, mz);
            setIfAir(world, mx, my, mz, EndExpansion.glowshroom);
        }
    }

    // ===== КОКОН =====

    /**
     * Кокон из паутины — сфера с паутиной снаружи и заражённым блоком внутри.
     * Внутри иногда — сундук с лутом (жертва).
     */
    private void generateCocoon(World world, Random rand, int x, int y, int z) {
        int r = 2 + rand.nextInt(2); // 2-3

        // Внешняя оболочка — паутина
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = 0; dy <= r * 2; dy++) {
                    float cx = dx;
                    float cy = dy - r;
                    float cz = dz;
                    float dist = cx * cx + cy * cy * 0.8F + cz * cz;

                    if (dist <= r * r) {
                        // Внешний слой — паутина
                        if (dist > (r - 1) * (r - 1)) {
                            setIfAir(world, x + dx, y + dy, z + dz, Blocks.web);
                        } else {
                            // Внутренность — заражённый материал
                            setIfAir(world, x + dx, y + dy, z + dz, EndExpansion.pulsingRock);
                        }
                    }
                }
            }
        }

        // Полость внутри
        world.setBlock(x,     y + r,     z,     Blocks.air, 0, 2);
        world.setBlock(x + 1, y + r,     z,     Blocks.air, 0, 2);
        world.setBlock(x,     y + r,     z + 1, Blocks.air, 0, 2);
        world.setBlock(x,     y + r + 1, z,     Blocks.air, 0, 2);

        // Сундук внутри — жертва каравана
        if (rand.nextInt(3) == 0) {
            world.setBlock(x, y + r, z, Blocks.chest, 0, 2);
            fillCocoonChest(world, rand, x, y + r, z);
        } else {
            // Или светящееся ядро
            world.setBlock(x, y + r, z, EndExpansion.glowshroom, 0, 2);
        }

        // Нити паутины свисают вниз
        for (int i = 0; i < 4 + rand.nextInt(4); i++) {
            int tx = x + rand.nextInt(r * 2) - r;
            int tz = z + rand.nextInt(r * 2) - r;
            int threadLen = 2 + rand.nextInt(4);
            for (int ty = 1; ty <= threadLen; ty++) {
                setIfAir(world, tx, y - ty, tz, Blocks.web);
            }
        }
    }

    // ===== ЗАРАЖЁННЫЕ РУИНЫ =====

    /**
     * Бывшая постройка полностью поглощена заражением.
     * Стены покрыты мицелием, внутри грибы и паутина.
     */
    private void generateInfectedRuin(World world, Random rand, int x, int y, int z) {
        int w = 7 + rand.nextInt(5); // 7-11
        int h = 3 + rand.nextInt(3); // 3-5
        int d = 7 + rand.nextInt(5);

        // Остатки пола — покрытые мицелием
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                if (rand.nextInt(3) != 0) {
                    world.setBlock(x + dx, y - 1, z + dz, EndExpansion.infestedMycelium, 0, 2);
                }
            }
        }

        // Заражённые стены — pulsingRock вместо оригинального камня
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                placeInfectedWall(world, rand, x + dx, y + dy, z,         h);
                placeInfectedWall(world, rand, x + dx, y + dy, z + d - 1, h);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                placeInfectedWall(world, rand, x,         y + dy, z + dz, h);
                placeInfectedWall(world, rand, x + w - 1, y + dy, z + dz, h);
            }
        }

        // Паутина внутри руин
        for (int dx = 1; dx < w - 1; dx++) {
            for (int dz = 1; dz < d - 1; dz++) {
                if (rand.nextInt(5) == 0) {
                    setIfAir(world, x + dx, y + h - 1, z + dz, Blocks.web);
                }
            }
        }

        // Грибы внутри
        for (int i = 0; i < 3 + rand.nextInt(4); i++) {
            int gx = x + 1 + rand.nextInt(w - 2);
            int gz = z + 1 + rand.nextInt(d - 2);
            int gy = world.getTopSolidOrLiquidBlock(gx, gz);
            setIfAir(world, gx, gy, gz, EndExpansion.glowshroom);
        }

        // Нарыв внутри — иногда
        if (rand.nextInt(3) == 0) {
            int px = x + w / 2;
            int pz = z + d / 2;
            int py = world.getTopSolidOrLiquidBlock(px, pz);
            generatePulsatingPustule(world, rand, px, py, pz);
        }

        // Сундук — жуткий лут
        if (rand.nextInt(2) == 0) {
            int cx = x + w / 2;
            int cz = z + d / 2 + 1;
            int cy = world.getTopSolidOrLiquidBlock(cx, cz);
            world.setBlock(cx, cy, cz, Blocks.chest, 0, 2);
            fillInfectionChest(world, rand, cx, cy, cz);
        }
    }

    /**
     * Заражённый блок стены — с дырками и мицелием.
     */
    private void placeInfectedWall(World world, Random rand, int x, int y, int z, int maxH) {
        if (rand.nextInt(4) == 0) return; // Дыра в стене
        Block block = rand.nextInt(3) == 0
                ? EndExpansion.infestedMycelium  // Мицелий захватывает стену
                : EndExpansion.pulsingRock;
        setIfAir(world, x, y, z, block);
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

    private void fillCocoonChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.rotten_flesh, 2 + rand.nextInt(4)));
        chest.setInventorySlotContents(1, new ItemStack(Items.spider_eye,   1 + rand.nextInt(3)));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl,  1 + rand.nextInt(2)));
        chest.setInventorySlotContents(3, new ItemStack(Items.string,       3 + rand.nextInt(5)));
    }

    private void fillInfectionChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.rotten_flesh,    2 + rand.nextInt(5)));
        chest.setInventorySlotContents(1, new ItemStack(Items.fermented_spider_eye, 1 + rand.nextInt(2)));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl,     1 + rand.nextInt(3)));
        chest.setInventorySlotContents(3, new ItemStack(EndExpansion.glowshroom, 1 + rand.nextInt(4)));
        // TODO: добавить кастомные предметы заражения
    }
}