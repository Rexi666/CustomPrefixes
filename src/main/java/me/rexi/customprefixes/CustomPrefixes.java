package me.rexi.customprefixes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player; // Asegúrate de importar esto
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class CustomPrefixes extends JavaPlugin {

    private DatabaseManager databaseManager;
    private LuckPerms luckPerms;
    private DiscordWebhook discordWebhook;

    private static final int LATEST_CONFIG_VERSION = 1;
    private FileConfiguration config;

    Component prefix = Component.text("[").color(NamedTextColor.GRAY)
            .append(Component.text("Custom").color(NamedTextColor.LIGHT_PURPLE))
            .append(Component.text("Prefixes").color(NamedTextColor.DARK_PURPLE))
            .append(Component.text("]").color(NamedTextColor.GRAY));

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();
        updateConfigIfNecessary();

        String dbType = config.getString("database.type");
        if (dbType == null || (!dbType.equalsIgnoreCase("mysql") && !dbType.equalsIgnoreCase("sqlite"))) {
            Bukkit.getConsoleSender().sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Database type in config.yml is not valid. Use mysql or sqlite").color(NamedTextColor.RED)));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager(config, dbType);

        ServicesManager servicesManager = getServer().getServicesManager();
        luckPerms = servicesManager.getRegistration(LuckPerms.class).getProvider();

        if (luckPerms == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" LuckPerms service not found.").color(NamedTextColor.RED)));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            Bukkit.getConsoleSender().sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" PlaceholderAPI service not found.").color(NamedTextColor.RED)));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        initializeDiscordWebhook();

        TagCommand tagCommand = new TagCommand(this, databaseManager);
        getCommand("tag").setExecutor(tagCommand);
        getCommand("confirmtag").setExecutor(new ConfirmTagCommand(tagCommand, this));
        getCommand("admintag").setExecutor(new AdminTagCommand(this, databaseManager));
        this.getCommand("admintag").setTabCompleter(new CommandCompleter());

        Bukkit.getPluginManager().registerEvents(new PlayerListener(databaseManager, luckPerms, this), this);

        Bukkit.getConsoleSender().sendMessage(Component.text("").append(prefix)
                .append(Component.text(" Plugin has been activated successfully").color(NamedTextColor.GREEN)));
        Bukkit.getConsoleSender().sendMessage(Component.text("").append(prefix)
                .append(Component.text(" Thanks for using Rexi666 plugins :D").color(NamedTextColor.BLUE)));
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }

        Bukkit.getConsoleSender().sendMessage(Component.text("").append(prefix)
                .append(Component.text(" Plugin has been successfully deactivated").color(NamedTextColor.RED)));
        Bukkit.getConsoleSender().sendMessage(Component.text("").append(prefix)
                .append(Component.text(" Thanks for using Rexi666 plugins :D").color(NamedTextColor.BLUE)));
    }

    private void updateConfigIfNecessary() {
        config = getConfig();

        int currentConfigVersion = config.getInt("config-version", 1);

        if (currentConfigVersion < LATEST_CONFIG_VERSION) {
            getLogger().info("An old version of the config has been detected. Updating...");
            config.set("config-version", LATEST_CONFIG_VERSION);
            addNewDefaultConfigValues();
            saveConfig();
            getLogger().info("The config has been updated to version " + LATEST_CONFIG_VERSION + ".");
        }
    }

    private void addNewDefaultConfigValues() {
        if (!config.contains("nueva-config")) {
            config.set("nueva-config", "valor por defecto");
        }
    }

    private void initializeDiscordWebhook() {
        boolean enabled = config.getBoolean("discord-webhook.enabled", false);
        if (enabled) {
            discordWebhook = new DiscordWebhook(config);
        }
    }

    // Cambiado para aceptar un Player en lugar de un String
    public void notifyDiscord(Player player, String newTag) {
        if (discordWebhook != null) {
            discordWebhook.send(player, newTag);
        }
    }

    // Cambiado para aceptar un Player en lugar de un String
    public void onTagChange(Player player, String newTag) {
        notifyDiscord(player, newTag);
    }

    @Override
    public void saveDefaultConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getLogger().info("Config.yml file not found, generating a new one.");
            saveResource("config.yml", false);
        }
    }

    @Override
    public void saveConfig() {
        try {
            getConfig().save(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().severe("Failed to save config file: " + e.getMessage());
        }
    }
}
