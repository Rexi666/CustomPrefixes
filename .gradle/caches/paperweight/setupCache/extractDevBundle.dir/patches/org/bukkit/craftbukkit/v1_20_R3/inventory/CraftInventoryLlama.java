package org.bukkit.craftbukkit.v1_20_R3.inventory;

import net.minecraft.world.Container;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.LlamaInventory;

public class CraftInventoryLlama extends CraftInventoryAbstractHorse implements LlamaInventory {

    public CraftInventoryLlama(Container inventory) {
        super(inventory);
    }

    @Override
    public ItemStack getDecor() {
        return this.getItem(1);
    }

    @Override
    public void setDecor(ItemStack stack) {
        this.setItem(1, stack);
    }
}
