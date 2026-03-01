package com.zastupnik.endexpansion.handler;

import com.zastupnik.endexpansion.handler.ConfigHandler;
import com.zastupnik.endexpansion.world.biome.EndBiomeBase;
import com.zastupnik.endexpansion.world.biome.EndBiomes;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraft.entity.boss.EntityDragon;

public class EndEventHandler {

    // ===== ЭФФЕКТЫ БИОМОВ =====

    @SubscribeEvent
    public void onEntityUpdate(LivingUpdateEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entityLiving;
        World world = player.worldObj;

        if (world.isRemote || world.provider.dimensionId != 1) return;

        int x = (int) player.posX;
        int z = (int) player.posZ;
        BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
        long time = world.getTotalWorldTime();

        // Кладбище: слабость каждые 4 секунды
        if (biome == EndBiomes.biomeCemetery) {
            if (time % 80 == 0) {
                player.addPotionEffect(new PotionEffect(Potion.weakness.id, 120, 0));
            }
        }

        // Заражение: голод + тошнота
        if (biome == EndBiomes.biomeInfection) {
            if (time % 60 == 0) {
                player.addPotionEffect(new PotionEffect(Potion.hunger.id, 120, 1));
            }
            if (time % 200 == 0) {
                player.addPotionEffect(new PotionEffect(Potion.confusion.id, 100, 0));
            }
        }

        // Океан: скорость копания под водой
        if (biome == EndBiomes.biomeOcean && player.isInWater()) {
            player.addPotionEffect(new PotionEffect(Potion.digSpeed.id, 40, 0));
        }

        // Крепость: замедление
        if (biome == EndBiomes.biomeFortress) {
            if (time % 100 == 0) {
                player.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 60, 0));
            }
        }

        // Джунгли: регенерация
        if (biome == EndBiomes.biomeJungle) {
            if (time % 120 == 0) {
                player.addPotionEffect(new PotionEffect(Potion.regeneration.id, 40, 0));
            }
        }
    }

    // ===== ТУМАН (КЛИЕНТ) =====

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (!ConfigHandler.enableCustomSky) return;

        World world = Minecraft.getMinecraft().theWorld;
        if (world == null || world.provider.dimensionId != 1) return;

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) return;

        BiomeGenBase biome = world.getBiomeGenForCoords(
                (int) player.posX, (int) player.posZ);

        if (biome instanceof EndBiomeBase) {
            int fog = ((EndBiomeBase) biome).getFogColor();
            event.red   = ((fog >> 16) & 0xFF) / 255.0F;
            event.green = ((fog >> 8)  & 0xFF) / 255.0F;
            event.blue  = ( fog        & 0xFF) / 255.0F;
        }
    }

    // ===== DRACONIC EVOLUTION — ОТКЛЮЧЕНИЕ РИТУАЛА =====

    @SubscribeEvent
    public void onLivingSpawn(LivingSpawnEvent.CheckSpawn event) {
        if (!ConfigHandler.disableDraconicRitual) return;
        if (event.world.provider.dimensionId != 1) return;

        String className = event.entityLiving.getClass().getName();

        if (className.contains("com.brandon3055.draconicevolution") &&
                className.contains("Dragon")) {
            event.setResult(cpw.mods.fml.common.eventhandler.Event.Result.DENY);
        }
    }

}