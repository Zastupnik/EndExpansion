package com.zastupnik.endexpansion;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class EndTab extends CreativeTabs {

    public EndTab(String label) {
        super(label);
    }

    /**
     * Этот метод определяет, какой предмет будет отображаться на иконке вкладки.
     * Мы используем deadGrass (Мертвый дерн), так как это базовый блок Кладбища.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        // Мы берем иконку из нашего главного класса
        return Item.getItemFromBlock(EndExpansion.deadGrass);
    }

    /**
     * Если ты хочешь, чтобы у вкладки было локализованное имя (например, в ru_RU.lang),
     * этот метод поможет правильно его отобразить.
     */
    @Override
    public String getTranslatedTabLabel() {
        return "End Expansion";
    }
}