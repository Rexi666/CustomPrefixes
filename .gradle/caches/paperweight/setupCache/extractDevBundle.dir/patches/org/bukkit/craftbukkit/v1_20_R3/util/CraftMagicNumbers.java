package org.bukkit.craftbukkit.v1_20_R3.util;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import org.bukkit.Bukkit;
import org.bukkit.FeatureFlag;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.UnsafeValues;
import org.bukkit.advancement.Advancement;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R3.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_20_R3.CraftFeatureFlag;
import org.bukkit.craftbukkit.v1_20_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R3.attribute.CraftAttribute;
import org.bukkit.craftbukkit.v1_20_R3.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v1_20_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R3.damage.CraftDamageEffect;
import org.bukkit.craftbukkit.v1_20_R3.damage.CraftDamageSourceBuilder;
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_20_R3.legacy.CraftLegacy;
import org.bukkit.craftbukkit.v1_20_R3.potion.CraftPotionType;
import org.bukkit.damage.DamageEffect;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.potion.PotionType;

@SuppressWarnings("deprecation")
public final class CraftMagicNumbers implements UnsafeValues {
    public static final UnsafeValues INSTANCE = new CraftMagicNumbers();

    private CraftMagicNumbers() {}

    // Paper start
    @Override
    public net.kyori.adventure.text.flattener.ComponentFlattener componentFlattener() {
        return io.papermc.paper.adventure.PaperAdventure.FLATTENER;
    }

    @Override
    public net.kyori.adventure.text.serializer.gson.GsonComponentSerializer colorDownsamplingGsonComponentSerializer() {
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.colorDownsamplingGson();
    }

    @Override
    public net.kyori.adventure.text.serializer.gson.GsonComponentSerializer gsonComponentSerializer() {
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson();
    }

    @Override
    public net.kyori.adventure.text.serializer.plain.PlainComponentSerializer plainComponentSerializer() {
        return io.papermc.paper.adventure.PaperAdventure.PLAIN;
    }

    @Override
    public net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer plainTextSerializer() {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText();
    }

    @Override
    public net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacyComponentSerializer() {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();
    }

    @Override
    public net.kyori.adventure.text.Component resolveWithContext(final net.kyori.adventure.text.Component component, final org.bukkit.command.CommandSender context, final org.bukkit.entity.Entity scoreboardSubject, final boolean bypassPermissions) throws IOException {
        return io.papermc.paper.adventure.PaperAdventure.resolveWithContext(component, context, scoreboardSubject, bypassPermissions);
    }
    // Paper end

    public static BlockState getBlock(MaterialData material) {
        return CraftMagicNumbers.getBlock(material.getItemType(), material.getData());
    }

    public static BlockState getBlock(Material material, byte data) {
        return CraftLegacy.fromLegacyData(CraftLegacy.toLegacy(material), data);
    }

    public static MaterialData getMaterial(BlockState data) {
        return CraftLegacy.toLegacy(CraftMagicNumbers.getMaterial(data.getBlock())).getNewData(CraftMagicNumbers.toLegacyData(data));
    }

    public static Item getItem(Material material, short data) {
        if (material.isLegacy()) {
            return CraftLegacy.fromLegacyData(CraftLegacy.toLegacy(material), data);
        }

        return CraftMagicNumbers.getItem(material);
    }

    public static MaterialData getMaterialData(Item item) {
        return CraftLegacy.toLegacyData(CraftMagicNumbers.getMaterial(item));
    }

    // ========================================================================
    private static final Map<Block, Material> BLOCK_MATERIAL = new HashMap<>();
    private static final Map<Item, Material> ITEM_MATERIAL = new HashMap<>();
    private static final Map<Material, Item> MATERIAL_ITEM = new HashMap<>();
    private static final Map<Material, Block> MATERIAL_BLOCK = new HashMap<>();
    // Paper start
    private static final Map<org.bukkit.entity.EntityType, net.minecraft.world.entity.EntityType<?>> ENTITY_TYPE_ENTITY_TYPES = new HashMap<>();
    private static final Map<net.minecraft.world.entity.EntityType<?>, org.bukkit.entity.EntityType> ENTITY_TYPES_ENTITY_TYPE = new HashMap<>();

    static {
        for (org.bukkit.entity.EntityType type : org.bukkit.entity.EntityType.values()) {
            if (type == org.bukkit.entity.EntityType.UNKNOWN) continue;
            ENTITY_TYPE_ENTITY_TYPES.put(type, BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(type.getKey())));
            ENTITY_TYPES_ENTITY_TYPE.put(BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(type.getKey())), type);
        }
        // Paper end
        for (Block block : BuiltInRegistries.BLOCK) {
            BLOCK_MATERIAL.put(block, Material.getMaterial(BuiltInRegistries.BLOCK.getKey(block).getPath().toUpperCase(Locale.ROOT)));
        }

        for (Item item : BuiltInRegistries.ITEM) {
            ITEM_MATERIAL.put(item, Material.getMaterial(BuiltInRegistries.ITEM.getKey(item).getPath().toUpperCase(Locale.ROOT)));
        }

        for (Material material : Material.values()) {
            if (material.isLegacy()) {
                continue;
            }

            ResourceLocation key = key(material);
            BuiltInRegistries.ITEM.getOptional(key).ifPresent((item) -> {
                CraftMagicNumbers.MATERIAL_ITEM.put(material, item);
            });
            BuiltInRegistries.BLOCK.getOptional(key).ifPresent((block) -> {
                CraftMagicNumbers.MATERIAL_BLOCK.put(material, block);
            });
        }
    }

    public static Material getMaterial(Block block) {
        return CraftMagicNumbers.BLOCK_MATERIAL.get(block);
    }

    public static Material getMaterial(Item item) {
        return CraftMagicNumbers.ITEM_MATERIAL.getOrDefault(item, Material.AIR);
    }

    public static Item getItem(Material material) {
        if (material != null && material.isLegacy()) {
            material = CraftLegacy.fromLegacy(material);
        }

        return CraftMagicNumbers.MATERIAL_ITEM.get(material);
    }

    public static Block getBlock(Material material) {
        if (material != null && material.isLegacy()) {
            material = CraftLegacy.fromLegacy(material);
        }

        return CraftMagicNumbers.MATERIAL_BLOCK.get(material);
    }

    public static ResourceLocation key(Material mat) {
        return CraftNamespacedKey.toMinecraft(mat.getKey());
    }
    // Paper start
    public static net.minecraft.world.entity.EntityType<?> getEntityTypes(org.bukkit.entity.EntityType type) {
        return ENTITY_TYPE_ENTITY_TYPES.get(type);
    }
    public static org.bukkit.entity.EntityType getEntityType(net.minecraft.world.entity.EntityType<?> entityTypes) {
        return ENTITY_TYPES_ENTITY_TYPE.get(entityTypes);
    }
    // Paper end
    // ========================================================================
    // Paper start
    @Override
    public void reportTimings() {
        co.aikar.timings.TimingsExport.reportTimings();
    }
    // Paper end

    public static byte toLegacyData(BlockState data) {
        return CraftLegacy.toLegacyData(data);
    }

    @Override
    public Material toLegacy(Material material) {
        return CraftLegacy.toLegacy(material);
    }

    @Override
    public Material fromLegacy(Material material) {
        return CraftLegacy.fromLegacy(material);
    }

    @Override
    public Material fromLegacy(MaterialData material) {
        return CraftLegacy.fromLegacy(material);
    }

    @Override
    public Material fromLegacy(MaterialData material, boolean itemPriority) {
        return CraftLegacy.fromLegacy(material, itemPriority);
    }

    @Override
    public BlockData fromLegacy(Material material, byte data) {
        return CraftBlockData.fromData(CraftMagicNumbers.getBlock(material, data));
    }

    @Override
    public Material getMaterial(String material, int version) {
        Preconditions.checkArgument(material != null, "material == null");
        Preconditions.checkArgument(version <= this.getDataVersion(), "Newer version! Server downgrades are not supported!");

        // Fastpath up to date materials
        if (version == this.getDataVersion()) {
            return Material.getMaterial(material);
        }

        Dynamic<Tag> name = new Dynamic<>(NbtOps.INSTANCE, StringTag.valueOf("minecraft:" + material.toLowerCase(Locale.ROOT)));
        Dynamic<Tag> converted = DataFixers.getDataFixer().update(References.ITEM_NAME, name, version, this.getDataVersion());

        if (name.equals(converted)) {
            converted = DataFixers.getDataFixer().update(References.BLOCK_NAME, name, version, this.getDataVersion());
        }

        return Material.matchMaterial(converted.asString(""));
    }

    /**
     * This string should be changed if the NMS mappings do.
     *
     * It has no meaning and should only be used as an equality check. Plugins
     * which are sensitive to the NMS mappings may read it and refuse to load if
     * it cannot be found or is different to the expected value.
     *
     * Remember: NMS is not supported API and may break at any time for any
     * reason irrespective of this. There is often supported API to do the same
     * thing as many common NMS usages. If not, you are encouraged to open a
     * feature and/or pull request for consideration, or use a well abstracted
     * third-party API such as ProtocolLib.
     *
     * @return string
     */
    public String getMappingsVersion() {
        return "60a2bb6bf2684dc61c56b90d7c41bddc";
    }

    @Override
    public int getDataVersion() {
        return SharedConstants.getCurrentVersion().getDataVersion().getVersion();
    }

    @Override
    public ItemStack modifyItemStack(ItemStack stack, String arguments) {
        net.minecraft.world.item.ItemStack nmsStack = CraftItemStack.asNMSCopy(stack);

        try {
            nmsStack.setTag((CompoundTag) TagParser.parseTag(arguments));
        } catch (CommandSyntaxException ex) {
            Logger.getLogger(CraftMagicNumbers.class.getName()).log(Level.SEVERE, null, ex);
        }

        stack.setItemMeta(CraftItemStack.getItemMeta(nmsStack));

        return stack;
    }

    private static File getBukkitDataPackFolder() {
        return new File(MinecraftServer.getServer().getWorldPath(LevelResource.DATAPACK_DIR).toFile(), "bukkit");
    }

    @Override
    public Advancement loadAdvancement(NamespacedKey key, String advancement) {
        Preconditions.checkArgument(Bukkit.getAdvancement(key) == null, "Advancement %s already exists", key);
        ResourceLocation minecraftkey = CraftNamespacedKey.toMinecraft(key);

        JsonElement jsonelement = ServerAdvancementManager.GSON.fromJson(advancement, JsonElement.class);
        net.minecraft.advancements.Advancement nms = Util.getOrThrow(net.minecraft.advancements.Advancement.CODEC.parse(JsonOps.INSTANCE, jsonelement), JsonParseException::new);
        if (nms != null) {
            // Paper start - Fix throw UnsupportedOperationException
            //MinecraftServer.getServer().getAdvancements().advancements.put(minecraftkey, new AdvancementHolder(minecraftkey, nms));
            final com.google.common.collect.ImmutableMap.Builder<ResourceLocation, AdvancementHolder> mapBuilder = com.google.common.collect.ImmutableMap.builder();
            mapBuilder.putAll(MinecraftServer.getServer().getAdvancements().advancements);

            final AdvancementHolder holder = new AdvancementHolder(minecraftkey, nms);
            mapBuilder.put(minecraftkey, holder);

            MinecraftServer.getServer().getAdvancements().advancements = mapBuilder.build();
            final net.minecraft.advancements.AdvancementTree tree = MinecraftServer.getServer().getAdvancements().tree();
            tree.addAll(List.of(holder));

            // recalculate advancement position
            final net.minecraft.advancements.AdvancementNode node = tree.get(minecraftkey);
            if (node != null) {
                final net.minecraft.advancements.AdvancementNode root = node.root();
                if (root.holder().value().display().isPresent()) {
                    net.minecraft.advancements.TreeNodePosition.run(root);
                }
            }
            // Paper end - Fix throw UnsupportedOperationException
            Advancement bukkit = Bukkit.getAdvancement(key);

            if (bukkit != null) {
                File file = new File(CraftMagicNumbers.getBukkitDataPackFolder(), "data" + File.separator + key.getNamespace() + File.separator + "advancements" + File.separator + key.getKey() + ".json");
                file.getParentFile().mkdirs();

                try {
                    Files.write(advancement, file, Charsets.UTF_8);
                } catch (IOException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "Error saving advancement " + key, ex);
                }

                // Paper start - Fix client lag on advancement loading
                //MinecraftServer.getServer().getPlayerList().reload();
                MinecraftServer.getServer().getPlayerList().getPlayers().forEach(player -> {
                    player.getAdvancements().reload(MinecraftServer.getServer().getAdvancements());
                    player.getAdvancements().flushDirty(player);
                });
                // Paper end - Fix client lag on advancement loading

                return bukkit;
            }
        }

        return null;
    }

    @Override
    public boolean removeAdvancement(NamespacedKey key) {
        File file = new File(CraftMagicNumbers.getBukkitDataPackFolder(), "data" + File.separator + key.getNamespace() + File.separator + "advancements" + File.separator + key.getKey() + ".json");
        return file.delete();
    }

    private static final List<String> SUPPORTED_API = Arrays.asList("1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20");

    @Override
    public void checkSupported(PluginDescriptionFile pdf) throws InvalidPluginException {
        String minimumVersion = MinecraftServer.getServer().server.minimumAPI;
        int minimumIndex = CraftMagicNumbers.SUPPORTED_API.indexOf(minimumVersion);

        if (pdf.getAPIVersion() != null) {
            int pluginIndex = CraftMagicNumbers.SUPPORTED_API.indexOf(pdf.getAPIVersion());

            if (pluginIndex == -1) {
                throw new InvalidPluginException("Unsupported API version " + pdf.getAPIVersion());
            }

            if (pluginIndex < minimumIndex) {
                throw new InvalidPluginException("Plugin API version " + pdf.getAPIVersion() + " is lower than the minimum allowed version. Please update or replace it.");
            }
        } else {
            if (minimumIndex == -1) {
                CraftLegacy.init();
                Bukkit.getLogger().log(Level.WARNING, "Legacy plugin " + pdf.getFullName() + " does not specify an api-version.");
            } else {
                throw new InvalidPluginException("Plugin API version " + pdf.getAPIVersion() + " is lower than the minimum allowed version. Please update or replace it.");
            }
        }
    }

    public static boolean isLegacy(PluginDescriptionFile pdf) {
        return pdf.getAPIVersion() == null;
    }

    @Override
    public byte[] processClass(PluginDescriptionFile pdf, String path, byte[] clazz) {
        try {
            clazz = Commodore.convert(clazz, !CraftMagicNumbers.isLegacy(pdf));
        } catch (Exception ex) {
            Bukkit.getLogger().log(Level.SEVERE, "Fatal error trying to convert " + pdf.getFullName() + ":" + path, ex);
        }

        return clazz;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(Material material, EquipmentSlot slot) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> defaultAttributes = ImmutableMultimap.builder();

        Multimap<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> nmsDefaultAttributes = CraftMagicNumbers.getItem(material).getDefaultAttributeModifiers(CraftEquipmentSlot.getNMS(slot));
        for (Entry<net.minecraft.world.entity.ai.attributes.Attribute, net.minecraft.world.entity.ai.attributes.AttributeModifier> mapEntry : nmsDefaultAttributes.entries()) {
            Attribute attribute = CraftAttribute.minecraftToBukkit(mapEntry.getKey());
            defaultAttributes.put(attribute, CraftAttributeInstance.convert(mapEntry.getValue(), slot));
        }

        return defaultAttributes.build();
    }

    @Override
    public CreativeCategory getCreativeCategory(Material material) {
        return CreativeCategory.BUILDING_BLOCKS; // TODO: Figure out what to do with this
    }

    @Override
    public String getBlockTranslationKey(Material material) {
        Block block = CraftMagicNumbers.getBlock(material);
        return (block != null) ? block.getDescriptionId() : null;
    }

    @Override
    public String getItemTranslationKey(Material material) {
        Item item = CraftMagicNumbers.getItem(material);
        return (item != null) ? item.getDescriptionId() : null;
    }

    @Override
    public String getTranslationKey(EntityType entityType) {
        Preconditions.checkArgument(entityType.getName() != null, "Invalid name of EntityType %s for translation key", entityType);
        return net.minecraft.world.entity.EntityType.byString(entityType.getName()).map(net.minecraft.world.entity.EntityType::getDescriptionId).orElseThrow();
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.getItem().getDescriptionId(nmsItemStack);
    }
    // Paper start
    @Override
    public boolean isSupportedApiVersion(String apiVersion) {
        return apiVersion != null && SUPPORTED_API.contains(apiVersion);
    }
    // Paper end

    @Override
    public String getTranslationKey(final Attribute attribute) {
        return CraftAttribute.bukkitToMinecraft(attribute).getDescriptionId();
    }

    @Override
    public FeatureFlag getFeatureFlag(NamespacedKey namespacedKey) {
        Preconditions.checkArgument(namespacedKey != null, "NamespaceKey cannot be null");
        return CraftFeatureFlag.getFromNMS(namespacedKey);
    }

    @Override
    public PotionType.InternalPotionData getInternalPotionData(NamespacedKey namespacedKey) {
        Potion potionRegistry = CraftRegistry.getMinecraftRegistry(Registries.POTION)
                .getOptional(CraftNamespacedKey.toMinecraft(namespacedKey)).orElseThrow();

        return new CraftPotionType(namespacedKey, potionRegistry);
    }

    @Override
    public DamageEffect getDamageEffect(String key) {
        Preconditions.checkArgument(key != null, "key cannot be null");
        return CraftDamageEffect.getById(key);
    }

    @Override
    public DamageSource.Builder createDamageSourceBuilder(DamageType damageType) {
        return new CraftDamageSourceBuilder(damageType);
    }
    // Paper start
    @Override
    public String getTimingsServerName() {
        return io.papermc.paper.configuration.GlobalConfiguration.get().timings.serverName;
    }

    @Override
    public com.destroystokyo.paper.util.VersionFetcher getVersionFetcher() {
        return new com.destroystokyo.paper.PaperVersionFetcher();
    }

    @Override
    public byte[] serializeItem(ItemStack item) {
        Preconditions.checkNotNull(item, "null cannot be serialized");
        Preconditions.checkArgument(item.getType() != Material.AIR, "air cannot be serialized");

        return serializeNbtToBytes((item instanceof CraftItemStack ? ((CraftItemStack) item).handle : CraftItemStack.asNMSCopy(item)).save(new CompoundTag()));
    }

    @Override
    public ItemStack deserializeItem(byte[] data) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");

        CompoundTag compound = deserializeNbtFromBytes(data);
        final int dataVersion = compound.getInt("DataVersion");
        return CraftItemStack.asCraftMirror(net.minecraft.world.item.ItemStack.of(ca.spottedleaf.dataconverter.minecraft.MCDataConverter.convertTag(ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry.ITEM_STACK, compound, dataVersion, this.getDataVersion()))); // Paper - rewrite dataconverter
    }

    @Override
    public byte[] serializeEntity(org.bukkit.entity.Entity entity) {
        Preconditions.checkNotNull(entity, "null cannot be serialized");
        Preconditions.checkArgument(entity instanceof org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity, "only CraftEntities can be serialized");

        CompoundTag compound = new CompoundTag();
        ((org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity) entity).getHandle().serializeEntity(compound);
        return serializeNbtToBytes(compound);
    }

    @Override
    public org.bukkit.entity.Entity deserializeEntity(byte[] data, org.bukkit.World world, boolean preserveUUID) {
        Preconditions.checkNotNull(data, "null cannot be deserialized");
        Preconditions.checkArgument(data.length > 0, "cannot deserialize nothing");

        CompoundTag compound = deserializeNbtFromBytes(data);
        int dataVersion = compound.getInt("DataVersion");
        compound = ca.spottedleaf.dataconverter.minecraft.MCDataConverter.convertTag(ca.spottedleaf.dataconverter.minecraft.datatypes.MCTypeRegistry.ENTITY, compound, dataVersion, getDataVersion()); // Paper - rewrite dataconverter
        if (!preserveUUID) {
            // Generate a new UUID so we don't have to worry about deserializing the same entity twice
            compound.remove("UUID");
        }
        return net.minecraft.world.entity.EntityType.create(compound, ((org.bukkit.craftbukkit.v1_20_R3.CraftWorld) world).getHandle())
            .orElseThrow(() -> new IllegalArgumentException("An ID was not found for the data. Did you downgrade?")).getBukkitEntity();
    }

    private byte[] serializeNbtToBytes(CompoundTag compound) {
        compound.putInt("DataVersion", getDataVersion());
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        try {
            net.minecraft.nbt.NbtIo.writeCompressed(
                compound,
                outputStream
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return outputStream.toByteArray();
    }

    private CompoundTag deserializeNbtFromBytes(byte[] data) {
        CompoundTag compound;
        try {
            compound = net.minecraft.nbt.NbtIo.readCompressed(
                new java.io.ByteArrayInputStream(data), net.minecraft.nbt.NbtAccounter.unlimitedHeap()
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        int dataVersion = compound.getInt("DataVersion");
        Preconditions.checkArgument(dataVersion <= getDataVersion(), "Newer version! Server downgrades are not supported!");
        return compound;
    }

    @Override
    public int nextEntityId() {
        return net.minecraft.world.entity.Entity.nextEntityId();
    }

    @Override
    public String getMainLevelName() {
        return ((net.minecraft.server.dedicated.DedicatedServer) net.minecraft.server.MinecraftServer.getServer()).getProperties().levelName;
    }

    @Override
    public io.papermc.paper.inventory.ItemRarity getItemRarity(org.bukkit.Material material) {
        Item item = getItem(material);
        if (item == null) {
            throw new IllegalArgumentException(material + " is not an item, and rarity does not apply to blocks");
        }
        return io.papermc.paper.inventory.ItemRarity.values()[item.rarity.ordinal()];
    }

    @Override
    public io.papermc.paper.inventory.ItemRarity getItemStackRarity(org.bukkit.inventory.ItemStack itemStack) {
        return io.papermc.paper.inventory.ItemRarity.values()[getItem(itemStack.getType()).getRarity(CraftItemStack.asNMSCopy(itemStack)).ordinal()];
    }

    @Override
    public boolean isValidRepairItemStack(org.bukkit.inventory.ItemStack itemToBeRepaired, org.bukkit.inventory.ItemStack repairMaterial) {
        if (!itemToBeRepaired.getType().isItem() || !repairMaterial.getType().isItem()) {
            return false;
        }
        return CraftMagicNumbers.getItem(itemToBeRepaired.getType()).isValidRepairItem(CraftItemStack.asNMSCopy(itemToBeRepaired), CraftItemStack.asNMSCopy(repairMaterial));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getItemAttributes(Material material, EquipmentSlot equipmentSlot) {
        Item item = CraftMagicNumbers.getItem(material);
        if (item == null) {
            throw new IllegalArgumentException(material + " is not an item and therefore does not have attributes");
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeMapBuilder = ImmutableMultimap.builder();
        item.getDefaultAttributeModifiers(CraftEquipmentSlot.getNMS(equipmentSlot)).forEach((attributeBase, attributeModifier) -> {
            attributeMapBuilder.put(CraftAttribute.stringToBukkit(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getKey(attributeBase).toString()), CraftAttributeInstance.convert(attributeModifier, equipmentSlot));
        });
        return attributeMapBuilder.build();
    }

    @Override
    public int getProtocolVersion() {
        return net.minecraft.SharedConstants.getCurrentVersion().getProtocolVersion();
    }

    @Override
    public boolean hasDefaultEntityAttributes(NamespacedKey bukkitEntityKey) {
        return net.minecraft.world.entity.ai.attributes.DefaultAttributes.hasSupplier(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(bukkitEntityKey)));
    }

    @Override
    public org.bukkit.attribute.Attributable getDefaultEntityAttributes(NamespacedKey bukkitEntityKey) {
        Preconditions.checkArgument(hasDefaultEntityAttributes(bukkitEntityKey), bukkitEntityKey + " doesn't have default attributes");
        var supplier = net.minecraft.world.entity.ai.attributes.DefaultAttributes.getSupplier((net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.LivingEntity>) net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.get(CraftNamespacedKey.toMinecraft(bukkitEntityKey)));
        return new io.papermc.paper.attribute.UnmodifiableAttributeMap(supplier);
    }

    @Override
    public boolean isCollidable(Material material) {
        Preconditions.checkArgument(material.isBlock(), material + " is not a block");
        return getBlock(material).hasCollision;
    }

    @Override
    public org.bukkit.NamespacedKey getBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z) {
        org.bukkit.craftbukkit.v1_20_R3.CraftRegionAccessor cra = (org.bukkit.craftbukkit.v1_20_R3.CraftRegionAccessor) accessor;
        return org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey.fromMinecraft(cra.getHandle().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getKey(cra.getHandle().getBiome(new net.minecraft.core.BlockPos(x, y, z)).value()));
    }

    @Override
    public void setBiomeKey(org.bukkit.RegionAccessor accessor, int x, int y, int z, org.bukkit.NamespacedKey biomeKey) {
        org.bukkit.craftbukkit.v1_20_R3.CraftRegionAccessor cra = (org.bukkit.craftbukkit.v1_20_R3.CraftRegionAccessor) accessor;
        net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeBase = cra.getHandle().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BIOME, org.bukkit.craftbukkit.v1_20_R3.util.CraftNamespacedKey.toMinecraft(biomeKey)));
        cra.setBiome(x, y, z, biomeBase);
    }

    @Override
    public String getStatisticCriteriaKey(org.bukkit.Statistic statistic) {
        if (statistic.getType() != org.bukkit.Statistic.Type.UNTYPED) return "minecraft.custom:minecraft." + statistic.getKey().getKey();
        return org.bukkit.craftbukkit.v1_20_R3.CraftStatistic.getNMSStatistic(statistic).getName();
    }
    // Paper end
    // Paper start - expose itemstack tooltip lines
    @Override
    public List<net.kyori.adventure.text.Component> computeTooltipLines(final ItemStack itemStack, final io.papermc.paper.inventory.tooltip.TooltipContext tooltipContext, final org.bukkit.entity.Player player) {
        Preconditions.checkArgument(tooltipContext != null, "tooltipContext cannot be null");
        net.minecraft.world.item.TooltipFlag.Default flag = tooltipContext.isAdvanced() ? net.minecraft.world.item.TooltipFlag.ADVANCED : net.minecraft.world.item.TooltipFlag.NORMAL;
        if (tooltipContext.isCreative()) {
            flag = flag.asCreative();
        }
        final List<net.minecraft.network.chat.Component> lines = CraftItemStack.asNMSCopy(itemStack).getTooltipLines(player == null ? null : ((org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer) player).getHandle(), flag);
        return lines.stream().map(io.papermc.paper.adventure.PaperAdventure::asAdventure).toList();
    }
    // Paper end - expose itemstack tooltip lines

    // Paper start - spawn egg color visibility
    @Override
    public org.bukkit.Color getSpawnEggLayerColor(final EntityType entityType, final int layer) {
        final net.minecraft.world.entity.EntityType<?> nmsType = org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntityType.bukkitToMinecraft(entityType);
        final net.minecraft.world.item.SpawnEggItem eggItem = net.minecraft.world.item.SpawnEggItem.byId(nmsType);
        return eggItem == null ? null : org.bukkit.Color.fromRGB(eggItem.getColor(layer));
    }
    // Paper end - spawn egg color visibility

    // Paper start - lifecycle event API
    @Override
    public io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager<org.bukkit.plugin.Plugin> createPluginLifecycleEventManager(final org.bukkit.plugin.java.JavaPlugin plugin, final java.util.function.BooleanSupplier registrationCheck) {
        return new io.papermc.paper.plugin.lifecycle.event.PaperLifecycleEventManager<>(plugin, registrationCheck);
    }
    // Paper end - lifecycle event API

    /**
     * This helper class represents the different NBT Tags.
     * <p>
     * These should match NBTBase#getTypeId
     */
    public static class NBT {

        public static final int TAG_END = 0;
        public static final int TAG_BYTE = 1;
        public static final int TAG_SHORT = 2;
        public static final int TAG_INT = 3;
        public static final int TAG_LONG = 4;
        public static final int TAG_FLOAT = 5;
        public static final int TAG_DOUBLE = 6;
        public static final int TAG_BYTE_ARRAY = 7;
        public static final int TAG_STRING = 8;
        public static final int TAG_LIST = 9;
        public static final int TAG_COMPOUND = 10;
        public static final int TAG_INT_ARRAY = 11;
        public static final int TAG_ANY_NUMBER = 99;
    }
}
