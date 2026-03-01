package com.zastupnik.endexpansion.items;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;

public class ItemEndFood extends ItemFood {

    private int potionId;
    private int duration;

    public ItemEndFood(String name, int amount, float saturation, boolean isWolfFood) {
        super(amount, saturation, isWolfFood);
        this.setUnlocalizedName(name);
        this.setTextureName("endexpansion:" + name);
        this.setCreativeTab(EndExpansion.tabEndExpansion);
    }

    // Метод для добавления эффектов при поедании
    public ItemEndFood setEffect(int id, int seconds) {
        this.potionId = id;
        this.duration = seconds * 20; // В тиках
        return this;
    }

    @Override
    protected void onFoodEaten(ItemStack stack, World world, EntityPlayer player) {
        super.onFoodEaten(stack, world, player);
        if (!world.isRemote && potionId > 0) {
            player.addPotionEffect(new PotionEffect(potionId, duration, 0));
        }
    }
}