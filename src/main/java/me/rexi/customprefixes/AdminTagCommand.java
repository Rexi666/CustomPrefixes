package me.rexi.customprefixes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.UUID;

public class AdminTagCommand implements CommandExecutor {
    Component prefix = Component.text("[").color(NamedTextColor.GRAY).append(Component.text("Custom").color(NamedTextColor.LIGHT_PURPLE)).append(Component.text("Prefixes").color(NamedTextColor.DARK_PURPLE)).append(Component.text("]").color(NamedTextColor.GRAY));

    private final CustomPrefixes plugin;
    private final DatabaseManager databaseManager;

    public AdminTagCommand(CustomPrefixes plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customprefixes.admin")) {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" You don't have permission to use this command").color(NamedTextColor.RED)));
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Configuration reloaded successfully").color(NamedTextColor.GREEN)));
            return true;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("resetcooldown")) {
            OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(args[1]);
            UUID targetUUID = targetPlayer.getUniqueId();

            long currentTime = System.currentTimeMillis();
            databaseManager.resetCooldown(targetUUID, currentTime);
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Cooldown reset for player " + targetPlayer.getName()).color(NamedTextColor.GREEN)));
            return true;
        }

        sender.sendMessage(Component.text("").append(prefix)
                .append(Component.text(" That command is not correct").color(NamedTextColor.RED)));
        sender.sendMessage(Component.text("").append(prefix)
                .append(Component.text(" - /admintag reload").color(NamedTextColor.GOLD))
                .append(Component.text(" - ").color(NamedTextColor.GRAY))
                .append(Component.text("Reload the plugin's config")));
        sender.sendMessage(Component.text("").append(prefix)
                .append(Component.text(" - /admintag resetcooldown <player>").color(NamedTextColor.GOLD))
                .append(Component.text(" - ").color(NamedTextColor.GRAY))
                .append(Component.text("Delete someone's tag cooldown")));
        return true;
    }
}
