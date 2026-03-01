package com.zastupnik.endexpansion.world.gen;

import com.zastupnik.endexpansion.EndExpansion;
import com.zastupnik.endexpansion.world.biome.EndBiomes;
import com.zastupnik.endexpansion.world.decoration.*;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.Random;

public class EndIslandGenerator {

    // ===== ТОЧКА ВХОДА =====

    public void generateIsland(World world, Random rand, int x, int z, BiomeGenBase biome, int baseRadius) {
        Block topBlock    = getTopBlock(biome);
        Block fillerBlock = getFillerBlock(biome);
        int y = getIslandY(rand); // 60–160 для всех биомов

        int thickness = pickThickness(rand); // 3–40 блоков высоты

        generateShape(world, rand, x, y, z, baseRadius, thickness, topBlock, fillerBlock, biome);

        // Декор СТРОГО после блоков — иначе флора выпадает предметами
        decorateIsland(world, rand, x, y, z, biome, baseRadius);

        if (rand.nextInt(3) != 0) spawnEndermen(world, rand, x, y, z, baseRadius);
    }

    // ===== БЛОКИ БИОМОВ =====

    private Block getTopBlock(BiomeGenBase biome) {
        if (biome == EndBiomes.biomeCemetery)  return EndExpansion.deadGrass;
        if (biome == EndBiomes.biomeDesert)    return EndExpansion.endSand;
        if (biome == EndBiomes.biomeForest)    return EndExpansion.forestMoss;
        if (biome == EndBiomes.biomeInfection) return EndExpansion.infestedMycelium;
        if (biome == EndBiomes.biomeJungle)    return EndExpansion.jungleTurf;
        if (biome == EndBiomes.biomeOcean)     return EndExpansion.oceanStone;
        if (biome == EndBiomes.biomeFortress)  return EndExpansion.fortressBrick;
        return Blocks.end_stone;
    }

    private Block getFillerBlock(BiomeGenBase biome) {
        if (biome == EndBiomes.biomeCemetery)  return EndExpansion.ashenStone;
        if (biome == EndBiomes.biomeDesert)    return EndExpansion.sandstoneEnd;
        if (biome == EndBiomes.biomeForest)    return EndExpansion.ashenStone;
        if (biome == EndBiomes.biomeInfection) return EndExpansion.pulsingRock;
        if (biome == EndBiomes.biomeJungle)    return EndExpansion.ashenStone;
        if (biome == EndBiomes.biomeOcean)     return EndExpansion.oceanStone;
        if (biome == EndBiomes.biomeFortress)  return EndExpansion.fortressPillar;
        return Blocks.end_stone;
    }

    // ===== ВЫСОТА И ТОЛЩИНА =====

    private int getIslandY(Random rand) {
        return 60 + rand.nextInt(101); // 60–160
    }

    /**
     * Толщина острова вниз:
     *  50% — тонкая плита (3–10): парящие камни, выглядит атмосферно
     *  30% — средний (10–25)
     *  20% — толстый (25–45): массивные острова
     */
    private int pickThickness(Random rand) {
        int r = rand.nextInt(10);
        if (r < 5) return 3  + rand.nextInt(8);  // 3–10
        if (r < 8) return 10 + rand.nextInt(16); // 10–25
        return 25 + rand.nextInt(21);             // 25–45
    }

    // ===== ВЫБОР ФОРМЫ =====

    private void generateShape(World world, Random rand, int cx, int cy, int cz,
                               int baseRadius, int thickness,
                               Block topBlock, Block fillerBlock, BiomeGenBase biome) {
        int roll = rand.nextInt(12);
        if      (roll < 2)  generateOval     (world, rand, cx, cy, cz, baseRadius, thickness, topBlock, fillerBlock, biome);
        else if (roll < 4)  generateLShape   (world, rand, cx, cy, cz, baseRadius, thickness, topBlock, fillerBlock, biome);
        else if (roll < 6)  generateCrescent (world, rand, cx, cy, cz, baseRadius, thickness, topBlock, fillerBlock, biome);
        else if (roll < 8)  generateRidge    (world, rand, cx, cy, cz, baseRadius, thickness, topBlock, fillerBlock, biome);
        else if (roll < 10) generateBlob     (world, rand, cx, cy, cz, baseRadius, thickness, topBlock, fillerBlock, biome);
        else                generateComposite(world, rand, cx, cy, cz, baseRadius, thickness, topBlock, fillerBlock, biome);
    }

    // ===== ФОРМЫ =====

    /**
     * Эллипс с рандомным соотношением сторон и поворотом.
     * scaleX/Z от 0.5 до 2.5 → можно получить 300×120 из радиуса 150.
     */
    private void generateOval(World world, Random rand, int cx, int cy, int cz,
                              int R, int thickness,
                              Block top, Block filler, BiomeGenBase biome) {
        float scaleX = 0.5F + rand.nextFloat() * 2.0F;
        float scaleZ = 0.5F + rand.nextFloat() * 2.0F;
        float angle  = rand.nextFloat() * (float)Math.PI;
        float cosA   = (float)Math.cos(angle);
        float sinA   = (float)Math.sin(angle);

        int extX = (int)(R * scaleX + 2);
        int extZ = (int)(R * scaleZ + 2);

        for (int dy = 0; dy < thickness; dy++) {
            float shrink = 1.0F - (float)dy / thickness * 0.85F;
            float rx = R * scaleX * shrink;
            float rz = R * scaleZ * shrink;
            if (rx < 1 || rz < 1) break;

            for (int dx = -extX; dx <= extX; dx++) {
                for (int dz = -extZ; dz <= extZ; dz++) {
                    float lx =  dx * cosA + dz * sinA;
                    float lz = -dx * sinA + dz * cosA;
                    float check = (lx * lx) / (rx * rx) + (lz * lz) / (rz * rz);
                    if (check > 1.0F) continue;
                    if (check > 0.82F && rand.nextFloat() < (check - 0.82F) * 5F) continue;
                    placeBlock(world, cx + dx, cy - dy, cz + dz, dy == 0 ? top : filler, dy == 0, biome);
                }
            }
        }
        if (biome == EndBiomes.biomeOcean) fillOceanBasin(world, rand, cx, cy, cz, (int)(R * Math.min(scaleX, scaleZ) * 0.5F));
    }

    /**
     * Г-образный остров. Два прямоугольных плеча с рваными краями.
     */
    private void generateLShape(World world, Random rand, int cx, int cy, int cz,
                                int R, int thickness,
                                Block top, Block filler, BiomeGenBase biome) {
        float angle = rand.nextFloat() * (float)(Math.PI * 2);
        float cosA  = (float)Math.cos(angle);
        float sinA  = (float)Math.sin(angle);

        // Плечо 1 — длинное горизонтальное
        float arm1HalfLen = R * (1.0F + rand.nextFloat() * 1.0F); // R–2R
        float arm1HalfWid = R * (0.3F + rand.nextFloat() * 0.3F); // 0.3R–0.6R

        // Плечо 2 — перпендикулярное, начинается с одного конца плеча 1
        float arm2HalfLen = R * (0.6F + rand.nextFloat() * 0.8F);
        float arm2HalfWid = R * (0.3F + rand.nextFloat() * 0.3F);
        // Смещение центра плеча 2 вдоль плеча 1 и поперёк
        float arm2OffAlong = arm1HalfLen - arm2HalfWid;
        float arm2OffPerp  = arm2HalfLen;

        for (int dy = 0; dy < thickness; dy++) {
            float s = 1.0F - (float)dy / thickness * 0.8F;

            int ext = (int)(Math.max(arm1HalfLen, arm2OffAlong + arm2HalfWid) + 4);
            for (int dx = -ext; dx <= ext; dx++) {
                for (int dz = -ext; dz <= ext; dz++) {
                    // Переходим в локальную систему (вдоль, поперёк)
                    float along =  dx * cosA + dz * sinA;
                    float perp  = -dx * sinA + dz * cosA;

                    boolean inArm1 = Math.abs(along) < arm1HalfLen * s && Math.abs(perp) < arm1HalfWid * s;
                    boolean inArm2 = Math.abs(along - arm2OffAlong * s) < arm2HalfWid * s
                            && Math.abs(perp  - arm2OffPerp  * s) < arm2HalfLen * s;

                    if (!inArm1 && !inArm2) continue;
                    if (rand.nextInt(10) == 0) continue; // Рваные края

                    placeBlock(world, cx + dx, cy - dy, cz + dz, dy == 0 ? top : filler, dy == 0, biome);
                }
            }
        }
    }

    /**
     * Полумесяц / дуга.
     */
    private void generateCrescent(World world, Random rand, int cx, int cy, int cz,
                                  int R, int thickness,
                                  Block top, Block filler, BiomeGenBase biome) {
        float outerR   = R * (1.0F + rand.nextFloat() * 0.5F);
        float innerR   = outerR * (0.35F + rand.nextFloat() * 0.3F);
        float cutAngle = rand.nextFloat() * (float)(Math.PI * 2);
        float cutArc   = (float)Math.PI * (0.4F + rand.nextFloat() * 0.7F);

        int ext = (int)(outerR + 2);

        for (int dy = 0; dy < thickness; dy++) {
            float s = 1.0F - (float)dy / thickness * 0.8F;
            float orS = outerR * s;
            float irS = innerR * s;

            for (int dx = -ext; dx <= ext; dx++) {
                for (int dz = -ext; dz <= ext; dz++) {
                    float dist  = (float)Math.sqrt(dx * dx + dz * dz);
                    if (dist > orS) continue;

                    float ang = (float)Math.atan2(dz, dx);
                    float diff = Math.abs(ang - cutAngle);
                    if (diff > Math.PI) diff = (float)(Math.PI * 2) - diff;

                    if (diff < cutArc / 2 && dist > irS) continue;

                    float edge = dist / orS;
                    if (edge > 0.82F && rand.nextFloat() < (edge - 0.82F) * 5F) continue;

                    placeBlock(world, cx + dx, cy - dy, cz + dz, dy == 0 ? top : filler, dy == 0, biome);
                }
            }
        }
    }

    /**
     * Хребет — длинный вытянутый остров с волнистым Y-профилем и переменной шириной.
     */
    private void generateRidge(World world, Random rand, int cx, int cy, int cz,
                               int R, int thickness,
                               Block top, Block filler, BiomeGenBase biome) {
        float angle = rand.nextFloat() * (float)Math.PI;
        float cosA  = (float)Math.cos(angle);
        float sinA  = (float)Math.sin(angle);

        int   length = (int)(R * (1.5F + rand.nextFloat() * 1.5F)); // 1.5R–3R
        float width  = R * (0.15F + rand.nextFloat() * 0.25F);       // Узкий

        for (int step = -length; step <= length; step++) {
            float t = (float)step / length; // -1..1

            // Y волнится вдоль хребта
            int yOff = (int)(Math.sin(t * Math.PI * (1 + rand.nextInt(3))) * thickness * 0.35F);
            // Ширина в этой точке — уже к краям
            float localW = width * (1.0F - Math.abs(t) * 0.5F) * (0.7F + rand.nextFloat() * 0.6F);
            // Толщина тоже волнится
            int localThick = Math.max(2, (int)(thickness * (0.6F + Math.cos(t * Math.PI * 2) * 0.4F)));

            int segX = cx + (int)(step * cosA);
            int segZ = cz + (int)(step * sinA);

            for (int w = -(int)localW; w <= (int)localW; w++) {
                int px = segX + (int)(-w * sinA);
                int pz = segZ + (int)( w * cosA);
                float edge = Math.abs((float)w / Math.max(1, localW));

                for (int dy = 0; dy < localThick; dy++) {
                    if (edge > 0.8F && rand.nextFloat() < (edge - 0.8F) * 4F) continue;
                    int ry = cy - dy + yOff;
                    placeBlock(world, px, ry, pz, dy == 0 ? top : filler, dy == 0, biome);
                }
            }
        }
    }

    /**
     * Blob: несколько перекрывающихся кругов = органичная неправильная форма.
     */
    private void generateBlob(World world, Random rand, int cx, int cy, int cz,
                              int R, int thickness,
                              Block top, Block filler, BiomeGenBase biome) {
        int count = 4 + rand.nextInt(6); // 4–9 кругов
        int[] bx = new int[count];
        int[] bz = new int[count];
        int[] br = new int[count];

        bx[0] = 0; bz[0] = 0;
        br[0] = (int)(R * (0.5F + rand.nextFloat() * 0.5F));

        for (int i = 1; i < count; i++) {
            float a = rand.nextFloat() * (float)(Math.PI * 2);
            float d = br[i-1] * (0.4F + rand.nextFloat() * 0.8F);
            bx[i] = bx[i-1] + (int)(Math.cos(a) * d);
            bz[i] = bz[i-1] + (int)(Math.sin(a) * d);
            br[i] = (int)(R * (0.25F + rand.nextFloat() * 0.5F));
        }

        int ext = R * 3;
        for (int dy = 0; dy < thickness; dy++) {
            float s = 1.0F - (float)dy / thickness * 0.85F;

            for (int dx = -ext; dx <= ext; dx++) {
                for (int dz = -ext; dz <= ext; dz++) {
                    float minEdge = 2.0F;
                    for (int i = 0; i < count; i++) {
                        float r = br[i] * s;
                        if (r < 1) continue;
                        int ddx = dx - bx[i], ddz = dz - bz[i];
                        float dist = (float)Math.sqrt(ddx * ddx + ddz * ddz);
                        if (dist < r) minEdge = Math.min(minEdge, dist / r);
                    }
                    if (minEdge > 1.0F) continue;
                    if (minEdge > 0.82F && rand.nextFloat() < (minEdge - 0.82F) * 5F) continue;
                    placeBlock(world, cx + dx, cy - dy, cz + dz, dy == 0 ? top : filler, dy == 0, biome);
                }
            }
        }
        if (biome == EndBiomes.biomeOcean) fillOceanBasin(world, rand, cx, cy, cz, R / 2);
    }

    /**
     * Составной: blob + ridge с небольшим смещением и разной высотой.
     * Дают самые интересные силуэты типа Г, Т, S.
     */
    private void generateComposite(World world, Random rand, int cx, int cy, int cz,
                                   int R, int thickness,
                                   Block top, Block filler, BiomeGenBase biome) {
        // Часть 1 — blob
        int r1 = (int)(R * (0.6F + rand.nextFloat() * 0.4F));
        generateBlob(world, rand, cx, cy, cz, r1, thickness, top, filler, biome);

        // Часть 2 — ridge или ещё один blob, со смещением
        float a  = rand.nextFloat() * (float)(Math.PI * 2);
        int off  = (int)(r1 * (0.4F + rand.nextFloat() * 0.6F));
        int cx2  = cx + (int)(Math.cos(a) * off);
        int cz2  = cz + (int)(Math.sin(a) * off);
        int r2   = (int)(R * (0.4F + rand.nextFloat() * 0.5F));
        int t2   = Math.max(3, thickness - rand.nextInt(Math.max(1, thickness / 3)));
        int yOff = rand.nextInt(15) - 7; // Вторая часть может быть на другой высоте

        if (rand.nextBoolean()) {
            generateRidge(world, rand, cx2, cy + yOff, cz2, r2, t2, top, filler, biome);
        } else {
            generateBlob(world, rand, cx2, cy + yOff, cz2, r2, t2, top, filler, biome);
        }
    }

    // ===== УТИЛИТЫ СЛОЁВ =====

    private void placeBlock(World world, int x, int y, int z, Block block, boolean isTop, BiomeGenBase biome) {
        world.setBlock(x, y, z, block, 0, 2);
        if (isTop) setBiomeAt(world, x, z, biome);
    }

    // ===== ОКЕАН =====

    private void fillOceanBasin(World world, Random rand, int cx, int cy, int cz, int radius) {
        int r = Math.max(4, radius);
        int waterLevel = cy - 1;
        int depth = 4 + rand.nextInt(4);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz <= r * r) {
                    for (int dy = 0; dy < depth; dy++)
                        world.setBlock(cx + dx, waterLevel - dy, cz + dz, Blocks.air, 0, 2);
                    for (int dy = 0; dy < depth - 1; dy++)
                        if (world.isAirBlock(cx + dx, waterLevel - dy, cz + dz))
                            world.setBlock(cx + dx, waterLevel - dy, cz + dz, Blocks.water, 0, 2);
                }
            }
        }
    }

    // ===== ДЕКОР =====

    private void decorateIsland(World world, Random rand, int cx, int cy, int cz,
                                BiomeGenBase biome, int radius) {
        if      (biome == EndBiomes.biomeCemetery)  new DecoratorCemetery().decorate(world, rand, cx, cy, cz, radius);
        else if (biome == EndBiomes.biomeFortress)  new DecoratorFortress().decorate(world, rand, cx, cy, cz, radius);
        else if (biome == EndBiomes.biomeOcean)     new DecoratorOcean().decorate(world, rand, cx, cy, cz, radius);
        else if (biome == EndBiomes.biomeDesert)    new DecoratorDesert().decorate(world, rand, cx, cy, cz, radius);
        else if (biome == EndBiomes.biomeForest)    new DecoratorForest().decorate(world, rand, cx, cy, cz, radius);
        else if (biome == EndBiomes.biomeJungle)    new DecoratorJungle().decorate(world, rand, cx, cy, cz, radius);
        else if (biome == EndBiomes.biomeInfection) new DecoratorInfection().decorate(world, rand, cx, cy, cz, radius);
    }

    // ===== ЭНДЕРМЕНЫ =====

    private void spawnEndermen(World world, Random rand, int cx, int cy, int cz, int radius) {
        int count = 1 + rand.nextInt(3);
        for (int i = 0; i < count; i++) {
            float a = rand.nextFloat() * (float)(Math.PI * 2);
            float d = rand.nextFloat() * radius * 0.6F;
            int ex = cx + (int)(Math.cos(a) * d);
            int ez = cz + (int)(Math.sin(a) * d);
            int ey = cy + 3;
            while (ey > cy - 5 && world.isAirBlock(ex, ey, ez)) ey--;
            ey++;
            if (world.isAirBlock(ex, ey, ez)) continue;
            EntityEnderman e = new EntityEnderman(world);
            e.setPosition(ex + 0.5, ey, ez + 0.5);
            world.spawnEntityInWorld(e);
        }
    }

    // ===== ДЕРЕВЬЯ (публичные — используются декораторами) =====

    public void generateGnarledTree(World world, Random rand, int x, int y, int z) {
        int height = 10 + rand.nextInt(8);
        int sx = x, sz = z;
        for (int i = 0; i < height; i++) {
            world.setBlock(sx, y + i, sz, EndExpansion.ancientLog, 0, 2);
            if (i > 3 && rand.nextInt(4) == 0) { sx += rand.nextInt(3)-1; sz += rand.nextInt(3)-1; }
        }
        for (int bx = -1; bx <= 1; bx++)
            for (int bz = -1; bz <= 1; bz++)
                if (rand.nextInt(3) != 0) world.setBlock(x+bx, y, z+bz, EndExpansion.ancientLog, 0, 2);

        int branchCount = 3 + rand.nextInt(3);
        for (int b = 0; b < branchCount; b++) {
            int branchY = y + height/2 + rand.nextInt(height/2);
            int bdx = rand.nextInt(3)-1, bdz = rand.nextInt(3)-1;
            int blen = 3 + rand.nextInt(4);
            int bx = sx, bz = sz;
            for (int bl = 0; bl < blen; bl++) {
                bx += bdx; bz += bdz;
                world.setBlock(bx, branchY + (bl > blen/2 ? 1 : 0), bz, EndExpansion.ancientLog, 0, 2);
            }
            for (int lx=-2; lx<=2; lx++) for (int lz=-2; lz<=2; lz++) for (int ly=-1; ly<=2; ly++)
                if (lx*lx+lz*lz+ly*ly <= 6 && world.isAirBlock(bx+lx, branchY+ly, bz+lz))
                    world.setBlock(bx+lx, branchY+ly, bz+lz, EndExpansion.ancientLeaves, 0, 2);
        }
        for (int lx=-3; lx<=3; lx++) for (int lz=-3; lz<=3; lz++) for (int ly=-1; ly<=3; ly++) {
            float d = lx*lx + lz*lz + ly*ly*0.5F;
            if (d <= 10 && rand.nextInt(4) != 0 && world.isAirBlock(sx+lx, y+height+ly, sz+lz))
                world.setBlock(sx+lx, y+height+ly, sz+lz, EndExpansion.ancientLeaves, 0, 2);
        }
    }

    public void generateJungleTree(World world, Random rand, int x, int y, int z) {
        int height = 12 + rand.nextInt(7);
        for (int i = 0; i < height; i++) {
            world.setBlock(x, y+i, z, EndExpansion.tropicalLog, 0, 2);
            if (height > 14) {
                world.setBlock(x+1, y+i, z,   EndExpansion.tropicalLog, 0, 2);
                world.setBlock(x,   y+i, z+1, EndExpansion.tropicalLog, 0, 2);
                world.setBlock(x+1, y+i, z+1, EndExpansion.tropicalLog, 0, 2);
            }
        }
        int cr = 4 + rand.nextInt(3);
        for (int lx=-cr; lx<=cr; lx++) for (int lz=-cr; lz<=cr; lz++) for (int ly=-1; ly<=2; ly++) {
            float d = lx*lx + lz*lz;
            float maxR = (cr-Math.abs(ly))*(cr-Math.abs(ly));
            if (d <= maxR && rand.nextInt(5)!=0 && world.isAirBlock(x+lx, y+height+ly, z+lz))
                world.setBlock(x+lx, y+height+ly, z+lz, EndExpansion.tropicalLeaves, 0, 2);
        }
    }

    public void generateCustomTree(World world, Random rand, int x, int y, int z, Block log, Block leaves) {
        int height = 5 + rand.nextInt(4);
        for (int i = 0; i < height; i++) world.setBlock(x, y+i, z, log, 0, 2);
        for (int lx=-2; lx<=2; lx++) for (int lz=-2; lz<=2; lz++) for (int ly=height-2; ly<=height+1; ly++)
            if (Math.abs(lx)+Math.abs(lz) <= 3 && world.isAirBlock(x+lx, y+ly, z+lz))
                world.setBlock(x+lx, y+ly, z+lz, leaves, 0, 2);
    }

    // ===== УТИЛИТЫ =====

    private void setBiomeAt(World world, int x, int z, BiomeGenBase biome) {
        try {
            net.minecraft.world.chunk.Chunk chunk = world.getChunkFromBlockCoords(x, z);
            chunk.getBiomeArray()[(z & 15) << 4 | (x & 15)] = (byte) biome.biomeID;
        } catch (Exception ignored) {}
    }
}