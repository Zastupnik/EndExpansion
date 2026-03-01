package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;

import java.util.Random;

public class DecoratorFortress implements IEndBiomeDecorator {

    @Override
    public void decorate(World world, Random rand, int centerX, int centerY, int centerZ, int radius) {
        int groundedCenterY = Math.max(40, world.getTopSolidOrLiquidBlock(centerX, centerZ));

        // Крепость строится от центра острова
        int y = world.getTopSolidOrLiquidBlock(centerX, centerZ);
        terraformCourtyard(world, centerX, y, centerZ, Math.max(16, radius - 14));

        // 1. Внешние стены с воротами
        int wallRadius = Math.min(Math.max(28, radius - 10), 52); // масштаб по размеру острова
        generateOuterWalls(world, rand, centerX, y, centerZ, wallRadius);
        generateInnerWallRing(world, rand, centerX, y, centerZ, Math.max(10, wallRadius - 12));
        generateGatehouse(world, rand, centerX, stableSurfaceY(world, centerX, centerZ - wallRadius, y), centerZ - wallRadius + 2);

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
            int by = stableSurfaceY(world, bx, bz, groundedCenterY);
            generateBarracks(world, rand, bx, by, bz);
            generateFortressPath(world, centerX, y, centerZ, bx, by, bz);
        }

        // 5. Дорожки между зданиями
        generateFortressPath(world, centerX, y, centerZ,
                centerX - wallRadius / 2, y, centerZ - wallRadius / 2);
        generateFortressPath(world, centerX, y, centerZ,
                centerX + wallRadius / 2, y, centerZ - wallRadius / 2);
        generateFortressPath(world, centerX, y, centerZ, centerX, y, centerZ - wallRadius);
        generateFortressPath(world, centerX, y, centerZ, centerX, y, centerZ + wallRadius);
        generateFortressPath(world, centerX, y, centerZ, centerX - wallRadius, y, centerZ);
        generateFortressPath(world, centerX, y, centerZ, centerX + wallRadius, y, centerZ);

        generateRuinedOutposts(world, rand, centerX, y, centerZ, wallRadius + 6);

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

    private void terraformCourtyard(World world, int cx, int y, int cz, int radius) {
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > r2) continue;
                int x = cx + dx;
                int z = cz + dz;
                int top = world.getTopSolidOrLiquidBlock(x, z);
                int target = y + (((Math.abs(dx) + Math.abs(dz)) & 3) == 0 ? 0 : -1);

                if (top > target) {
                    for (int yy = top; yy > target; yy--) {
                        world.setBlock(x, yy - 1, z, EndExpansion.fortressBrick, 0, 2);
                    }
                } else {
                    for (int yy = top; yy <= target; yy++) {
                        world.setBlock(x, yy - 1, z, EndExpansion.fortressBrick, 0, 2);
                    }
                }
                anchorDown(world, x, target - 1, z, EndExpansion.fortressPillar, 18);
            }
        }
    }

    private void generateOuterWalls(World world, Random rand, int cx, int y, int cz, int r) {
        int wallH = 9 + rand.nextInt(4);
        for (int i = -r; i <= r; i++) {
            int northY = stableSurfaceY(world, cx + i, cz - r, y);
            int southY = stableSurfaceY(world, cx + i, cz + r, y);
            int westY = stableSurfaceY(world, cx - r, cz + i, y);
            int eastY = stableSurfaceY(world, cx + r, cz + i, y);
            buildWallSegment(world, cx + i, northY, cz - r, wallH, false);
            buildWallSegment(world, cx + i, southY, cz + r, wallH, false);
            buildWallSegment(world, cx - r, westY, cz + i, wallH, true);
            buildWallSegment(world, cx + r, eastY, cz + i, wallH, true);
        }

        for (int i = -r; i <= r; i += 2) {
            world.setBlock(cx + i, y + wallH, cz - r, EndExpansion.fortressBrick, 0, 2);
            world.setBlock(cx + i, y + wallH, cz + r, EndExpansion.fortressBrick, 0, 2);
            world.setBlock(cx - r, y + wallH, cz + i, EndExpansion.fortressBrick, 0, 2);
            world.setBlock(cx + r, y + wallH, cz + i, EndExpansion.fortressBrick, 0, 2);
        }

        carveGate(world, cx, y, cz - r, true);
        carveGate(world, cx, y, cz + r, true);
    }

    private void generateInnerWallRing(World world, Random rand, int cx, int y, int cz, int r) {
        int wallH = 5 + rand.nextInt(2);
        for (int i = -r; i <= r; i++) {
            buildWallSegment(world, cx + i, y, cz - r, wallH, false);
            buildWallSegment(world, cx + i, y, cz + r, wallH, false);
            buildWallSegment(world, cx - r, y, cz + i, wallH, true);
            buildWallSegment(world, cx + r, y, cz + i, wallH, true);
        }

        carveGate(world, cx, y, cz - r, false);
        carveGate(world, cx, y, cz + r, false);
        carveGate(world, cx - r, y, cz, false);
        carveGate(world, cx + r, y, cz, false);
    }

    private void carveGate(World world, int x, int y, int z, boolean torch) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy < 5; dy++) {
                world.setBlock(x + dx, y + dy, z, Blocks.air, 0, 2);
            }
        }
        world.setBlock(x - 3, y + 5, z, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x + 3, y + 5, z, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x, y + 6, z, EndExpansion.fortressBrick, 0, 2);
        if (torch) {
            world.setBlock(x - 3, y + 3, z + (z > 0 ? -1 : 1), EndExpansion.endTorch, 0, 2);
            world.setBlock(x + 3, y + 3, z + (z > 0 ? -1 : 1), EndExpansion.endTorch, 0, 2);
        }
    }

    private void buildWallSegment(World world, int x, int y, int z, int height, boolean xAxis) {
        for (int dy = 0; dy < height; dy++) {
            world.setBlock(x, y + dy, z, EndExpansion.fortressBrick, 0, 2);
            if (xAxis) {
                world.setBlock(x + 1, y + dy, z, EndExpansion.fortressBrick, 0, 2);
            } else {
                world.setBlock(x, y + dy, z + 1, EndExpansion.fortressBrick, 0, 2);
            }
        }
        anchorDown(world, x, y - 1, z, EndExpansion.fortressPillar, 14);
        if (xAxis) {
            anchorDown(world, x + 1, y - 1, z, EndExpansion.fortressPillar, 14);
        } else {
            anchorDown(world, x, y - 1, z + 1, EndExpansion.fortressPillar, 14);
        }
    }

    private void generateCornerTower(World world, Random rand, int x, int y, int z) {
        int r = 4;
        int h = 16 + rand.nextInt(6);

        for (int dy = 0; dy < h; dy++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    int dist = dx * dx + dz * dz;
                    if (dist <= r * r && dist >= (r - 1) * (r - 1)) {
                        world.setBlock(x + dx, y + dy, z + dz, EndExpansion.fortressBrick, 0, 2);
                    }
                }
            }
        }

        for (int dy = 0; dy < h; dy++) {
            int sx = (int) (Math.cos(dy * 0.75F) * (r - 2));
            int sz = (int) (Math.sin(dy * 0.75F) * (r - 2));
            world.setBlock(x + sx, y + dy, z + sz, EndExpansion.fortressPillar, 0, 2);
        }

        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                world.setBlock(x + dx, y + h, z + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        world.setBlock(x, y + h + 1, z, Blocks.chest, 0, 2);
        fillTowerChest(world, rand, x, y + h + 1, z);
        anchorDown(world, x, y - 1, z, EndExpansion.fortressPillar, 20);
    }

    private void generateGatehouse(World world, Random rand, int x, int y, int z) {
        int w = 9;
        int d = 7;
        int h = 6;
        int ox = x - w / 2;
        int oz = z - d / 2;

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(ox + dx, y - 1, oz + dz, EndExpansion.fortressBrick, 0, 2);
                anchorDown(world, ox + dx, y - 2, oz + dz, EndExpansion.fortressPillar, 10);
            }
        }

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                world.setBlock(ox + dx, y + dy, oz, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + dx, y + dy, oz + d - 1, EndExpansion.fortressBrick, 0, 2);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                world.setBlock(ox, y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + w - 1, y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 3; dy++) {
                world.setBlock(x + dx, y + dy, z, Blocks.air, 0, 2);
            }
        }

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(ox + dx, y + h, oz + dz, EndExpansion.fortressPillar, 0, 2);
            }
        }
    }

    private void generateThroneHall(World world, Random rand, int x, int y, int z) {
        int w = 19, h = 13, d = 25;
        int ox = x - w / 2;
        int oz = z - d / 2;

        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                world.setBlock(ox + dx, y - 1, oz + dz, EndExpansion.fortressBrick, 0, 2);
                anchorDown(world, ox + dx, y - 2, oz + dz, EndExpansion.fortressPillar, 12);
            }
        }

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                world.setBlock(ox + dx, y + dy, oz, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + dx, y + dy, oz + d - 1, EndExpansion.fortressBrick, 0, 2);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                world.setBlock(ox, y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + w - 1, y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                Block roof = (dx == 0 || dz == 0 || dx == w - 1 || dz == d - 1)
                        ? EndExpansion.fortressPillar
                        : EndExpansion.fortressBrick;
                world.setBlock(ox + dx, y + h, oz + dz, roof, 0, 2);
            }
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy < 6; dy++) {
                world.setBlock(x + dx, y + dy, oz, Blocks.air, 0, 2);
            }
        }

        int[][] columns = {
                {-6, 5}, {6, 5}, {-6, 12}, {6, 12}, {-6, 19}, {6, 19}
        };
        for (int i = 0; i < columns.length; i++) {
            int cx = x + columns[i][0];
            int cz = oz + columns[i][1];
            for (int dy = 0; dy < h; dy++) {
                world.setBlock(cx, y + dy, cz, EndExpansion.fortressPillar, 0, 2);
            }
            world.setBlock(cx + (i % 2 == 0 ? 1 : -1), y + h / 2, cz, EndExpansion.endTorch, 0, 2);
        }

        for (int dx = 1; dx < w - 1; dx++) {
            for (int dz = 1; dz < d - 1; dz++) {
                Block floor = ((dx + dz) & 1) == 0 ? EndExpansion.fortressBrick : EndExpansion.fortressPillar;
                world.setBlock(ox + dx, y, oz + dz, floor, 0, 2);
            }
        }

        generateThrone(world, x, y, oz + d - 5);

        world.setBlock(x, y + 1, oz + d - 8, Blocks.mob_spawner, 0, 2);
        TileEntityMobSpawner spawner = (TileEntityMobSpawner) world.getTileEntity(x, y + 1, oz + d - 8);
        if (spawner != null) {
            net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
            spawner.writeToNBT(nbt);
            nbt.getCompoundTag("SpawnData").setString("id", "Enderman");
            spawner.readFromNBT(nbt);
        }

        world.setBlock(x - 2, y + 1, oz + d - 4, Blocks.chest, 0, 2);
        fillBossChest(world, rand, x - 2, y + 1, oz + d - 4);
        world.setBlock(x + 2, y + 1, oz + d - 4, Blocks.chest, 0, 2);
        fillBossChest(world, rand, x + 2, y + 1, oz + d - 4);
    }

    private void generateCentralPlaza(World world, Random rand, int x, int y, int z, int r) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r) continue;
                Block floor = ((Math.abs(dx) + Math.abs(dz)) % 3 == 0)
                        ? EndExpansion.fortressPillar
                        : EndExpansion.fortressBrick;
                world.setBlock(x + dx, y - 1, z + dz, floor, 0, 2);
            }
        }

        for (int i = 0; i < 4; i++) {
            int sx = x + (i < 2 ? -r + 2 : r - 2);
            int sz = z + ((i % 2 == 0) ? -r + 2 : r - 2);
            for (int h = 0; h < 4; h++) {
                world.setBlock(sx, y + h, sz, EndExpansion.fortressPillar, 0, 2);
            }
            world.setBlock(sx, y + 4, sz, EndExpansion.endTorch, 0, 2);
        }
    }

    private void generateThrone(World world, int x, int y, int z) {
        world.setBlock(x - 1, y + 1, z, EndExpansion.fortressBrick, 0, 2);
        world.setBlock(x, y + 1, z, EndExpansion.fortressBrick, 0, 2);
        world.setBlock(x + 1, y + 1, z, EndExpansion.fortressBrick, 0, 2);

        world.setBlock(x - 1, y + 2, z + 1, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x, y + 2, z + 1, EndExpansion.fortressBrick, 0, 2);
        world.setBlock(x + 1, y + 2, z + 1, EndExpansion.fortressPillar, 0, 2);
        world.setBlock(x, y + 4, z + 1, EndExpansion.seaCrystal, 0, 2);

        world.setBlock(x - 1, y + 2, z, EndExpansion.fortressBrick, 0, 2);
        world.setBlock(x + 1, y + 2, z, EndExpansion.fortressBrick, 0, 2);
    }

    private void generateBarracks(World world, Random rand, int x, int y, int z) {
        int w = 9, h = 6, d = 13;
        int ox = x - w / 2;
        int oz = z - d / 2;

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(ox + dx, y - 1, oz + dz, EndExpansion.fortressBrick, 0, 2);
                anchorDown(world, ox + dx, y - 2, oz + dz, EndExpansion.fortressPillar, 10);
            }
        }

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                world.setBlock(ox + dx, y + dy, oz, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + dx, y + dy, oz + d - 1, EndExpansion.fortressBrick, 0, 2);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                world.setBlock(ox, y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(ox + w - 1, y + dy, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(ox + dx, y + h, oz + dz, EndExpansion.fortressBrick, 0, 2);
            }
        }

        world.setBlock(x, y, oz, Blocks.air, 0, 2);
        world.setBlock(x, y + 1, oz, Blocks.air, 0, 2);

        for (int i = 1; i < d - 1; i += 3) {
            world.setBlock(ox + 1, y + 1, oz + i, Blocks.chest, 0, 2);
            fillBarracksChest(world, rand, ox + 1, y + 1, oz + i);
            world.setBlock(ox + w - 2, y + 1, oz + i, Blocks.chest, 0, 2);
            fillBarracksChest(world, rand, ox + w - 2, y + 1, oz + i);
        }

        world.setBlock(x, y + 1, oz + d - 6, Blocks.mob_spawner, 0, 2);
        TileEntityMobSpawner spawner = (TileEntityMobSpawner) world.getTileEntity(x, y + 1, oz + d - 6);
        if (spawner != null) {
            net.minecraft.nbt.NBTTagCompound nbt = new net.minecraft.nbt.NBTTagCompound();
            spawner.writeToNBT(nbt);
            nbt.getCompoundTag("SpawnData").setString("id", "Enderman");
            spawner.readFromNBT(nbt);
        }
    }

    private void generateFortressPath(World world,
                                      int x1, int y1, int z1,
                                      int x2, int y2, int z2) {
        int dx = x2 - x1;
        int dz = z2 - z1;
        int steps = Math.max(1, (int) Math.sqrt(dx * dx + dz * dz));

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / (float) steps;
            int px = x1 + (int) (dx * t);
            int pz = z1 + (int) (dz * t);
            int py = stableSurfaceY(world, px, pz, y1);

            for (int ox = 0; ox <= 1; ox++) {
                for (int oz = 0; oz <= 1; oz++) {
                    world.setBlock(px + ox, py - 1, pz + oz, EndExpansion.fortressBrick, 0, 2);
                    anchorDown(world, px + ox, py - 2, pz + oz, EndExpansion.fortressPillar, 8);
                }
            }
        }
    }

    private void generateRuinedOutposts(World world, Random rand, int cx, int y, int cz, int radius) {
        int count = 2 + rand.nextInt(3);
        for (int i = 0; i < count; i++) {
            float angle = rand.nextFloat() * (float) (Math.PI * 2);
            int dist = radius - 2 + rand.nextInt(7);
            int x = cx + (int) (Math.cos(angle) * dist);
            int z = cz + (int) (Math.sin(angle) * dist);
            int py = stableSurfaceY(world, x, z, y);

            int pillarH = 4 + rand.nextInt(4);
            for (int h = 0; h < pillarH; h++) {
                world.setBlock(x, py + h, z, EndExpansion.fortressPillar, 0, 2);
            }
            world.setBlock(x, py + pillarH, z, EndExpansion.endTorch, 0, 2);
            anchorDown(world, x, py - 1, z, EndExpansion.fortressPillar, 12);

            if (rand.nextBoolean()) {
                world.setBlock(x + 1, py, z, EndExpansion.fortressBrick, 0, 2);
                world.setBlock(x - 1, py, z, EndExpansion.fortressBrick, 0, 2);
            }
        }
    }

    private int stableSurfaceY(World world, int x, int z, int fallbackY) {
        int y = world.getTopSolidOrLiquidBlock(x, z);
        if (y < fallbackY - 6) return fallbackY - 6;
        if (y > fallbackY + 6) return fallbackY + 6;
        return y;
    }

    private void anchorDown(World world, int x, int startY, int z, Block block, int maxDepth) {
        int depth = 0;
        int y = startY;
        while (y > 8 && depth < maxDepth) {
            Block existing = world.getBlock(x, y, z);
            if (existing != Blocks.air && existing != Blocks.water) break;
            world.setBlock(x, y, z, block, 0, 2);
            y--;
            depth++;
        }
    }

    private void fillTowerChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.iron_ingot, 2 + rand.nextInt(4)));
        chest.setInventorySlotContents(1, new ItemStack(Items.ender_pearl, 1 + rand.nextInt(3)));
        chest.setInventorySlotContents(2, new ItemStack(Items.arrow, 4 + rand.nextInt(8)));
    }

    private void fillBarracksChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.iron_sword, 1));
        chest.setInventorySlotContents(1, new ItemStack(Items.iron_helmet, 1));
        chest.setInventorySlotContents(2, new ItemStack(Items.bread, 2 + rand.nextInt(3)));
        chest.setInventorySlotContents(3, new ItemStack(Items.ender_pearl, 1 + rand.nextInt(2)));
    }

    private void fillBossChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.diamond, 1 + rand.nextInt(3)));
        chest.setInventorySlotContents(1, new ItemStack(Items.golden_apple, 1));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl, 2 + rand.nextInt(4)));
        chest.setInventorySlotContents(3, new ItemStack(Items.diamond_sword, 1));
        chest.setInventorySlotContents(4, new ItemStack(Items.diamond_helmet, 1));
    }
}
