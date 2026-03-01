package com.zastupnik.endexpansion.world.gen;

import com.zastupnik.endexpansion.EndExpansion;
import com.zastupnik.endexpansion.world.biome.EndBiomes;
import com.zastupnik.endexpansion.world.decoration.*;
import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EndIslandGenerator {

    public List<IslandNode> generateCluster(World world, Random rand,
                                            int cx, int cz, BiomeGenBase biome,
                                            int islandCount, int baseRadius) {

        List<IslandNode> nodes = new ArrayList<>();
        int mainY = getIslandY(rand);

        for (int i = 0; i < islandCount; i++) {
            int ox = cx, oz = cz, oy = mainY;

            if (i > 0) {
                float a    = rand.nextFloat() * (float)(Math.PI * 2);
                int   dist = 70 + rand.nextInt(131); // 70–200 блоков между центрами
                ox = cx + (int)(Math.cos(a) * dist);
                oz = cz + (int)(Math.sin(a) * dist);
                oy = mainY + rand.nextInt(21) - 10;  // ±10 по высоте
            }

            int radius;
            if (i == 0) {
                radius = Math.max(48, baseRadius + 8 + rand.nextInt(17));
            } else if (rand.nextInt(8) == 0) {
                // Маленькие островки только в кластерах и редко.
                radius = Math.max(24, baseRadius / 2 + rand.nextInt(8));
            } else {
                radius = Math.max(40, baseRadius + rand.nextInt(15) - 3);
            }
            int thickness = pickThickness(rand);

            Block top    = getTopBlock(biome);
            Block filler = getFillerBlock(biome, rand);

            // ===== FIX: biome ставим ОДИН РАЗ на остров, а не на каждый блок =====
            setBiomeArea(world, ox, oz, radius + 10, biome);

            // ===== генерация =====
            generateShape(world, rand, ox, oy, oz, radius, thickness, top, filler, biome);
            applySurfaceRelief(world, rand, ox, oy, oz, radius, biome);
            decorateIsland(world, rand, ox, oy, oz, biome, radius);

            if (rand.nextInt(3) != 0)
                spawnEndermen(world, rand, ox, oy, oz, radius);

            nodes.add(new IslandNode(ox, oy, oz, radius));
        }

        // Старые мосты отключены: они оставляли артефактные конструкции между островами.

        return nodes;
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

    private Block getFillerBlock(BiomeGenBase biome, Random rand) {
        if (biome == EndBiomes.biomeCemetery)  return rand.nextInt(4) == 0 ? Blocks.stonebrick : EndExpansion.ashenStone;
        if (biome == EndBiomes.biomeDesert)    return rand.nextInt(5) == 0 ? Blocks.sandstone : EndExpansion.sandstoneEnd;
        if (biome == EndBiomes.biomeForest)    return rand.nextInt(4) == 0 ? Blocks.dirt : EndExpansion.ashenStone;
        if (biome == EndBiomes.biomeInfection) return rand.nextInt(6) == 0 ? Blocks.end_stone : EndExpansion.pulsingRock;
        if (biome == EndBiomes.biomeJungle)    return rand.nextInt(4) == 0 ? Blocks.stone : EndExpansion.ashenStone;
        if (biome == EndBiomes.biomeOcean)     return rand.nextInt(4) == 0 ? Blocks.clay : EndExpansion.oceanStone;
        if (biome == EndBiomes.biomeFortress)  return rand.nextInt(3) == 0 ? Blocks.stonebrick : EndExpansion.fortressPillar;
        return Blocks.end_stone;
    }

    // ===== ВЫСОТА И ТОЛЩИНА =====

    private int getIslandY(Random rand) {
        return 72 + rand.nextInt(54); // 72–125
    }

    /**
     * Толщина острова снизу.
     * Убрали совсем тонкие (3–10) — они выглядят как платформы.
     * Теперь минимум 15, чаще 20–50.
     */
    private int pickThickness(Random rand) {
        int r = rand.nextInt(10);
        if (r < 3) return 18 + rand.nextInt(10); // 30% → 18–27
        if (r < 8) return 28 + rand.nextInt(17); // 50% → 28–44
        return 45 + rand.nextInt(16);            // 20% → 45–60
    }

    // ===== ВЫБОР ФОРМЫ =====

    private void generateShape(World world, Random rand, int cx, int cy, int cz,
                               int R, int thickness,
                               Block top, Block filler, BiomeGenBase biome) {

        int roll = rand.nextInt(20);

        boolean generated = false;

        if (roll < 2) {
            generateOval(world, rand, cx, cy, cz, R, thickness, top, filler, biome);
            generated = true;
        }
        else if (roll < 5) {
            generateCrescent(world, rand, cx, cy, cz, R, thickness, top, filler, biome);
            generated = true;
        }
        else if (roll < 9) {
            generateRidge(world, rand, cx, cy, cz, R, thickness, top, filler, biome);
            generated = true;
        }
        else if (roll < 16) {
            generateBlob(world, rand, cx, cy, cz, R, thickness, top, filler, biome);
            generated = true;
        }
        else {
            generateComposite(world, rand, cx, cy, cz, R, thickness, top, filler, biome);
            generated = true;
        }

        // ===== FAILSAFE =====
        // если какая-то форма ничего не сгенерила (баг внутри shape-методов)
        if (!generated) {
            generateBlob(world, rand, cx, cy, cz, R, thickness, top, filler, biome);
        }
    }

    // ===== ФОРМЫ =====

    /**
     * Эллипс с произвольным соотношением сторон и поворотом.
     * Нижняя часть сужается медленно — остров выглядит как каплевидная скала.
     */
    private void generateOval(World world, Random rand, int cx, int cy, int cz,
                              int R, int thickness, Block top, Block filler, BiomeGenBase biome) {
        float scaleX = 1.0F + rand.nextFloat() * 2.5F;
        float scaleZ = 1.0F + rand.nextFloat() * 2.5F;
        float angle  = rand.nextFloat() * (float)Math.PI;
        float cosA   = (float)Math.cos(angle);
        float sinA   = (float)Math.sin(angle);
        float[] noise = buildNoise(rand, 64);

        int extX = (int)(R * scaleX + 2);
        int extZ = (int)(R * scaleZ + 2);

        for (int dy = 0; dy < thickness; dy++) {
            // Верхняя треть — полный размер. Потом медленно сужается.
            float t      = Math.max(0, (float)(dy - thickness / 3) / (thickness * 0.67F));
            float shrink = 1.0F - t * 0.75F; // до 75% сужения к низу
            float rx     = R * scaleX * shrink;
            float rz     = R * scaleZ * shrink;
            if (rx < 1 || rz < 1) break;

            for (int dx = -extX; dx <= extX; dx++) {
                for (int dz = -extZ; dz <= extZ; dz++) {
                    float lx    =  dx * cosA + dz * sinA;
                    float lz    = -dx * sinA + dz * cosA;
                    float check = (lx * lx) / (rx * rx) + (lz * lz) / (rz * rz);
                    if (check > 1.0F) continue;
                    if (check > 0.80F) {
                        float n = noise[((dx & 7) << 3 | (dz & 7)) & 63];
                        if (n < (check - 0.80F) * 5F) continue;
                    }
                    placeBlock(world, cx+dx, cy-dy, cz+dz, pickBody(top, filler, dy, thickness, dx, dz), dy==0, biome);
                }
            }
        }
        if (biome == EndBiomes.biomeOcean)
            fillOceanBasin(world, rand, cx, cy, cz, (int)(R * Math.min(scaleX, scaleZ) * 0.5F));
    }

    /**
     * Г-образный остров со скруглёнными углами (superellipse).
     */
    private void generateLShape(World world, Random rand, int cx, int cy, int cz,
                                int R, int thickness, Block top, Block filler, BiomeGenBase biome) {

        // === FAILSAFE ===
        if (R < 8) R = 8;
        if (thickness < 6) thickness = 6;

        float angle = rand.nextFloat() * (float)(Math.PI * 2);
        float cosA  = (float)Math.cos(angle);
        float sinA  = (float)Math.sin(angle);

        float arm1Len = R * (1.0F + rand.nextFloat());
        float arm1Wid = R * (0.35F + rand.nextFloat() * 0.3F);
        float arm2Len = R * (0.7F + rand.nextFloat() * 0.8F);
        float arm2Wid = R * (0.35F + rand.nextFloat() * 0.3F);

        float offAlong = arm1Len - arm2Wid;
        float offPerp  = arm2Len;

        float[] noise  = buildNoise(rand, 64);
        int ext = (int)(arm1Len + arm2Len + 4);
        if (ext < 4) ext = 4;

        boolean placedAny = false;

        for (int dy = 0; dy < thickness; dy++) {
            float t = Math.max(0, (float)(dy - thickness / 3) / (thickness * 0.67F));
            float s = 1.0F - t * 0.75F;
            if (s < 0.15F) s = 0.15F;

            for (int dx = -ext; dx <= ext; dx++) {
                for (int dz = -ext; dz <= ext; dz++) {
                    float along = dx * cosA + dz * sinA;
                    float perp  = -dx * sinA + dz * cosA;

                    float d1 = softRect(along, perp, arm1Len * s, arm1Wid * s);
                    float d2 = softRect(along - offAlong * s, perp - offPerp * s, arm2Wid * s, arm2Len * s);
                    float closest = Math.min(d1, d2);
                    if (closest > 1.0F) continue;

                    // шумовой край
                    if (closest > 0.80F) {
                        float n = noise[((dx & 7) << 3 | (dz & 7)) & 63];
                        if (n < (closest - 0.80F) * 5F) continue;
                    }

                    placeBlock(world, cx + dx, cy - dy, cz + dz,
                            pickBody(top, filler, dy, thickness, dx, dz),
                            dy == 0, biome);

                    placedAny = true;
                }
            }
        }

        // === FAILSAFE: если L-образная форма не сгенерировалась ===
        if (!placedAny) {
            int rr = Math.max(6, R / 2);
            for (int dx = -rr; dx <= rr; dx++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= rr) {
                        for (int dy = 0; dy < Math.max(6, thickness / 2); dy++) {
                            placeBlock(world, cx+dx, cy-dy, cz+dz, filler, dy==0, biome);
                        }
                    }
                }
            }
        }
    }

    /**
     * Полумесяц / дуга.
     */
    private void generateCrescent(World world, Random rand, int cx, int cy, int cz,
                                  int R, int thickness, Block top, Block filler, BiomeGenBase biome) {

        if (R < 8) R = 8;
        if (thickness < 6) thickness = 6;

        float outerR   = R * (1.0F + rand.nextFloat() * 0.5F);
        float innerR   = outerR * (0.35F + rand.nextFloat() * 0.3F);
        float cutAngle = rand.nextFloat() * (float)(Math.PI * 2);
        float cutArc   = (float)Math.PI * (0.4F + rand.nextFloat() * 0.7F);
        float[] noise  = buildNoise(rand, 64);
        int ext = (int)(outerR + 2);
        if (ext < 4) ext = 4;

        boolean placedAny = false;

        for (int dy = 0; dy < thickness; dy++) {
            float t   = Math.max(0, (float)(dy - thickness / 3) / (thickness * 0.67F));
            float s   = Math.max(0.15F, 1.0F - t * 0.75F);
            float orS = outerR * s;
            float irS = innerR * s;
            float orSSq = orS * orS; // квадрат радиуса для проверки без sqrt

            for (int dx = -ext; dx <= ext; dx++) {
                for (int dz = -ext; dz <= ext; dz++) {

                    float distSq = dx*dx + dz*dz;
                    if (distSq > orSSq) continue; // быстрее чем sqrt

                    float ang  = (float)Math.atan2(dz, dx);
                    float diff = Math.abs(ang - cutAngle);
                    if (diff > Math.PI) diff = (float)(Math.PI*2) - diff;
                    if (diff < cutArc/2 && distSq > irS*irS) continue;

                    float edge = (float)Math.sqrt(distSq) / orS;
                    if (edge > 0.80F) {
                        float n = noise[((dx & 7) << 3 | (dz & 7)) & 63];
                        if (n < (edge - 0.80F) * 5F) continue;
                    }

                    placeBlock(world, cx+dx, cy-dy, cz+dz,
                            pickBody(top, filler, dy, thickness, dx, dz),
                            dy==0, biome);
                    placedAny = true;
                }
            }
        }

        // fail-safe только если не поставилось ни одного блока
        if (!placedAny) {
            int rr = Math.max(6, R / 2);
            for (int dx=-rr; dx<=rr; dx++) {
                for (int dz=-rr; dz<=rr; dz++) {
                    if (dx*dx + dz*dz <= rr*rr) {
                        for (int dy=0; dy<Math.max(6, thickness/2); dy++) {
                            placeBlock(world, cx+dx, cy-dy, cz+dz, filler, dy==0, biome);
                        }
                    }
                }
            }
        }
    }

    /**
     * Хребет с волнистым Y-профилем.
     */
    private void generateRidge(World world, Random rand, int cx, int cy, int cz,
                               int R, int thickness, Block top, Block filler, BiomeGenBase biome) {
        float angle  = rand.nextFloat() * (float)Math.PI;
        float cosA   = (float)Math.cos(angle);
        float sinA   = (float)Math.sin(angle);
        int   length = (int)(R * (1.5F + rand.nextFloat() * 1.5F));
        float width  = R * (0.2F + rand.nextFloat() * 0.3F);

        for (int step = -length; step <= length; step++) {
            float t      = (float)step / length;
            int   yOff   = (int)(Math.sin(t * Math.PI * (1 + (step & 2))) * thickness * 0.25F);
            float localW = width * (1.0F - Math.abs(t) * 0.4F);
            int localThick = Math.max(10, (int)(thickness * (0.7F + Math.cos(t * Math.PI * 2) * 0.3F)));

            int segX = cx + (int)(step * cosA);
            int segZ = cz + (int)(step * sinA);

            for (int w = -(int)localW; w <= (int)localW; w++) {
                int   px   = segX + (int)(-w * sinA);
                int   pz   = segZ + (int)( w * cosA);
                float edge = Math.abs((float)w / Math.max(1, localW));

                for (int dy = 0; dy < localThick; dy++) {
                    if (edge > 0.80F && (((px ^ pz ^ dy) & 3) < (int)((edge-0.80F)*16))) continue;
                    int ry = cy - dy + yOff;
                    placeBlock(world, px, ry, pz, pickBody(top, filler, dy, localThick, px, pz), dy==0, biome);
                }
            }
        }
    }

    /**
     * Blob: органичная форма из нескольких перекрывающихся кругов.
     * Ограничиваем ext чтобы не убить ТПС.
     */
    private void generateBlob(World world, Random rand, int cx, int cy, int cz,
                              int R, int thickness, Block top, Block filler, BiomeGenBase biome) {

        if (R < 6) R = 6;
        if (thickness < 4) thickness = 4;

        int count = 4 + rand.nextInt(5);
        int[] bx  = new int[count];
        int[] bz  = new int[count];
        int[] br  = new int[count];

        bx[0] = 0; bz[0] = 0; br[0] = Math.max(3, (int)(R*(0.5F + rand.nextFloat()*0.5F)));
        int maxExtent = br[0];

        for (int i=1;i<count;i++) {
            float a = rand.nextFloat() * (float)Math.PI*2;
            float d = Math.max(2, br[i-1]*(0.4F + rand.nextFloat()*0.7F));
            bx[i] = bx[i-1] + (int)(Math.cos(a)*d);
            bz[i] = bz[i-1] + (int)(Math.sin(a)*d);
            br[i] = Math.max(2, (int)(R*(0.3F + rand.nextFloat()*0.4F)));
            maxExtent = Math.max(maxExtent, Math.abs(bx[i])+br[i]);
            maxExtent = Math.max(maxExtent, Math.abs(bz[i])+br[i]);
        }

        maxExtent = Math.min(maxExtent+4, R*2+4);
        float[] noise = buildNoise(rand, 64);
        boolean placedAny = false;

        for (int dy=0; dy<thickness; dy++) {
            float t = Math.max(0, (float)(dy - thickness/3)/(thickness*0.67F));
            float s = 1.0F - t*0.75F;

            for (int dx=-maxExtent; dx<=maxExtent; dx++) {
                for (int dz=-maxExtent; dz<=maxExtent; dz++) {

                    float minEdge = 2.0F;
                    for (int i=0;i<count;i++) {
                        float r = br[i]*s;
                        if (r<1) continue;
                        int ddx = dx-bx[i], ddz = dz-bz[i];
                        float distSq = ddx*ddx + ddz*ddz;
                        if (distSq < r*r) minEdge = Math.min(minEdge, (float)Math.sqrt(distSq)/r);
                    }

                    if (minEdge>1.0F) continue;
                    if (minEdge>0.80F && noise[((dx &7)<<3 | (dz &7))&63] < (minEdge-0.80F)*5F) continue;

                    placeBlock(world, cx+dx, cy-dy, cz+dz,
                            pickBody(top, filler, dy, thickness, dx, dz),
                            dy==0, biome);
                    placedAny = true;
                }
            }
        }

        if (!placedAny) {
            int rr = Math.max(6, R/2);
            for (int dx=-rr; dx<=rr; dx++)
                for (int dz=-rr; dz<=rr; dz++)
                    if (dx*dx + dz*dz <= rr*rr)
                        for (int dy=0; dy<Math.max(4, thickness/2); dy++)
                            placeBlock(world, cx+dx, cy-dy, cz+dz, filler, dy==0, biome);
        }

        if (biome == EndBiomes.biomeOcean) fillOceanBasin(world, rand, cx, cy, cz, R/2);
    }

    /**
     * Составной: blob + ridge/blob с разной высотой.
     */
    private void generateComposite(World world, Random rand, int cx, int cy, int cz,
                                   int R, int thickness, Block top, Block filler, BiomeGenBase biome) {

        // === Первая часть (основа) ===
        int r1 = (int)(R * (0.6F + rand.nextFloat() * 0.4F));

        // failsafe на радиус
        if (r1 < 6) r1 = 6;

        generateBlob(world, rand, cx, cy, cz, r1, thickness, top, filler, biome);

        // === Вторая часть (пристыковка) ===
        float a   = rand.nextFloat() * (float)(Math.PI * 2);
        int off   = (int)(r1 * (0.4F + rand.nextFloat() * 0.5F));

        // failsafe на смещение
        if (off < 4) off = 4;

        int cx2   = cx + (int)(Math.cos(a) * off);
        int cz2   = cz + (int)(Math.sin(a) * off);

        int r2    = (int)(R * (0.4F + rand.nextFloat() * 0.4F));
        if (r2 < 5) r2 = 5;

        int t2    = Math.max(15, thickness - rand.nextInt(Math.max(1, thickness / 3)));
        int yOff  = rand.nextInt(16) - 8;

        boolean ridge = rand.nextBoolean();

        if (ridge) {
            generateRidge(world, rand, cx2, cy + yOff, cz2, r2, t2, top, filler, biome);
        } else {
            generateBlob(world, rand, cx2, cy + yOff, cz2, r2, t2, top, filler, biome);
        }

        // === FAILSAFE ===
        // если вдруг обе формы "ничего не нарисовали" из-за внутренних условий
        // гарантированно создаём ядро
        if (r1 <= 6 && r2 <= 5) {
            generateBlob(world, rand, cx, cy, cz, Math.max(10, R / 3), thickness, top, filler, biome);
        }
    }

    // ===== ВЫБОР БЛОКА ДЛЯ ТЕЛА =====

    /**
     * Поверхность — topBlock.
     * Нижние 3 слоя — всегда end_stone (натуральное основание).
     * Внутри — filler с редкими жилами end_stone.
     */
    private Block pickBody(Block top, Block filler, int dy, int thickness, int dx, int dz) {
        if (dy == 0) return top;

        int hash = Math.abs(dx * 7349 + dz * 9157 + dy * 1879);

        // Верхние слои мягче и ближе к биому.
        if (dy <= 2) {
            if ((hash & 15) == 0) return Blocks.end_stone;
            return filler;
        }

        // Глубже — хаотичный микс основания.
        if (dy > thickness / 3) {
            int roll = hash % 100;
            if (roll < 42) return Blocks.end_stone;
            if (roll < 67) return filler;
            if (roll < 79 && EndExpansion.fortressPillar != null) return EndExpansion.fortressPillar;
            if (roll < 88 && EndExpansion.corruptedStone != null) return EndExpansion.corruptedStone;
            if (roll < 96 && EndExpansion.obsidianEnd != null) return EndExpansion.obsidianEnd;
            if (roll < 98 && EndExpansion.pulsingRock != null) return EndExpansion.pulsingRock;
            return Blocks.bedrock;
        }

        if ((hash & 7) == 0) return Blocks.end_stone;
        return filler;
    }

    /**
     * Делает верх острова неровным: мягкие бугры и ложбины вместо плато.
     */
    private void applySurfaceRelief(World world, Random rand, int cx, int cy, int cz,
                                    int radius, BiomeGenBase biome) {
        int reliefRadius = Math.max(14, (int)(radius * 0.85F));
        double phaseX = rand.nextDouble() * Math.PI * 2.0D;
        double phaseZ = rand.nextDouble() * Math.PI * 2.0D;
        Block topBlock = getTopBlock(biome);
        Block fillerBlock = getFillerBlock(biome, rand);

        for (int dx = -reliefRadius; dx <= reliefRadius; dx++) {
            for (int dz = -reliefRadius; dz <= reliefRadius; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > reliefRadius) continue;

                int x = cx + dx;
                int z = cz + dz;
                int topY = world.getTopSolidOrLiquidBlock(x, z);
                if (topY < cy - 24 || topY > cy + 28) continue;

                double edgeFactor = 1.0D - (dist / reliefRadius);
                double macro = Math.sin(x * 0.07D + phaseX) + Math.cos(z * 0.06D + phaseZ);
                double micro = Math.sin((x + z) * 0.19D + phaseX * 0.5D);
                int delta = (int)Math.round(edgeFactor * (macro * 1.8D + micro * 0.9D));

                // Кладбище почти ровное, но не абсолютно плоское.
                if (biome == EndBiomes.biomeCemetery) {
                    delta = Math.max(-1, Math.min(1, delta));
                } else {
                    delta = Math.max(-4, Math.min(4, delta));
                }

                if (delta > 0) {
                    for (int i = 0; i < delta; i++) {
                        world.setBlock(x, topY + i, z, i == delta - 1 ? topBlock : fillerBlock, 0, 2);
                    }
                } else if (delta < 0) {
                    for (int i = 0; i < -delta; i++) {
                        int yy = topY - i;
                        if (!world.isAirBlock(x, yy, z)) {
                            world.setBlock(x, yy, z, Blocks.air, 0, 2);
                        }
                    }
                }
            }
        }

        erodeIslandEdges(world, rand, cx, cy, cz, radius, topBlock, fillerBlock);
    }

    // ===== УТИЛИТЫ =====

    /**
     * Superellipse distance — скруглённый прямоугольник без острых углов.
     * Степень 4: углы скруглены, но форма не становится кругом.
     */
    private float softRect(float x, float z, float hw, float hh) {
        if (hw < 1 || hh < 1) return 2.0F;
        double nx = x / hw, nz = z / hh;
        return (float)Math.pow(Math.pow(Math.abs(nx), 4) + Math.pow(Math.abs(nz), 4), 0.25);
    }

    /** Предвычисленный шум для рваных краёв — экономим вызовы Random. */
    private float[] buildNoise(Random rand, int size) {
        float[] n = new float[size];
        for (int i = 0; i < size; i++) n[i] = rand.nextFloat();
        return n;
    }

    private void placeBlock(World world, int x, int y, int z, Block block, boolean isTop, BiomeGenBase biome) {
        world.setBlock(x, y, z, block, 0, 0);
    }

    private void setBiomeAt(World world, int x, int z, BiomeGenBase biome) {
        try {
            net.minecraft.world.chunk.Chunk chunk = world.getChunkFromBlockCoords(x, z);
            chunk.getBiomeArray()[(z & 15) << 4 | (x & 15)] = (byte) biome.biomeID;
        } catch (Exception ignored) {}
    }

    private void setBiomeArea(World world, int cx, int cz, int radius, BiomeGenBase biome) {
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= r2) {
                    setBiomeAt(world, cx + dx, cz + dz, biome);
                }
            }
        }
    }

    // ===== ОКЕАН =====

    private void fillOceanBasin(World world, Random rand, int cx, int cy, int cz, int radius) {
        int r = Math.max(7, radius + 4);
        int wl = cy - 1;
        int depth = 6 + rand.nextInt(5);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double dist = Math.sqrt(dx * dx + dz * dz);
                double norm = dist / r;
                if (norm > 1.0D) continue;

                int localDepth = depth - (int)(norm * 3.0D);
                localDepth += ((dx * 13 + dz * 7) & 3) - 1;
                if (localDepth < 3) localDepth = 3;

                for (int dy = 0; dy < localDepth; dy++) {
                    world.setBlock(cx + dx, wl - dy, cz + dz, Blocks.water, 0, 2);
                }

                int floorY = wl - localDepth;
                Block floor = ((dx * dx + dz * dz) & 1) == 0 ? Blocks.end_stone : EndExpansion.oceanStone;
                if (((dx * 31 + dz * 17) & 15) == 0 && EndExpansion.coralStoneEnd != null) floor = EndExpansion.coralStoneEnd;
                world.setBlock(cx + dx, floorY, cz + dz, floor, 0, 2);
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

        populateIslandDetails(world, rand, cx, cy, cz, biome, radius);
    }

    private void erodeIslandEdges(World world, Random rand, int cx, int cy, int cz, int radius, Block top, Block filler) {
        int outer = Math.max(8, radius);
        int inner = (int)(outer * 0.74F);

        for (int dx = -outer; dx <= outer; dx++) {
            for (int dz = -outer; dz <= outer; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 < inner * inner || d2 > outer * outer) continue;

                int x = cx + dx;
                int z = cz + dz;
                int topY = world.getTopSolidOrLiquidBlock(x, z);
                if (topY < cy - 24 || topY > cy + 28) continue;

                int hash = Math.abs(dx * 37 + dz * 57);
                int carve = hash % 3;
                if (rand.nextInt(4) == 0) carve++;

                for (int i = 0; i < carve; i++) {
                    int y = topY - i;
                    if (!world.isAirBlock(x, y, z)) {
                        world.setBlock(x, y, z, Blocks.air, 0, 2);
                    }
                }

                if ((hash & 7) == 0) {
                    int lipY = Math.max(cy - 20, topY - carve);
                    world.setBlock(x, lipY, z, top, 0, 2);
                    if (rand.nextBoolean()) world.setBlock(x, lipY - 1, z, filler, 0, 2);
                }
            }
        }
    }

    private void populateIslandDetails(World world, Random rand, int cx, int cy, int cz,
                                       BiomeGenBase biome, int radius) {
        int featureCount = Math.max(18, radius / 2) + rand.nextInt(Math.max(8, radius / 2));
        Block accent = (biome == EndBiomes.biomeOcean && EndExpansion.coralStoneEnd != null)
                ? EndExpansion.coralStoneEnd
                : (EndExpansion.mossyAshenStone != null ? EndExpansion.mossyAshenStone : Blocks.end_stone);

        for (int i = 0; i < featureCount; i++) {
            float a = rand.nextFloat() * (float)(Math.PI * 2);
            float d = 6 + rand.nextFloat() * (radius * 0.92F);
            int x = cx + (int)(Math.cos(a) * d);
            int z = cz + (int)(Math.sin(a) * d);
            int y = world.getTopSolidOrLiquidBlock(x, z);
            if (y < cy - 28 || y > cy + 30) continue;

            if (rand.nextInt(5) == 0) {
                int h = 1 + rand.nextInt(3);
                for (int dy = 0; dy < h; dy++) {
                    if (world.isAirBlock(x, y + dy, z)) {
                        world.setBlock(x, y + dy, z, accent, 0, 2);
                    }
                }
            } else {
                int ring = 1 + rand.nextInt(2);
                for (int dx = -ring; dx <= ring; dx++) {
                    for (int dz = -ring; dz <= ring; dz++) {
                        if (dx * dx + dz * dz > ring * ring) continue;
                        if (rand.nextInt(3) == 0) continue;
                        int py = world.getTopSolidOrLiquidBlock(x + dx, z + dz) - 1;
                        if (py >= cy - 30) world.setBlock(x + dx, py, z + dz, accent, 0, 2);
                    }
                }
            }
        }
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

    // ===== ДЕРЕВЬЯ (публичные — для декораторов) =====

    public void generateGnarledTree(World world, Random rand, int x, int y, int z) {
        int height = 10 + rand.nextInt(8);
        int sx = x, sz = z;
        for (int i = 0; i < height; i++) {
            world.setBlock(sx, y+i, sz, EndExpansion.ancientLog, 0, 2);
            if (i > 3 && rand.nextInt(4) == 0) { sx += rand.nextInt(3)-1; sz += rand.nextInt(3)-1; }
        }
        for (int bx=-1; bx<=1; bx++)
            for (int bz=-1; bz<=1; bz++)
                if (rand.nextInt(3)!=0) world.setBlock(x+bx, y, z+bz, EndExpansion.ancientLog, 0, 2);
        int bc = 3 + rand.nextInt(3);
        for (int b = 0; b < bc; b++) {
            int by = y + height/2 + rand.nextInt(height/2);
            int bdx = rand.nextInt(3)-1, bdz = rand.nextInt(3)-1, blen = 3+rand.nextInt(4);
            int bx = sx, bz = sz;
            for (int bl=0; bl<blen; bl++) {
                bx += bdx; bz += bdz;
                world.setBlock(bx, by+(bl>blen/2?1:0), bz, EndExpansion.ancientLog, 0, 2);
            }
            for (int lx=-2; lx<=2; lx++) for (int lz=-2; lz<=2; lz++) for (int ly=-1; ly<=2; ly++)
                if (lx*lx+lz*lz+ly*ly<=6 && world.isAirBlock(bx+lx, by+ly, bz+lz))
                    world.setBlock(bx+lx, by+ly, bz+lz, EndExpansion.ancientLeaves, 0, 2);
        }
        for (int lx=-3; lx<=3; lx++) for (int lz=-3; lz<=3; lz++) for (int ly=-1; ly<=3; ly++) {
            float d = lx*lx+lz*lz+ly*ly*0.5F;
            if (d<=10 && rand.nextInt(4)!=0 && world.isAirBlock(sx+lx, y+height+ly, sz+lz))
                world.setBlock(sx+lx, y+height+ly, sz+lz, EndExpansion.ancientLeaves, 0, 2);
        }
    }

    public void generateJungleTree(World world, Random rand, int x, int y, int z) {
        int height = 12 + rand.nextInt(7);
        for (int i = 0; i < height; i++) {
            world.setBlock(x, y+i, z, EndExpansion.tropicalLog, 0, 2);
            if (height > 14) {
                world.setBlock(x+1, y+i, z, EndExpansion.tropicalLog, 0, 2);
                world.setBlock(x, y+i, z+1, EndExpansion.tropicalLog, 0, 2);
                world.setBlock(x+1, y+i, z+1, EndExpansion.tropicalLog, 0, 2);
            }
        }
        int cr = 4 + rand.nextInt(3);
        for (int lx=-cr; lx<=cr; lx++) for (int lz=-cr; lz<=cr; lz++) for (int ly=-1; ly<=2; ly++) {
            float d = lx*lx+lz*lz;
            float mR = (cr-Math.abs(ly))*(cr-Math.abs(ly));
            if (d<=mR && rand.nextInt(5)!=0 && world.isAirBlock(x+lx, y+height+ly, z+lz))
                world.setBlock(x+lx, y+height+ly, z+lz, EndExpansion.tropicalLeaves, 0, 2);
        }
    }

    public void generateCustomTree(World world, Random rand, int x, int y, int z, Block log, Block leaves) {
        int height = 5 + rand.nextInt(4);
        for (int i = 0; i < height; i++) world.setBlock(x, y+i, z, log, 0, 2);
        for (int lx=-2; lx<=2; lx++) for (int lz=-2; lz<=2; lz++) for (int ly=height-2; ly<=height+1; ly++)
            if (Math.abs(lx)+Math.abs(lz)<=3 && world.isAirBlock(x+lx, y+ly, z+lz))
                world.setBlock(x+lx, y+ly, z+lz, leaves, 0, 2);
    }

    // ===== DATA CLASS =====

    public static class IslandNode {
        public final int x, y, z, radius;
        public IslandNode(int x, int y, int z, int radius) {
            this.x = x; this.y = y; this.z = z; this.radius = radius;
        }
    }
}
