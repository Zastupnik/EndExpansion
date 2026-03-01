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

        generateOceanBasins(world, rand, centerX, centerZ, Math.max(16, radius - 6));

        // 1. Пирс с домиком — 1-3 штуки
        int pierCount = 1 + rand.nextInt(3);
        for (int i = 0; i < pierCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, Math.max(10, radius / 2));
            int shoreY = findShoreY(world, pos[0], pos[1]);
            if (shoreY > 0) {
                generatePierWithHouse(world, rand, pos[0], shoreY, pos[1]);
            }
        }

        int shipCount = 2 + rand.nextInt(3);
        for (int i = 0; i < shipCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, Math.max(12, radius - 4));
            int y = findWaterSurface(world, pos[0], pos[1]);
            if (y > 40) {
                generateSunkenShip(world, rand, pos[0], y, pos[1]);
            }
        }

    private void generateOceanBasins(World world, Random rand, int centerX, int centerZ, int radius) {
        int basinCount = 3 + rand.nextInt(3);
        for (int i = 0; i < basinCount; i++) {
            int[] pos = randomPos(rand, centerX, centerZ, radius);
            int y = world.getTopSolidOrLiquidBlock(pos[0], pos[1]);
            int r = 5 + rand.nextInt(4);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > r * r) continue;
                    world.setBlock(pos[0] + dx, y - 1, pos[1] + dz, Blocks.water, 0, 2);
                    if (rand.nextInt(7) == 0) {
                        setIfAir(world, pos[0] + dx, y, pos[1] + dz, EndExpansion.seaCrystal);
                    }
                }
            }
        }
    }

    // ===== ПИРС С ДОМИКОМ =====

    private void generatePierWithHouse(World world, Random rand, int x, int y, int z) {
        int pierLength = 7 + rand.nextInt(6);
        int dir = rand.nextInt(4);
        int dirX = dir == 0 ? 1 : (dir == 1 ? -1 : 0);
        int dirZ = dir == 2 ? 1 : (dir == 3 ? -1 : 0);

        for (int i = 0; i < pierLength; i++) {
            int px = x + dirX * i;
            int pz = z + dirZ * i;
            int waterY = findWaterSurface(world, px, pz);
            if (waterY < 0) break;

            int deckY = waterY + 1;
            world.setBlock(px, deckY, pz, EndExpansion.witheredLog, 0, 2);
            supportToFloor(world, px, deckY - 1, pz, EndExpansion.witheredLog, 14);

            if ((i & 1) == 0) {
                if (dirX != 0) {
                    placeLanternPost(world, px, deckY, pz - 1);
                    placeLanternPost(world, px, deckY, pz + 1);
                } else {
                    placeLanternPost(world, px - 1, deckY, pz);
                    placeLanternPost(world, px + 1, deckY, pz);
                }
            }
        }

        int houseX = x + dirX * pierLength;
        int houseZ = z + dirZ * pierLength;
        int houseY = findShoreY(world, houseX, houseZ);
        if (houseY > 0) {
            generatePierHouse(world, rand, houseX - 2, houseY, houseZ - 2);
        }
    }

    private void generatePierHouse(World world, Random rand, int x, int y, int z) {
        int w = 6, h = 5, d = 6;

        for (int dx = 0; dx < w; dx++) {
            for (int dz = 0; dz < d; dz++) {
                world.setBlock(x + dx, y - 1, z + dz, EndExpansion.witheredLog, 0, 2);
                supportToFloor(world, x + dx, y - 2, z + dz, EndExpansion.witheredLog, 10);
            }
        }

        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                world.setBlock(x + dx, y + dy, z, EndExpansion.witheredLog, 0, 2);
                world.setBlock(x + dx, y + dy, z + d - 1, EndExpansion.witheredLog, 0, 2);
            }
            for (int dz = 1; dz < d - 1; dz++) {
                world.setBlock(x, y + dy, z + dz, EndExpansion.witheredLog, 0, 2);
                world.setBlock(x + w - 1, y + dy, z + dz, EndExpansion.witheredLog, 0, 2);
            }
        }

        world.setBlock(x + w / 2, y, z, Blocks.air, 0, 2);
        world.setBlock(x + w / 2, y + 1, z, Blocks.air, 0, 2);

        for (int dx = -1; dx <= w; dx++) {
            for (int dz = -1; dz <= d; dz++) {
                if (Math.abs(dx - (w / 2)) + Math.abs(dz - (d / 2)) < 6) {
                    world.setBlock(x + dx, y + h, z + dz, EndExpansion.oceanStone, 0, 2);
                }
            }
        }

        world.setBlock(x + 1, y + 2, z + 1, EndExpansion.endTorch, 0, 2);
        world.setBlock(x + w - 2, y + 2, z + d - 2, EndExpansion.endTorch, 0, 2);

        world.setBlock(x + w / 2, y + 1, z + d / 2, Blocks.chest, 0, 2);
        fillOceanChest(world, rand, x + w / 2, y + 1, z + d / 2);
    }

    private void generateSunkenShip(World world, Random rand, int x, int y, int z) {
        int length = 10 + rand.nextInt(9);
        int width = 4;
        int dirX = rand.nextBoolean() ? 1 : 0;
        int dirZ = dirX == 0 ? 1 : 0;

        for (int i = 0; i < length; i++) {
            int sx = x + dirX * i;
            int sz = z + dirZ * i;
            int waterY = findWaterSurface(world, sx, sz);
            if (waterY < 0) continue;

            int hullY = waterY - 2 + (i < length / 3 ? 1 : 0) - (i > (length * 2) / 3 ? 1 : 0);
            for (int w = -width / 2; w <= width / 2; w++) {
                int px = sx + (dirZ * w);
                int pz = sz + (dirX * w);

                if (!isOpenWaterColumn(world, px, hullY, pz, 2)) continue;

                world.setBlock(px, hullY - 1, pz, EndExpansion.witheredLog, 0, 2);
                world.setBlock(px, hullY, pz, EndExpansion.witheredLog, 0, 2);
                if (Math.abs(w) == width / 2) {
                    world.setBlock(px, hullY + 1, pz, EndExpansion.witheredLog, 0, 2);
                }
            }
        }

        int trunkX = x + dirX * (length / 2);
        int trunkZ = z + dirZ * (length / 2);
        int trunkY = findWaterSurface(world, trunkX, trunkZ) - 1;
        if (trunkY > 35) {
            world.setBlock(trunkX, trunkY, trunkZ, Blocks.chest, 0, 2);
            fillShipChest(world, rand, trunkX, trunkY, trunkZ);
        }
    }

    private void generateCoralReefs(World world, Random rand, int cx, int cz, int radius) {
        int reefCount = Math.max(6, radius / 8);
        for (int i = 0; i < reefCount; i++) {
            int[] pos = randomPos(rand, cx, cz, radius);
            int waterY = findWaterSurface(world, pos[0], pos[1]);
            if (waterY < 0) continue;

            int floorY = findFloorUnderWater(world, pos[0], waterY, pos[1], 12);
            if (floorY < 0) continue;

            int r = 1 + rand.nextInt(3);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > r * r) continue;
                    int x = pos[0] + dx;
                    int z = pos[1] + dz;
                    int fy = findFloorUnderWater(world, x, waterY, z, 8);
                    if (fy < 0) continue;

                    Block block = ((dx + dz + i) & 1) == 0
                            ? EndExpansion.coralStoneEnd
                            : EndExpansion.seaCrystal;
                    world.setBlock(x, fy + 1, z, block, 0, 2);
                }
            }
        }
    }

    private void placeLanternPost(World world, int x, int y, int z) {
        if (!world.isAirBlock(x, y + 1, z)) return;
        world.setBlock(x, y + 1, z, EndExpansion.ashenStone, 0, 2);
        if (world.isAirBlock(x, y + 2, z)) {
            world.setBlock(x, y + 2, z, EndExpansion.endTorch, 0, 2);
        }
    }

    private void supportToFloor(World world, int x, int startY, int z, Block block, int maxDepth) {
        int y = startY;
        int depth = 0;
        while (y > 8 && depth < maxDepth) {
            Block below = world.getBlock(x, y, z);
            if (below != Blocks.water && below != Blocks.air) {
                return;
            }
            world.setBlock(x, y, z, block, 0, 2);
            y--;
            depth++;
        }
    }

    private boolean isOpenWaterColumn(World world, int x, int y, int z, int checksUp) {
        for (int i = 0; i <= checksUp; i++) {
            Block block = world.getBlock(x, y + i, z);
            if (block != Blocks.water && block != Blocks.air) return false;
        }
        return true;
    }

    private int findShoreY(World world, int x, int z) {
        int top = world.getTopSolidOrLiquidBlock(x, z);
        if (top < 35) return -1;
        if (world.getBlock(x, top - 1, z) == Blocks.water) {
            // ищем ближайший берег в малом радиусе
            for (int r = 1; r <= 4; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        int y = world.getTopSolidOrLiquidBlock(x + dx, z + dz);
                        if (y > 30 && world.getBlock(x + dx, y - 1, z + dz) != Blocks.water) {
                            return y;
                        }
                    }
                }
            }
            return -1;
        }
        return top;
    }

    private int findWaterSurface(World world, int x, int z) {
        for (int y = 110; y > 25; y--) {
            if (world.getBlock(x, y, z) == Blocks.water) return y;
        }
        return -1;
    }

    private int findFloorUnderWater(World world, int x, int startY, int z, int maxDepth) {
        for (int d = 0; d < maxDepth; d++) {
            int y = startY - d;
            Block block = world.getBlock(x, y, z);
            if (block != Blocks.water && block != Blocks.air) {
                return y;
            }
        }
        return -1;
    }

    private int[] randomPos(Random rand, int centerX, int centerZ, int radius) {
        int angle = rand.nextInt(360);
        int dist = 4 + rand.nextInt(Math.max(1, radius - 3));
        return new int[]{
                centerX + (int) (Math.cos(Math.toRadians(angle)) * dist),
                centerZ + (int) (Math.sin(Math.toRadians(angle)) * dist)
        };
    }

    private void fillOceanChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.fishing_rod, 1));
        chest.setInventorySlotContents(1, new ItemStack(Items.fish, 2 + rand.nextInt(4)));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl, 1 + rand.nextInt(2)));
        chest.setInventorySlotContents(3, new ItemStack(EndExpansion.seaCrystal, 1 + rand.nextInt(3)));
    }

    private void fillShipChest(World world, Random rand, int x, int y, int z) {
        TileEntityChest chest = (TileEntityChest) world.getTileEntity(x, y, z);
        if (chest == null) return;
        chest.setInventorySlotContents(0, new ItemStack(Items.gold_ingot, 1 + rand.nextInt(4)));
        chest.setInventorySlotContents(1, new ItemStack(Items.diamond, rand.nextInt(2)));
        chest.setInventorySlotContents(2, new ItemStack(Items.ender_pearl, 1 + rand.nextInt(3)));
        chest.setInventorySlotContents(3, new ItemStack(Items.compass, 1));
        chest.setInventorySlotContents(4, new ItemStack(Items.map, 1));
    }
}
