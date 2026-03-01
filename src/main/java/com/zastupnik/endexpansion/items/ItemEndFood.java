package com.zastupnik.endexpansion.items;

import net.minecraft.item.ItemFood;

/**
 * Базовая еда Энда.
 * healAmount  — сколько восстанавливает (в половинках сердец голода, 1 = полкуска)
 * saturation  — модификатор насыщения (0.0 = нет, 1.0 = высокое, 2.4 = как золотое яблоко)
 */
public class ItemEndFood extends ItemFood {

    public ItemEndFood(int healAmount, float saturation) {
        super(healAmount, saturation, false); // false = не волчья еда
        setMaxStackSize(64);
    }

    /** Для еды которую едят из миски (стакфится только на 1, как суп). */
    public ItemEndFood asBowlFood() {
        setMaxStackSize(1);
        return this;
    }
}