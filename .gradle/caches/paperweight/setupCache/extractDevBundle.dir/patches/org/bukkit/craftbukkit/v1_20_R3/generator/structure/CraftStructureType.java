package org.bukkit.craftbukkit.v1_20_R3.generator.structure;

import net.minecraft.core.registries.Registries;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R3.util.Handleable;
import org.bukkit.generator.structure.StructureType;

public class CraftStructureType extends StructureType implements Handleable<net.minecraft.world.level.levelgen.structure.StructureType<?>> {

    public static StructureType minecraftToBukkit(net.minecraft.world.level.levelgen.structure.StructureType<?> minecraft) {
        return CraftRegistry.minecraftToBukkit(minecraft, Registries.STRUCTURE_TYPE, Registry.STRUCTURE_TYPE);
    }

    public static net.minecraft.world.level.levelgen.structure.StructureType<?> bukkitToMinecraft(StructureType bukkit) {
        return CraftRegistry.bukkitToMinecraft(bukkit);
    }

    private final NamespacedKey key;
    private final net.minecraft.world.level.levelgen.structure.StructureType<?> structureType;

    public CraftStructureType(NamespacedKey key, net.minecraft.world.level.levelgen.structure.StructureType<?> structureType) {
        this.key = key;
        this.structureType = structureType;
    }

    @Override
    public net.minecraft.world.level.levelgen.structure.StructureType<?> getHandle() {
        return this.structureType;
    }

    @Override
    public NamespacedKey getKey() {
        return this.key;
    }
}
