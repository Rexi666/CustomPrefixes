package org.bukkit.craftbukkit.v1_20_R3.entity;

import net.minecraft.world.entity.FlyingMob;
import org.bukkit.craftbukkit.v1_20_R3.CraftServer;
import org.bukkit.entity.Flying;

public class CraftFlying extends CraftMob implements Flying {

    public CraftFlying(CraftServer server, FlyingMob entity) {
        super(server, entity);
    }

    @Override
    public FlyingMob getHandle() {
        return (FlyingMob) this.entity;
    }

    @Override
    public String toString() {
        return "CraftFlying";
    }
}
