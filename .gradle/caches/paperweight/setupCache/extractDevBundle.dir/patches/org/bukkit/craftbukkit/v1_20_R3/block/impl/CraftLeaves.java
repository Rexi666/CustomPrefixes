/**
 * Automatically generated file, changes will be lost.
 */
package org.bukkit.craftbukkit.v1_20_R3.block.impl;

public final class CraftLeaves extends org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData implements org.bukkit.block.data.type.Leaves, org.bukkit.block.data.Waterlogged {

    public CraftLeaves() {
        super();
    }

    public CraftLeaves(net.minecraft.world.level.block.state.BlockState state) {
        super(state);
    }

    // org.bukkit.craftbukkit.v1_20_R3.block.data.type.CraftLeaves

    private static final net.minecraft.world.level.block.state.properties.IntegerProperty DISTANCE = getInteger(net.minecraft.world.level.block.LeavesBlock.class, "distance");
    private static final net.minecraft.world.level.block.state.properties.BooleanProperty PERSISTENT = getBoolean(net.minecraft.world.level.block.LeavesBlock.class, "persistent");

    @Override
    public boolean isPersistent() {
        return this.get(CraftLeaves.PERSISTENT);
    }

    @Override
    public void setPersistent(boolean persistent) {
        this.set(CraftLeaves.PERSISTENT, persistent);
    }

    @Override
    public int getDistance() {
        return this.get(CraftLeaves.DISTANCE);
    }

    @Override
    public void setDistance(int distance) {
        this.set(CraftLeaves.DISTANCE, distance);
    }

    // org.bukkit.craftbukkit.v1_20_R3.block.data.CraftWaterlogged

    private static final net.minecraft.world.level.block.state.properties.BooleanProperty WATERLOGGED = getBoolean(net.minecraft.world.level.block.LeavesBlock.class, "waterlogged");

    @Override
    public boolean isWaterlogged() {
        return this.get(CraftLeaves.WATERLOGGED);
    }

    @Override
    public void setWaterlogged(boolean waterlogged) {
        this.set(CraftLeaves.WATERLOGGED, waterlogged);
    }

    // Paper start
    @Override
    public int getMaximumDistance() {
        return getMax(CraftLeaves.DISTANCE);
    }

    @Override
    public int getMinimumDistance() {
        return getMin(CraftLeaves.DISTANCE);
    }
    // Paper end
}
