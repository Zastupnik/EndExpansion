package com.zastupnik.endexpansion.items;

import com.zastupnik.endexpansion.EndExpansion;
import com.zastupnik.endexpansion.world.EndTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class ItemEndTeleporter extends Item {

    public ItemEndTeleporter() {
        this.setCreativeTab(EndExpansion.tabEndExpansion);
        this.setTextureName("endexpansion:end_teleporter");
        this.setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            MinecraftServer server = MinecraftServer.getServer();

            if (playerMP.dimension != 1) {
                // ТЕЛЕПОРТАЦИЯ В ЭНД
                WorldServer endWorld = server.worldServerForDimension(1);
                // Задаем точку появления (например, высота 80 над центром)
                // Позже заменим на координаты конкретного биома
                server.getConfigurationManager().transferPlayerToDimension(playerMP, 1,
                        new EndTeleporter(endWorld, 0.5D, 80.0D, 0.5D));
            } else {
                // ВОЗВРАТ В ОБЫЧНЫЙ МИР
                WorldServer overWorld = server.worldServerForDimension(0);
                server.getConfigurationManager().transferPlayerToDimension(playerMP, 0,
                        new EndTeleporter(overWorld, playerMP.posX, 100.0D, playerMP.posZ));
            }
        }

        player.swingItem();
        return stack;
    }
}