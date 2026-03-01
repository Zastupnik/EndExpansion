package com.zastupnik.endexpansion.blocks;

import com.zastupnik.endexpansion.EndExpansion;
import com.zastupnik.endexpansion.world.gen.EndIslandGenerator;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.Random;

public class BlockEndSapling extends BlockBush implements IGrowable {

    private final String treeType;

    public BlockEndSapling(String name, String treeType) {
        super();
        this.treeType = treeType;
        this.setBlockName(name);
        this.setBlockTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);
        this.setStepSound(soundTypeGrass);
        float f = 0.4F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f * 2.0F, 0.5F + f);
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (!world.isRemote) {
            super.updateTick(world, x, y, z, rand);
            if (world.getBlockLightValue(x, y + 1, z) >= 9 && rand.nextInt(7) == 0) {
                this.growTree(world, x, y, z, rand);
            }
        }
    }

    public void growTree(World world, int x, int y, int z, Random rand) {
        world.setBlock(x, y, z, Blocks.air, 0, 4);

        EndIslandGenerator gen = new EndIslandGenerator();

        if (treeType.equals("ancient")) {
            gen.generateGnarledTree(world, rand, x, y, z);
        } else if (treeType.equals("tropical")) {
            gen.generateJungleTree(world, rand, x, y, z);
        } else if (treeType.equals("withered")) {
            gen.generateCustomTree(world, rand, x, y, z,
                    EndExpansion.witheredLog, EndExpansion.witheredLeaves);
        } else if (treeType.equals("infected")) {
            gen.generateCustomTree(world, rand, x, y, z,
                    EndExpansion.infectedStalk, EndExpansion.infectedLeaves);
        }
    }

    @Override
    protected boolean canPlaceBlockOn(net.minecraft.block.Block ground) {
        return ground == Blocks.end_stone      ||
                ground == EndExpansion.deadGrass     ||
                ground == EndExpansion.forestMoss    ||
                ground == EndExpansion.jungleTurf;
    }

    // --- IGrowable (костная мука) ---

    @Override
    public boolean func_149851_a(World world, int x, int y, int z, boolean isRemote) {
        return true;
    }

    @Override
    public boolean func_149852_a(World world, Random rand, int x, int y, int z) {
        return (double) world.rand.nextFloat() < 0.45D;
    }

    @Override
    public void func_149853_b(World world, Random rand, int x, int y, int z) {
        this.growTree(world, x, y, z, rand);
    }
}