package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import java.util.Random;

public class DecoratorDesert implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {

        // 1. Пирамида — 1 на остров, в центре
        generatePyramid(world, rand, centerX, centerY, centerZ);

        // 2. Разрушенные храмы — 1-2 штуки
        int templeCount = 1 + rand.nextInt(2);
        for (int i = 0; i < templeCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            generateRuinedTemple(world, rand, pos[0], y, pos[1]);
        }

        // 3. Караванные стоянки — 2-3 штуки
        int campCount = 2 + rand.nextInt(2);
        for (int i = 0; i < campCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            generateCaravanCamp(world, rand, pos[0], y, pos[1]);
        }
    }

    // ===== ПИРАМИДА =====

    /**
     * Ступенчатая пирамида с внутренней камерой и лутом.
     * 4 уровня, основание 13x13.
     */
    private void generatePyramid(World world, Random rand, int x, int y, int z) {
        int levels   = 4;
        int baseSize = 13;

        // Строим уровни снизу вверх
        for (int level = 0; level < levels; level++) {
            int size   = baseSize - level * 2; // 13, 11, 9, 7
            int offset = level;                // Смещение к центру
            int yLevel = y + level * 2;        // Каждый уровень на 2 блока выше

            // Заполняем слой
            for (int dx = 0; dx < size; dx++) {
                for (int dz = 0; dz < size; dz++) {
                    world.setBlock(x - baseSize/2 + offset + dx, yLevel,
                            z - baseSize/2 + offset + dz, EndExpansion.sandstoneEnd, 0, 2);
                    // Второй слой каждого уровня
                    world.setBlock(x - baseSize/2 + offset + dx, yLevel + 1,
                            z - baseSize/2 + offset + dz, EndExpansion.sandstoneEnd, 0, 2);
                }
            }

            // Края из кирпича для контраста
            for (int dx = 0; dx < size; dx++) {
                world.setBlock(x - baseSize/2 + offset + dx, yLevel,
                        z - baseSize/2 + offset,          EndExpansion.ashenStone, 0, 2);
                world.setBlock(x - baseSize/2 + offset + dx, yLevel,
                        z - baseSize/2 + offset + size-1, EndExpansion.ashenStone, 0, 2);
            }
            for (int dz = 0; dz < size; dz++) {
                world.setBlock(x - baseSize/2 + offset,          yLevel,
                        z - baseSize/2 + offset + dz, EndExpansion.ashenStone, 0, 2);
                world.setBlock(x - baseSize/2 + offset + size-1, yLevel,
                        z - baseSize/2 + offset + dz, EndExpansion.ashenStone, 0, 2);
            }
        }

        // Верхушка — одиночный блок
        int topY = y + levels * 2;
        world.setBlock(x, topY,     z, EndExpansion.seaCrystal, 0, 2); // Светящийся кристалл на вершине
        world.setBlock(x, topY + 1, z, EndExpansion.seaCrystal, 0, 2);

        // Внутренняя камера — в основании пирамиды
        generatePyramidChamber(world, rand, x, y, z);

        // Вход — туннель с севера
        int entranceX = x;
        int entranceZ = z - baseSize / 2;
        for (int i = 0; i < baseSize / 2; i++) {
            world.setBlock(entranceX, y + 1, entranceZ + i, Blocks.air, 0, 2);
            world.setBlock(entranceX, y + 2, entranceZ + i, Blocks.air, 0, 2);
        }
    }

    /**
     * Внутренняя камера пирамиды — сундуки с лутом, ловушки.
     */
    private void generatePyramidChamber(World world, Random rand, int x, int y, int z) {
        int cw = 7, ch = 4, cd = 7;
        int ox = x - cw / 2;
        int oz = z - cd / 2;

        // Очищаем пространство камеры
        for (int dx = 0; dx < cw; dx++) {
            for (int dz = 0; dz < cd; dz++) {
                for (int dy = 1; dy <= ch; dy++) {
                    world.setBlock(ox + dx, y + dy, oz + dz, Blocks.air, 0, 2);
                }
            }
        }

        // Пол камеры
        for (int dx = 0; dx < cw; dx++) {
            for (int dz = 0; dz < cd; dz++) {
                world.setBlock(ox + dx, y, oz + dz, EndExpansion.sandstoneEnd, 0, 2);
            }
        }

        // Колонны по углам
        setBlock(world, ox + 1,      y + 1, oz + 1,      EndExpansion.fortressPillar);
        setBlock(world, ox + 1,      y + 2, oz + 1,      EndExpansion.fortressPillar);
        setBlock(world, ox + cw - 2, y + 1, oz + 1,      EndExpansion.fortressPillar);
        setBlock(world, ox + cw - 2, y + 2, oz + 1,      EndExpansion.fortressPillar);
        setBlock(world, ox + 1,      y + 1, oz + cd - 2, EndExpansion.fortressPillar);
        setBlock(world, ox + 1,      y + 2, oz + cd - 2, EndExpansion.fortressPillar);
        setBlock(world, ox + cw - 2, y + 1, oz + cd - 2, EndExpansion.fortressPillar);
        setBlock(world, ox + cw - 2, y + 2, oz + cd - 2, EndExpansion.fortressPillar);

        // Алтарь в центре
        setBlock(world, x, y + 1, z, EndExpansion.ashenStone);
        setBlock(world, x, y + 2, z, EndExpansion.seaCrystal); // Светящийся алтарь

        // Сундуки по бокам алтаря
        world.setBlock(x - 1, y + 1, z, Blocks.chest, 0, 2);
        fillDesertChest(world, rand, x - 1, y + 1, z);
        world.setBlock(x + 1, y + 1, z, Blocks.chest, 0, 2);
        fillDesertChest(world, rand, x + 1, y + 1, z);

        // Факелы
        setBlock(world, ox + 1,      y + 3, oz + 1,      EndExpansion.endTorch);
        setBlock(world, ox + cw - 2, y + 3, oz + 1,      EndExpansion.endTorch);
        setBlock(world, ox + 1,      y + 3, oz + cd - 2, EndExpansion.endTorch);
        setBlock(world, ox + cw - 2, y + 3, oz + cd - 2, EndExpansion.endTorch);
    }

    // ===== РАЗРУШЕННЫЙ ХРАМ =====

    /**
     * Остатки колонного зала — несколько колонн, обломки стен, сундук.
     */
    private void generateRuinedTemple(World world, Random rand, int x, int y, int z) {
        int w = 9 + rand.nextInt(5); // 9-13
        int d = 9 + rand.nextInt(5);

        // Остатки пола
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                if (rand.nextInt(3) != 0) {
                    world.setBlock(x + dx, y - 1, z + dz, EndExpansion.sandstoneEnd, 0, 2);
                }
            }
        }

        // Колонны — 4 штуки по углам, частично разрушены
        int[][] columnPos = {
                {x + 1,     z + 1},
                {x + w - 2, z + 1},
                {x + 1,     z + d - 2},
                {x + w - 2, z + d - 2}
        };

        for (int[] cp : columnPos) {
            int colHeight = 3 + rand.nextInt(4); // 3-6, разная высота — разрушение
            for (int dy = 0; dy < colHeight; dy++) {
                if (rand.nextInt(5) != 0) { // Иногда пропускаем блок — дырки в колонне
                    setBlock(world, cp[0], y + dy, cp[1], EndExpansion.fortressPillar);
                }
            }
            // Обломки у основания
            for (int bx = -1; bx <= 1; bx++) {
                for (int bz = -1; bz <= 1; bz++) {
                    if (rand.nextInt(3) == 0 && world.isAirBlock(cp[0] + bx, y, cp[1] + bz)) {
                        world.setBlock(cp[0] + bx, y, cp[1] + bz, EndExpansion.sandstoneEnd, 0, 2);
                    }
                }
            }
        }

        // Остатки стен — фрагменты
        for (int dx = 0; dx < w; dx++) {
            placeRuinWall(world, rand, x + dx, y, z,         2);
            placeRuinWall(world, rand, x + dx, y, z + d - 1, 2);
        }
        for (int dz = 1; dz < d - 1; dz++) {
            placeRuinWall(world, rand, x,         y, z + dz, 2);
            placeRuinWall(world, rand, x + w - 1, y, z + dz, 2);
        }

        // Алтарный камень в центре
        setBlock(world, x + w/2, y,     z + d/2, EndExpansion.ashenStone);
        setBlock(world, x + w/2, y + 1, z + d/2, EndExpansion.sandstoneEnd);

        // Сундук с лутом — иногда
        if (rand.nextInt(2) == 0) {
            world.setBlock(x + w/2, y + 1, z + d/2 + 1, Blocks.chest, 0, 2);
            fillDesertChest(world, rand, x + w/2, y + 1, z + d/2 + 1);
        }
    }

    /**
     * Ставит фрагмент стены — случайная высота, дыры.
     */
    private void placeRuinWall(World world, Random rand, int x, int y, int z, int maxH) {
        int h = rand.nextInt(maxH + 1);
        for (int dy = 0; dy <= h; dy++) {
            if (rand.nextInt(3) != 0 && world.isAirBlock(x, y + dy, z)) {
                world.setBlock(x, y + dy, z, EndExpansion.sandstoneEnd, 0, 2);
            }
        }
    }

    // ===== КАРАВАННАЯ СТОЯНКА =====

    /**
     * Стоянка каравана — несколько сундуков под навесом, факелы, кактусы.
     */
    private void generateCaravanCamp(World world, Random rand, int x, int y, int z) {

        // Навес из песчаника (3x3 крыша на столбах)
        int w = 5, d = 4;

        // Столбы навеса
        for (int dy = 0; dy < 3; dy++) {
            setBlock(world, x,         y + dy, z,         EndExpansion.sandstoneEnd);
            setBlock(world, x + w - 1, y + dy, z,         EndExpansion.sandstoneEnd);
            setBlock(world, x,         y + dy, z + d - 1, EndExpansion.sandstoneEnd);
            setBlock(world, x + w - 1, y + dy, z + d - 1, EndExpansion.sandstoneEnd);
        }

        // Крыша навеса
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                setBlock(world, x + dx, y + 3, z + dz, EndExpansion.sandstoneEnd);
            }
        }

        // Сундуки под навесом — 2-3 штуки
        int chestCount = 2 + rand.nextInt(2);
        for (int i = 0; i < chestCount; i++) {
            int cx = x + 1 + rand.nextInt(w - 2);
            int cz = z + 1 + rand.nextInt(d - 2);
            if (world.isAirBlock(cx, y, cz)) {
                world.setBlock(cx, y, cz, Blocks.chest, 0, 2);
                fillCaravanChest(world, rand, cx, y, cz);
            }
        }

        // Факелы на столбах
        setBlock(world, x + 1,     y + 2, z,         EndExpansion.endTorch);
        setBlock(world, x + w - 2, y + 2, z,         EndExpansion.endTorch);

        // Кактусы вокруг стоянки — ограда
        int[] cactusOffsets = {-2, w + 1};
        for (int co : cactusOffsets) {
            for (int dz = 0; dz < d; dz++) {
                if (rand.nextInt(2) == 0) {
                    int cy = world.getTopSolidOrLiquidBlock(x + co, z + dz);
                    if (world.getBlock(x + co, cy - 1, z + dz) == EndExpansion.endSand) {
                        world.setBlock(x + co, cy, z + dz, EndExpansion.endCactus, 0, 2);
                    }
                }
            }
        }

        // Оазис рядом — иногда
        if (rand.nextInt(3) == 0) {
            int oasisX = x + w + 3 + rand.nextInt(4);
            int oasisZ = z + rand.nextInt(d);
            int oasisY = world.getTopSolidOrLiquidBlock(oasisX, oasisZ);
            generateOasis(world, rand, oasisX, oasisY, oasisZ);
        }
    }

    /**
     * Оазис — небольшой водоём.
     */
    private void generateOasis(World world, Random rand, int x, int y, int z) {
        int r = 3 + rand.nextInt(3);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= r * r) {
                    world.setBlock(x + dx, y - 1, z + dz, Blocks.water, 0, 2);
                    world.setBlock(x + dx, y - 2, z + dz, EndExpansion.endSand,  0, 2);
                    // Кактусы по берегу
                    if (dx * dx + dz * dz > (r - 1) * (r - 1) && rand.nextInt(3) == 0) {
                        if (world.getBlock(x + dx, y, z + dz) == Blocks.air) {
                            world.setBlock(x + dx, y, z + dz, EndExpansion.endCactus, 0, 2);
                        }
                    }
                }
            }
        }
    }

    // ===== УТИЛИТЫ =====

    private void setBlock(World world, int x, int y, int z, Block block) {
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

    private void fillDesertChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;

        chest.setInventorySlotContents(0, new ItemStack(Items.gold_ingot,   1 + rand.nextInt(3)));
        chest.setInventorySlotContents(1, new ItemStack(Items.ender_pearl,  1 + rand.nextInt(2)));
        chest.setInventorySlotContents(2, new ItemStack(Items.bone,         2 + rand.nextInt(4)));
        chest.setInventorySlotContents(3, new ItemStack(EndExpansion.endSand, 3 + rand.nextInt(5)));
        // TODO: добавить кастомные предметы пустыни
    }

    private void fillCaravanChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;

        chest.setInventorySlotContents(0, new ItemStack(Items.emerald,      1 + rand.nextInt(2)));
        chest.setInventorySlotContents(1, new ItemStack(Items.bread,        2 + rand.nextInt(4)));
        chest.setInventorySlotContents(2, new ItemStack(Items.leather,      1 + rand.nextInt(3)));
        chest.setInventorySlotContents(3, new ItemStack(Items.ender_pearl,  1 + rand.nextInt(2)));
        // TODO: добавить кастомные предметы каравана
    }
}