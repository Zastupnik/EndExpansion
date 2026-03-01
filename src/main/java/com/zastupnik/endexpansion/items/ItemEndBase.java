package com.zastupnik.endexpansion.items;

import com.zastupnik.endexpansion.EndExpansion;
import net.minecraft.item.Item;

/**
 * Базовый предмет-материал (не еда, не инструмент).
 * Используется для: осколков, порошков, рун, ключей, компасов и т.д.
 */
public class ItemEndBase extends Item {

    public ItemEndBase() {
        super();
        setMaxStackSize(64);
    }
}