package org.bukkit.craftbukkit.v1_20_R3.block.data;

import org.bukkit.block.data.Openable;

public abstract class CraftOpenable extends CraftBlockData implements Openable {

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty OPEN = getBoolean("open");

    @Override
    public boolean isOpen() {
        return this.get(CraftOpenable.OPEN);
    }

    @Override
    public void setOpen(boolean open) {
        this.set(CraftOpenable.OPEN, open);
    }
}
