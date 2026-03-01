package com.zastupnik.endexpansion.world;

import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

public class EndTeleporter extends Teleporter {

    private final WorldServer worldServerInstance;
    private double x, y, z;

    public EndTeleporter(WorldServer world, double x, double y, double z) {
        super(world);
        this.worldServerInstance = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void placeInPortal(Entity entity, double p2, double p3, double p4, float p5) {
        // Мы не создаем ванильный портал из обсидиана
        // Просто перемещаем сущность в заданные координаты
        entity.setLocationAndAngles(this.x, this.y, this.z, entity.rotationYaw, 0.0F);
        entity.motionX = entity.motionY = entity.motionZ = 0.0D;
    }

    // В 1.7.10 эти методы обязательны, но мы оставляем их пустыми,
    // чтобы не спавнить рамку портала под игроком.
    @Override
    public boolean placeInExistingPortal(Entity entity, double p2, double p3, double p4, float p5) {
        return true;
    }

    @Override
    public boolean makePortal(Entity entity) {
        return true;
    }
}