package me.rexi.customprefixes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomPrefixes extends JavaPlugin {

    private DatabaseManager databaseManager;
    private LuckPerms luckPerms;

    Component prefix = Component.text("[").color(NamedTextColor.GRAY)
            .append(Component.text("Custom").color(NamedTextColor.LIGHT_PURPLE))
            .append(Component.text("Prefixes").color(NamedTextColor.DARK_PURPLE))
            .append(Component.text("]").color(NamedTextColor.GRAY));

    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration config = getConfig();
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
}
