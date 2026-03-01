package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import java.util.Random;

public class DecoratorCemetery implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {
        // 1. Генерируем домик хранителя в центре острова
        generateKeeperHouse(world, rand, centerX, centerY, centerZ);

        // 2. Генерируем участки с могилами вокруг
        int plotCount = 3 + rand.nextInt(4); // 3-6 участков
        for (int i = 0; i < plotCount; i++) {
            int angle = rand.nextInt(360);
            int dist  = 15 + rand.nextInt(radius / 2);
            int px    = centerX + (int)(Math.cos(Math.toRadians(angle)) * dist);
            int pz    = centerZ + (int)(Math.sin(Math.toRadians(angle)) * dist);
            int py    = world.getTopSolidOrLiquidBlock(px, pz);

            generateGravePlot(world, rand, px, py, pz);
        }

        // 3. Склепы — редко, 1-2 на остров
        int cryptCount = 1 + rand.nextInt(2);
        for (int i = 0; i < cryptCount; i++) {
            int angle = rand.nextInt(360);
            int dist  = 20 + rand.nextInt(radius / 3);
            int cx    = centerX + (int)(Math.cos(Math.toRadians(angle)) * dist);
            int cz    = centerZ + (int)(Math.sin(Math.toRadians(angle)) * dist);
            int cy    = world.getTopSolidOrLiquidBlock(cx, cz);

            generateCrypt(world, rand, cx, cy, cz);
        }
    }

    // ===== ДОМИК ХРАНИТЕЛЯ =====

    private void generateKeeperHouse(World world, Random rand, int x, int y, int z) {
        int w = 7, h = 5, d = 7;

        // Фундамент и пол
        fillLayer(world, x, y - 1, z, w, d, EndExpansion.ashenStone);

        // Стены
        buildWalls(world, x, y, z, w, h, d, EndExpansion.ashenStone);

        // Крыша из плит
        // TODO: заменить на ashen_slab когда будет зарегистрирован
        fillLayer(world, x, y + h, z, w, d, EndExpansion.ashenStone);

        // Дверной проём (2 блока высотой, по центру одной стены)
        world.setBlock(x + w / 2, y,     z, Blocks.air, 0, 2);
        world.setBlock(x + w / 2, y + 1, z, Blocks.air, 0, 2);

        // Факелы внутри по углам
        setIfAir(world, x + 1,     y + 2, z + 1,     EndExpansion.endTorch);
        setIfAir(world, x + w - 2, y + 2, z + 1,     EndExpansion.endTorch);
        setIfAir(world, x + 1,     y + 2, z + d - 2, EndExpansion.endTorch);
        setIfAir(world, x + w - 2, y + 2, z + d - 2, EndExpansion.endTorch);

        // Сундук внутри
        int chestX = x + w / 2;
        int chestZ = z + d - 2;
        world.setBlock(chestX, y + 1, chestZ, Blocks.chest, 0, 2);
        fillChest(world, rand, chestX, y + 1, chestZ);

        // Дорожка от двери
        generatePath(world, x + w / 2, y, z - 1, 0, -1, 5 + rand.nextInt(5));
    }

    // ===== УЧАСТОК С МОГИЛАМИ =====

    private void generateGravePlot(World world, Random rand, int x, int y, int z) {
        int plotW = 9 + rand.nextInt(5);  // 9-13
        int plotD = 9 + rand.nextInt(5);

        // Забор вокруг участка
        buildFenceBorder(world, x, y, z, plotW, plotD);

        // Вход в участок (разрыв в заборе)
        world.setBlock(x + plotW / 2, y, z, Blocks.air, 0, 2);

        // Дорожка внутри участка
        generatePath(world, x + plotW / 2, y, z + 1, 0, 1, plotD - 2);

        // Могилы внутри
        int graveCount = 2 + rand.nextInt(4); // 2-5 могил
        for (int i = 0; i < graveCount; i++) {
            int gx = x + 1 + rand.nextInt(plotW - 2);
            int gz = z + 1 + rand.nextInt(plotD - 2);
            int gy = world.getTopSolidOrLiquidBlock(gx, gz);

            if (rand.nextInt(4) == 0) {
                generateComplexGrave(world, rand, gx, gy, gz);
            } else {
                generateSimpleGrave(world, gx, gy, gz);
            }
        }
    }

    // ===== НАДГРОБИЯ =====

    /**
     * Простое надгробие: плита + надгробный камень + забор-крест
     */
    private void generateSimpleGrave(World world, int x, int y, int z) {
        // Плита перед надгробием
        setIfSurface(world, x, y, z, EndExpansion.ashenStone);

        // Надгробный камень
        setIfAir(world, x, y + 1, z, EndExpansion.gravestone);

        // Забор сверху как крест
        setIfAir(world, x,     y + 2, z,     EndExpansion.gravestone);
        setIfAir(world, x - 1, y + 2, z,     EndExpansion.gravestone);
        setIfAir(world, x + 1, y + 2, z,     EndExpansion.gravestone);
    }

    /**
     * Сложное надгробие: арка из стен и плит
     */
    private void generateComplexGrave(World world, Random rand, int x, int y, int z) {
        // Основание
        setIfSurface(world, x - 1, y, z, EndExpansion.ashenStone);
        setIfSurface(world, x,     y, z, EndExpansion.ashenStone);
        setIfSurface(world, x + 1, y, z, EndExpansion.ashenStone);

        // Два столба по бокам
        setIfAir(world, x - 1, y + 1, z, EndExpansion.ashenStone);
        setIfAir(world, x - 1, y + 2, z, EndExpansion.ashenStone);
        setIfAir(world, x + 1, y + 1, z, EndExpansion.ashenStone);
        setIfAir(world, x + 1, y + 2, z, EndExpansion.ashenStone);

        // Перекладина сверху (арка)
        setIfAir(world, x - 1, y + 3, z, EndExpansion.ashenStone);
        setIfAir(world, x,     y + 3, z, EndExpansion.ashenStone);
        setIfAir(world, x + 1, y + 3, z, EndExpansion.ashenStone);

        // Надгробие по центру
        setIfAir(world, x, y + 1, z, EndExpansion.gravestone);
        setIfAir(world, x, y + 2, z, EndExpansion.gravestone);

        // Факел рядом — иногда
        if (rand.nextInt(3) == 0) {
            setIfAir(world, x + 2, y + 1, z, EndExpansion.endTorch);
        }
    }

    // ===== СКЛЕП =====

    private void generateCrypt(World world, Random rand, int x, int y, int z) {
        int w = 5, h = 4, d = 6;

        // Пол
        fillLayer(world, x, y - 1, z, w, d, EndExpansion.fortressBrick);

        // Стены
        buildWalls(world, x, y, z, w, h, d, EndExpansion.fortressBrick);

        // Крыша
        fillLayer(world, x, y + h, z, w, d, EndExpansion.fortressBrick);

        // Дверной проём
        world.setBlock(x + w / 2, y,     z, Blocks.air, 0, 2);
        world.setBlock(x + w / 2, y + 1, z, Blocks.air, 0, 2);

        // Колонны по углам снаружи
        setIfAir(world, x - 1,  y,     z - 1, EndExpansion.fortressPillar);
        setIfAir(world, x - 1,  y + 1, z - 1, EndExpansion.fortressPillar);
        setIfAir(world, x + w,  y,     z - 1, EndExpansion.fortressPillar);
        setIfAir(world, x + w,  y + 1, z - 1, EndExpansion.fortressPillar);
        setIfAir(world, x - 1,  y,     z + d, EndExpansion.fortressPillar);
        setIfAir(world, x - 1,  y + 1, z + d, EndExpansion.fortressPillar);
        setIfAir(world, x + w,  y,     z + d, EndExpansion.fortressPillar);
        setIfAir(world, x + w,  y + 1, z + d, EndExpansion.fortressPillar);

        // Внутри — факелы и сундук
        setIfAir(world, x + 1,     y + 2, z + 1, EndExpansion.endTorch);
        setIfAir(world, x + w - 2, y + 2, z + 1, EndExpansion.endTorch);

        // Саркофаг из блоков (имитация)
        setIfAir(world, x + w / 2 - 1, y + 1, z + d - 2, EndExpansion.ashenStone);
        setIfAir(world, x + w / 2,     y + 1, z + d - 2, EndExpansion.ashenStone);
        setIfAir(world, x + w / 2 + 1, y + 1, z + d - 2, EndExpansion.ashenStone);

        // Сундук с лутом у саркофага
        world.setBlock(x + w / 2, y + 1, z + d - 3, Blocks.chest, 0, 2);
        fillChest(world, rand, x + w / 2, y + 1, z + d - 3);
    }

    // ===== УТИЛИТЫ =====

    /**
     * Дорожка из плит в заданном направлении
     */
    private void generatePath(World world, int x, int y, int z, int dx, int dz, int length) {
        for (int i = 0; i < length; i++) {
            int px = x + dx * i;
            int pz = z + dz * i;
            int py = world.getTopSolidOrLiquidBlock(px, pz);
            // TODO: заменить на ashen_slab
            world.setBlock(px, py - 1, pz, EndExpansion.ashenStone, 0, 2);
        }
    }

    /**
     * Забор по периметру участка
     */
    private void buildFenceBorder(World world, int x, int y, int z, int w, int d) {
        for (int i = 0; i < w; i++) {
            setIfAir(world, x + i, y, z,         EndExpansion.ashenStone); // TODO: ashen_fence
            setIfAir(world, x + i, y, z + d - 1, EndExpansion.ashenStone);
        }
        for (int i = 1; i < d - 1; i++) {
            setIfAir(world, x,         y, z + i, EndExpansion.ashenStone);
            setIfAir(world, x + w - 1, y, z + i, EndExpansion.ashenStone);
        }
    }

    /**
     * Заполняет слой блоками (пол/крыша)
     */
    private void fillLayer(World world, int x, int y, int z, int w, int d, Block block) {
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(x + dx, y, z + dz, block, 0, 2);
            }
        }
    }

    /**
     * Строит пустые стены здания
     */
    private void buildWalls(World world, int x, int y, int z, int w, int h, int d, Block block) {
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                setIfAir(world, x + dx, y + dy, z,         block); // Передняя стена
                setIfAir(world, x + dx, y + dy, z + d - 1, block); // Задняя стена
            }
            for (int dz = 1; dz < d - 1; dz++) {
                setIfAir(world, x,         y + dy, z + dz, block); // Левая стена
                setIfAir(world, x + w - 1, y + dy, z + dz, block); // Правая стена
            }
        }
    }

    /**
     * Ставит блок только если на месте воздух
     */
    private void setIfAir(World world, int x, int y, int z, Block block) {
        if (world.isAirBlock(x, y, z)) {
            world.setBlock(x, y, z, block, 0, 2);
        }
    }

    /**
     * Ставит блок на поверхность (заменяет верхний блок)
     */
    private void setIfSurface(World world, int x, int y, int z, Block block) {
        world.setBlock(x, y - 1, z, block, 0, 2);
    }

    /**
     * Заполняет сундук случайным лутом кладбища
     */
    private void fillChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;

        // TODO: добавить кастомные предметы когда будут готовы
        // Пока кладём кости и гнилое мясо как заглушку
        chest.setInventorySlotContents(0,
                new net.minecraft.item.ItemStack(net.minecraft.init.Items.bone, 1 + rand.nextInt(4)));
        chest.setInventorySlotContents(1,
                new net.minecraft.item.ItemStack(net.minecraft.init.Items.rotten_flesh, 1 + rand.nextInt(3)));
        chest.setInventorySlotContents(4,
                new net.minecraft.item.ItemStack(net.minecraft.init.Items.ender_pearl, 1 + rand.nextInt(2)));
    }
}