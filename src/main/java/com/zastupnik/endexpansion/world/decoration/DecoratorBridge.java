package com.zastupnik.endexpansion.world.decoration;

import com.zastupnik.endexpansion.EndExpansion;
import com.zastupnik.endexpansion.world.gen.EndIslandGenerator.IslandNode;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Строит мосты между островами в кластере.
 * Материал: эндерняк (настил) + witheredFence (перила).
 * Биом моста — дефолтный биом Энда (не меняем).
 */
public class DecoratorBridge {

    // Максимальная длина моста. Дальше — не строим (слишком далеко).
    private static final int MAX_BRIDGE_LENGTH = 300;

    /**
     * Строит мосты внутри кластера:
     * - Главный узел → все остальные (гарантирует связность)
     * - Случайные дополнительные связи (петли, альтернативные пути)
     */
    public void buildClusterBridges(World world, Random rand, List<IslandNode> nodes) {
        if (nodes == null || nodes.size() < 2) return;

        IslandNode root = nodes.get(0);

        // Обязательные связи: root → каждый
        for (int i = 1; i < nodes.size(); i++) {
            buildBridge(world, rand, root, nodes.get(i));
        }

        // Дополнительные случайные петли (0..size-1 штук)
        int extra = rand.nextInt(nodes.size());
        for (int i = 0; i < extra; i++) {
            IslandNode a = nodes.get(rand.nextInt(nodes.size()));
            IslandNode b = nodes.get(rand.nextInt(nodes.size()));
            if (a != b) buildBridge(world, rand, a, b);
        }
    }

    /**
     * Один мост от узла A до узла B.
     *
     * Конструкция (вид сбоку):
     *   F   F       F   F     ← заборы (перила), на 1 выше настила
     *   E E E  ...  E E E     ← эндерняк, 3 блока ширина
     *
     * Y-профиль: провисает посередине (катенарный эффект).
     * Длина ограничена MAX_BRIDGE_LENGTH.
     */
    private void buildBridge(World world, Random rand, IslandNode from, IslandNode to) {
        int x1 = from.x, y1 = from.y, z1 = from.z;
        int x2 = to.x,   y2 = to.y,   z2 = to.z;

        int dx  = x2 - x1;
        int dz  = z2 - z1;
        int len = (int)Math.sqrt(dx * dx + dz * dz);

        if (len < 8 || len > MAX_BRIDGE_LENGTH) return;

        float stepX = (float)dx / len;
        float stepZ = (float)dz / len;

        // Перпендикуляр для ширины
        float perpX = -stepZ;
        float perpZ =  stepX;

        // Случайный стиль моста для разнообразия
        BridgeStyle style = pickStyle(rand);

        Block fence = (EndExpansion.witheredFence != null) ? EndExpansion.witheredFence : Blocks.fence;

        for (int i = 0; i <= len; i++) {
            float t = (float)i / len;

            // Провис: sin(t*PI) даёт 0 на концах, 1 в середине
            float sag = (float)Math.sin(t * Math.PI) * (len * 0.05F);
            int by = Math.round(y1 + (y2 - y1) * t - sag);

            int bx = x1 + Math.round(stepX * i);
            int bz = z1 + Math.round(stepZ * i);

            switch (style) {
                case WIDE:    buildWideSection (world, rand, bx, by, bz, perpX, perpZ, fence, i, len); break;
                case NARROW:  buildNarrowSection(world, rand, bx, by, bz, perpX, perpZ, fence, i, len); break;
                case RUINED:  buildRuinedSection(world, rand, bx, by, bz, perpX, perpZ, fence, i, len); break;
                case ARCHED:  buildArchedSection(world, rand, bx, by, bz, perpX, perpZ, fence, i, len, sag); break;
            }

            if (i % 6 == 0 || i == 0 || i == len) {
                buildSupport(world, bx, by - 1, bz);
            }
        }
    }

    // ===== СТИЛИ МОСТОВ =====

    private enum BridgeStyle { WIDE, NARROW, RUINED, ARCHED }

    private BridgeStyle pickStyle(Random rand) {
        int r = rand.nextInt(4);
        if (r == 0) return BridgeStyle.WIDE;
        if (r == 1) return BridgeStyle.NARROW;
        if (r == 2) return BridgeStyle.RUINED;
        return BridgeStyle.ARCHED;
    }

    /**
     * Широкий мост: 5 блоков настила + перила.
     *    F   .   .   .   F
     *    E   E   E   E   E
     */
    private void buildWideSection(World world, Random rand, int bx, int by, int bz,
                                  float perpX, float perpZ, Block fence, int step, int len) {
        for (int w = -2; w <= 2; w++) {
            int px = bx + Math.round(perpX * w);
            int pz = bz + Math.round(perpZ * w);
            placeBridgeBlock(world, px, by, pz, rand);

            // Перила только по краям
            if (Math.abs(w) == 2) {
                world.setBlock(px, by + 1, pz, fence, 0, 2);
                // Иногда ставим фонарь на перила
                if (step % 8 == 0 && EndExpansion.endTorch != null) {
                    world.setBlock(px, by + 2, pz, EndExpansion.endTorch, 0, 2);
                }
            }
        }
    }

    /**
     * Узкий мост: 3 блока + перила.
     *    F   .   F
     *    E   E   E
     */
    private void buildNarrowSection(World world, Random rand, int bx, int by, int bz,
                                    float perpX, float perpZ, Block fence, int step, int len) {
        for (int w = -1; w <= 1; w++) {
            int px = bx + Math.round(perpX * w);
            int pz = bz + Math.round(perpZ * w);
            placeBridgeBlock(world, px, by, pz, rand);
            if (Math.abs(w) == 1) {
                world.setBlock(px, by + 1, pz, fence, 0, 2);
            }
        }
    }

    /**
     * Разрушенный мост: дыры, поломанные перила, разный уровень блоков.
     */
    private void buildRuinedSection(World world, Random rand, int bx, int by, int bz,
                                    float perpX, float perpZ, Block fence, int step, int len) {
        // 20% шанс полностью пропустить секцию (дыра)
        if (rand.nextInt(5) == 0) return;

        for (int w = -1; w <= 1; w++) {
            int px = bx + Math.round(perpX * w);
            int pz = bz + Math.round(perpZ * w);

            // 15% шанс пропустить отдельный блок
            if (rand.nextInt(7) != 0) {
                // Иногда блок чуть сдвинут по Y — обвалившиеся секции
                int yShift = rand.nextInt(3) == 0 ? -1 : 0;
                placeBridgeBlock(world, px, by + yShift, pz, rand);
            }

            if (Math.abs(w) == 1 && rand.nextInt(3) != 0) {
                world.setBlock(px, by + 1, pz, fence, 0, 2);
            }
        }

        // Редко — обломок сбоку висит в воздухе
        if (rand.nextInt(12) == 0) {
            int side = rand.nextBoolean() ? 2 : -2;
            int px   = bx + Math.round(perpX * side);
            int pz   = bz + Math.round(perpZ * side);
            placeBridgeBlock(world, px, by, pz, rand);
        }
    }

    /**
     * Арочный мост: под настилом строится арка из эндерняка.
     * Арка появляется только в середине моста.
     */
    private void buildArchedSection(World world, Random rand, int bx, int by, int bz,
                                    float perpX, float perpZ, Block fence,
                                    int step, int len, float sag) {
        // Настил (3 блока)
        for (int w = -1; w <= 1; w++) {
            int px = bx + Math.round(perpX * w);
            int pz = bz + Math.round(perpZ * w);
            placeBridgeBlock(world, px, by, pz, rand);
            if (Math.abs(w) == 1) {
                world.setBlock(px, by + 1, pz, fence, 0, 2);
            }
        }

        // Арка под мостом — только в центральной трети
        float t = (float)step / len;
        if (t > 0.2F && t < 0.8F) {
            int archDepth = (int)(sag * 0.8F) + 1;
            for (int ad = 1; ad <= archDepth; ad++) {
                // Два столба арки по бокам
                int px1 = bx + Math.round(perpX * 2);
                int pz1 = bz + Math.round(perpZ * 2);
                int px2 = bx - Math.round(perpX * 2);
                int pz2 = bz - Math.round(perpZ * 2);
                placeBridgeBlock(world, px1, by - ad, pz1, rand);
                placeBridgeBlock(world, px2, by - ad, pz2, rand);
            }
        }
    }

    private void buildSupport(World world, int x, int y, int z) {
        int minY = Math.max(30, y - 24);
        for (int sy = y; sy >= minY; sy--) {
            Block at = world.getBlock(x, sy, z);
            if (at != Blocks.air && at != Blocks.water) break;
            world.setBlock(x, sy, z, EndExpansion.fortressPillar != null ? EndExpansion.fortressPillar : Blocks.cobblestone, 0, 2);
        }
    }

    private void placeBridgeBlock(World world, int x, int y, int z, Random rand) {
        Block block = Blocks.end_stone;
        int r = rand.nextInt(8);
        if (r == 0) block = Blocks.stonebrick;
        else if (r == 1) block = Blocks.cobblestone;
        else if (r == 2 && EndExpansion.fortressBrick != null) block = EndExpansion.fortressBrick;
        world.setBlock(x, y, z, block, 0, 2);
    }

}