package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;

import java.util.Random;

public class DecoratorFortress implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {
        int groundedCenterY = Math.max(40, world.getTopSolidOrLiquidBlock(centerX, centerZ));

        // Крепость строится от центра острова
        int y = world.getTopSolidOrLiquidBlock(centerX, centerZ);

        // 1. Внешние стены с воротами
        int wallRadius = Math.min(Math.max(28, radius - 10), 52); // масштаб по размеру острова
        generateOuterWalls(world, rand, centerX, y, centerZ, wallRadius);

        // 2. Башни по углам (4 штуки)
        generateCornerTower(world, rand, centerX - wallRadius, y, centerZ - wallRadius);
        generateCornerTower(world, rand, centerX + wallRadius, y, centerZ - wallRadius);
        generateCornerTower(world, rand, centerX - wallRadius, y, centerZ + wallRadius);
        generateCornerTower(world, rand, centerX + wallRadius, y, centerZ + wallRadius);

        // 3. Тронный зал — в центре, самое большое здание
        generateThroneHall(world, rand, centerX, y, centerZ);

        // 4. Казармы — 2-3 штуки внутри стен
        int barracksCount = 2 + rand.nextInt(2);
        int[][] barracksOffsets = {
                {-wallRadius / 2, -wallRadius / 2},
                { wallRadius / 2, -wallRadius / 2},
                {-wallRadius / 2,  wallRadius / 2}
        };
        for (int i = 0; i < barracksCount; i++) {
            int bx = centerX + barracksOffsets[i][0];
            int bz = centerZ + barracksOffsets[i][1];
            int by = world.getTopSolidOrLiquidBlock(bx, bz);
            generateBarracks(world, rand, bx, by, bz);
        }

        // 5. Дорожки между зданиями
        generateFortressPath(world, centerX, y, centerZ,
                centerX - wallRadius / 2, y, centerZ - wallRadius / 2);
        generateFortressPath(world, centerX, y, centerZ,
                centerX + wallRadius / 2, y, centerZ - wallRadius / 2);

        spawnGuardPatrols(world, rand, centerX, y + 1, centerZ, wallRadius);
    }

    private void spawnGuardPatrols(World world, Random rand, int centerX, int y, int centerZ, int wallRadius) {
        int patrols = 6 + rand.nextInt(4);
        for (int i = 0; i < patrols; i++) {
            int px = centerX - wallRadius + rand.nextInt(wallRadius * 2 + 1);
            int pz = centerZ - wallRadius + rand.nextInt(wallRadius * 2 + 1);
            if (!world.isAirBlock(px, y, pz)) continue;
            world.setBlock(px, y, pz, Blocks.mob_spawner, 0, 2);
            net.minecraft.tileentity.TileEntityMobSpawner spawner =
                    (net.minecraft.tileentity.TileEntityMobSpawner) world.getTileEntity(px, y, pz);
            if (spawner != null) {
                net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
                spawner.writeToNBT(nbt);
                nbt.setString("EntityId", rand.nextInt(3) == 0 ? "CaveSpider" : "Enderman");
                spawner.readFromNBT(nbt);
            }
        }
    }

    // ===== ВНЕШНИЕ СТЕНЫ =====

    /**
     * Квадратные стены с зубцами наверху и воротами с севера и юга.
     */
    private void generateOuterWalls(World world, Random rand, int cx, int y, int cz, int r) {
        int wallH = 8 + rand.nextInt(4); // 8-11

        // Четыре стены
        for (int i = -r; i <= r; i++) {
            buildWallSegment(world, cx + i, y, cz - r, wallH); // Северная
            buildWallSegment(world, cx + i, y, cz + r, wallH); // Южная
            buildWallSegment(world, cx - r, y, cz + i, wallH); // Западная
            buildWallSegment(world, cx + r, y, cz + i, wallH); // Восточная
        }

        // Зубцы поверх стен
        for (int i = -r; i <= r; i += 2) {
            setBlock(world, cx + i, y + wallH, cz - r, EndExpansion.fortressBrick);
            setBlock(world, cx + i, y + wallH, cz + r, EndExpansion.fortressBrick);
            setBlock(world, cx - r, y + wallH, cz + i, EndExpansion.fortressBrick);
            setBlock(world, cx + r, y + wallH, cz + i, EndExpansion.fortressBrick);
        }

        // Ворота с севера — проём 4 блока шириной, 5 высотой
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                world.setBlock(cx + dx, y + dy, cz - r, Blocks.air, 0, 2);
            }
        }
        // Арка над воротами
        setBlock(world, cx - 3, y + 5, cz - r, EndExpansion.fortressPillar);
        setBlock(world, cx - 2, y + 5, cz - r, EndExpansion.fortressBrick);
        setBlock(world, cx - 1, y + 6, cz - r, EndExpansion.fortressBrick);
        setBlock(world, cx,     y + 6, cz - r, EndExpansion.fortressBrick);
        setBlock(world, cx + 1, y + 6, cz - r, EndExpansion.fortressBrick);
        setBlock(world, cx + 2, y + 5, cz - r, EndExpansion.fortressBrick);
        setBlock(world, cx + 3, y + 5, cz - r, EndExpansion.fortressPillar);

        // Факелы у ворот
        setBlock(world, cx - 3, y + 3, cz - r + 1, EndExpansion.endTorch);
        setBlock(world, cx + 3, y + 3, cz - r + 1, EndExpansion.endTorch);
    }

    /**
     * Сегмент стены — два блока толщиной.
     */
    private void buildWallSegment(World world, int x, int y, int z, int height) {
        for (int dy = 0; dy < height; dy++) {
            world.setBlock(x, y + dy, z,     EndExpansion.fortressBrick,  0, 2);
            world.setBlock(x, y + dy, z + 1, EndExpansion.fortressBrick,  0, 2);
        }
    }

    // ===== УГЛОВЫЕ БАШНИ =====

    /**
     * Круглая башня с внутренней лестницей и смотровой площадкой.
     */
    private void generateCornerTower(World world, Random rand, int x, int y, int z) {
        int towerR = 4;
        int towerH = 15 + rand.nextInt(6); // 15-20

        // Цилиндрические стены башни
        for (int dy = 0; dy < towerH; dy++) {
            for (int dx = -towerR; dx <= towerR; dx++) {
                for (int dz = -towerR; dz <= towerR; dz++) {
                    float dist = dx * dx + dz * dz;
                    float rSq  = towerR * towerR;
                    // Только стены — кольцо
                    if (dist <= rSq && dist > (towerR - 1.5F) * (towerR - 1.5F)) {
                        world.setBlock(x + dx, y + dy, z + dz, EndExpansion.fortressBrick, 0, 2);
                    }
                }
            }
        }

        // Пол башни
        for (int dx = -towerR + 1; dx <= towerR - 1; dx++) {
            for (int dz = -towerR + 1; dz <= towerR - 1; dz++) {
                world.setBlock(x + dx, y, z + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        // Спиральная лестница внутри
        for (int dy = 0; dy < towerH; dy++) {
            double angle = dy * (Math.PI / 4);
            int sx = (int)(Math.cos(angle) * (towerR - 2));
            int sz = (int)(Math.sin(angle) * (towerR - 2));
            world.setBlock(x + sx, y + dy, z + sz, EndExpansion.fortressBrick, 0, 2);
        }

        // Смотровая площадка наверху
        for (int dx = -towerR - 1; dx <= towerR + 1; dx++) {
            for (int dz = -towerR - 1; dz <= towerR + 1; dz++) {
                setBlock(world, x + dx, y + towerH, z + dz, EndExpansion.fortressBrick);
            }
        }

        // Зубцы на площадке
        for (int dx = -towerR - 1; dx <= towerR + 1; dx += 2) {
            setBlock(world, x + dx, y + towerH + 1, z - towerR - 1, EndExpansion.fortressBrick);
            setBlock(world, x + dx, y + towerH + 1, z + towerR + 1, EndExpansion.fortressBrick);
        }
        for (int dz = -towerR; dz <= towerR; dz += 2) {
            setBlock(world, x - towerR - 1, y + towerH + 1, z + dz, EndExpansion.fortressBrick);
            setBlock(world, x + towerR + 1, y + towerH + 1, z + dz, EndExpansion.fortressBrick);
        }

        // Факелы на башне
        setBlock(world, x - towerR, y + towerH / 2, z,      EndExpansion.endTorch);
        setBlock(world, x + towerR, y + towerH / 2, z,      EndExpansion.endTorch);
        setBlock(world, x,          y + towerH / 2, z - towerR, EndExpansion.endTorch);
        setBlock(world, x,          y + towerH / 2, z + towerR, EndExpansion.endTorch);

        // Сундук на смотровой площадке
        world.setBlock(x, y + towerH + 1, z, Blocks.chest, 0, 2);
        fillTowerChest(world, rand, x, y + towerH + 1, z);
    }

    // ===== ТРОННЫЙ ЗАЛ =====

    /**
     * Главное здание крепости — большой зал с колоннами,
     * троном и спавнером босса.
     */
    private void generateThroneHall(World world, Random rand, int x, int y, int z) {
        int w = 17, h = 12, d = 21;
        int ox = x - w / 2;
        int oz = z - d / 2;

        // Фундамент
        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                world.setBlock(ox + dx, y - 1, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        // Стены
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                world.setBlock(ox + dx, y + dy, oz,         EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + dx, y + dy, oz + d - 1, EndExpansion.fortressBrick, 0, 2);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                world.setBlock(ox,         y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + w - 1, y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        // Крыша
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(ox + dx, y + h, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        // Вход — большой проём
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy < 6; dy++) {
                world.setBlock(x + dx, y + dy, oz, Blocks.air, 0, 2);
            }
        }

        // Колонны внутри — 4 пары
        int[][] columnOffsets = {
                {-5, 4}, {5, 4}, {-5, 10}, {5, 10}, {-5, 16}, {5, 16}
        };
        for (int[] co : columnOffsets) {
            for (int dy = 0; dy < h; dy++) {
                world.setBlock(x + co[0], y + dy, oz + co[1], EndExpansion.fortressPillar, 0, 2);
            }
            // Факел на колонне
            setBlock(world, x + co[0] + 1, y + h / 2, oz + co[1], EndExpansion.endTorch);
        }

        // Пол из чередующихся блоков
        for (int dx = 1; dx < w - 1; dx++) {
            for (int dz = 1; dz < d - 1; dz++) {
                Block floorBlock = (dx + dz) % 2 == 0
                        ? EndExpansion.fortressBrick
                        : EndExpansion.fortressPillar;
                world.setBlock(ox + dx, y, oz + dz, floorBlock, 0, 2);
            }
        }

        // Трон в дальнем конце зала
        generateThrone(world, x, y, oz + d - 4);

        // Спавнер босса перед троном
        // TODO: заменить на кастомного босса когда будет готов
// Вместо всего блока со spawner:
        world.setBlock(x, y + 1, oz + d - 6, Blocks.mob_spawner, 0, 2);
        net.minecraft.tileentity.TileEntityMobSpawner spawner =
                (net.minecraft.tileentity.TileEntityMobSpawner) world.getTileEntity(x, y + 1, oz + d - 6);
        if (spawner != null) {
            net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
            spawner.writeToNBT(nbt);
            nbt.getCompoundTag("SpawnData").setString("id", "Enderman");
            spawner.readFromNBT(nbt);
        }

        // Сундуки с сокровищами у трона
        world.setBlock(x - 2, y + 1, oz + d - 3, Blocks.chest, 0, 2);
        fillBossChest(world, rand, x - 2, y + 1, oz + d - 3);
        world.setBlock(x + 2, y + 1, oz + d - 3, Blocks.chest, 0, 2);
        fillBossChest(world, rand, x + 2, y + 1, oz + d - 3);
    }

    /**
     * Трон из фортресс-блоков с подлокотниками и спинкой.
     */
    private void generateThrone(World world, int x, int y, int z) {
        // Сиденье
        world.setBlock(x - 1, y + 1, z, EndExpansion.fortressBrick, 0, 2);
        world.setBlock(x,     y + 1, z, EndExpansion.fortressBrick, 0, 2);
        world.setBlock(x + 1, y + 1, z, EndExpansion.fortressBrick, 0, 2);

        // Спинка
        world.setBlock(x - 1, y + 2, z + 1, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x,     y + 2, z + 1, EndExpansion.fortressBrick,  0, 2);
        world.setBlock(x + 1, y + 2, z + 1, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x - 1, y + 3, z + 1, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x,     y + 3, z + 1, EndExpansion.fortressBrick,  0, 2);
        world.setBlock(x + 1, y + 3, z + 1, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x - 1, y + 4, z + 1, EndExpansion.fortressBrick,  0, 2);
        world.setBlock(x,     y + 4, z + 1, EndExpansion.seaCrystal,     0, 2); // Кристалл на вершине трона
        world.setBlock(x + 1, y + 4, z + 1, EndExpansion.fortressBrick,  0, 2);

        // Подлокотники
        world.setBlock(x - 1, y + 2, z, EndExpansion.fortressBrick, 0, 2);
        world.setBlock(x + 1, y + 2, z, EndExpansion.fortressBrick, 0, 2);
    }

    // ===== КАЗАРМЫ =====

    /**
     * Прямоугольное здание с кроватями (сундуками) и спавнерами мобов.
     */
    private void generateBarracks(World world, Random rand, int x, int y, int z) {
        int w = 9, h = 5, d = 13;
        int ox = x - w / 2;
        int oz = z - d / 2;

        // Пол
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(ox + dx, y - 1, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        // Стены
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                setBlock(world, ox + dx, y + dy, oz,         EndExpansion.fortressBrick);
                setBlock(world, ox + dx, y + dy, oz + d - 1, EndExpansion.fortressBrick);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                setBlock(world, ox,         y + dy, oz + dz, EndExpansion.fortressBrick);
                setBlock(world, ox + w - 1, y + dy, oz + dz, EndExpansion.fortressBrick);
            }
        }

        // Крыша
        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                setBlock(world, ox + dx, y + h, oz + dz, EndExpansion.fortressBrick);
            }
        }

        // Дверной проём
        world.setBlock(x, y,     oz, Blocks.air, 0, 2);
        world.setBlock(x, y + 1, oz, Blocks.air, 0, 2);

        // Окна
        world.setBlock(ox + 2, y + 2, oz,         Blocks.air, 0, 2);
        world.setBlock(ox + 6, y + 2, oz,         Blocks.air, 0, 2);
        world.setBlock(ox + 2, y + 2, oz + d - 1, Blocks.air, 0, 2);
        world.setBlock(ox + 6, y + 2, oz + d - 1, Blocks.air, 0, 2);

        // Сундуки-кровати вдоль стен
        for (int i = 1; i < d - 1; i += 3) {
            world.setBlock(ox + 1,     y + 1, oz + i, Blocks.chest, 0, 2);
            fillBarracksChest(world, rand, ox + 1,     y + 1, oz + i);
            world.setBlock(ox + w - 2, y + 1, oz + i, Blocks.chest, 0, 2);
            fillBarracksChest(world, rand, ox + w - 2, y + 1, oz + i);
        }

        // Факелы
        setBlock(world, ox + 1,     y + 3, oz + d / 2, EndExpansion.endTorch);
        setBlock(world, ox + w - 2, y + 3, oz + d / 2, EndExpansion.endTorch);

        // Спавнер мобов
// Вместо всего блока со spawner:
        world.setBlock(x, y + 1, oz + d - 6, Blocks.mob_spawner, 0, 2);
        net.minecraft.tileentity.TileEntityMobSpawner spawner =
                (net.minecraft.tileentity.TileEntityMobSpawner) world.getTileEntity(x, y + 1, oz + d - 6);
        if (spawner != null) {
            net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
            spawner.writeToNBT(nbt);
            nbt.getCompoundTag("SpawnData").setString("id", "Enderman");
            spawner.readFromNBT(nbt);
        }
    }

    // ===== ДОРОЖКИ =====

    private void generateFortressPath(World world,
                                      int x1, int y1, int z1,
                                      int x2, int y2, int z2) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        int steps = (int) Math.sqrt(dx * dx + dz * dz);

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            int px = x1 + (int)(dx * t);
            int pz = z1 + (int)(dz * t);
            int py = world.getTopSolidOrLiquidBlock(px, pz);

            world.setBlock(px,     py - 1, pz,     EndExpansion.fortressBrick, 0, 2);
            world.setBlock(px + 1, py - 1, pz,     EndExpansion.fortressBrick, 0, 2);
            world.setBlock(px,     py - 1, pz + 1, EndExpansion.fortressBrick, 0, 2);
        }
    }

    // ===== УТИЛИТЫ =====

    private void setBlock(World world, int x, int y, int z, Block block) {
        if (world.isAirBlock(x, y, z)) {
            world.setBlock(x, y, z, block, 0, 2);
        }
    }

    private void fillTowerChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.iron_ingot,  2 + rand.nextInt(4)));
        chest.setInventorySlotContents(1, new ItemStack(Items.ender_pearl, 1 + rand.nextInt(3)));
        chest.setInventorySlotContents(2, new ItemStack(Items.arrow,       4 + rand.nextInt(8)));
    }

    private void fillBarracksChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.iron_sword,  1));
        chest.setInventorySlotContents(1, new ItemStack(Items.iron_helmet, 1));
        chest.setInventorySlotContents(2, new ItemStack(Items.bread,       2 + rand.nextInt(3)));
        chest.setInventorySlotContents(3, new ItemStack(Items.ender_pearl, 1 + rand.nextInt(2)));
    }

    private void fillBossChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.diamond,        1 + rand.nextInt(3)));
        chest.setInventorySlotContents(1, new ItemStack(Items.golden_apple,   1));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl,    2 + rand.nextInt(4)));
        chest.setInventorySlotContents(3, new ItemStack(Items.diamond_sword,  1));
        chest.setInventorySlotContents(4, new ItemStack(Items.diamond_helmet, 1));
        // TODO: добавить кастомный лут босса
    }
}