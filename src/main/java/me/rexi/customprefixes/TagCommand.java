package me.rexi.customprefixes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TagCommand implements CommandExecutor {
    Component prefix = Component.text("[").color(NamedTextColor.GRAY)
            .append(Component.text("Custom").color(NamedTextColor.LIGHT_PURPLE))
            .append(Component.text("Prefixes").color(NamedTextColor.DARK_PURPLE))
            .append(Component.text("]").color(NamedTextColor.GRAY));

    private final CustomPrefixes plugin;
    private final DatabaseManager databaseManager;
    private final Map<UUID, String> pendingTags = new HashMap<>();

    public TagCommand(CustomPrefixes plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" This command can only be executed by a player").color(NamedTextColor.RED)));
            return false;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("customprefixes.tag")) {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" You don't have permission to use that command.").color(NamedTextColor.RED)));
            return false;
        }

        if (args.length != 1) {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Incorrect use. Use /tag <tag>").color(NamedTextColor.RED)));
            return false;
        }

        String tag = args[0];
        if (!isValidTag(tag)) {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" The tag contains illegal words.").color(NamedTextColor.RED)));
            return false;
        }

        String tagFormat = plugin.getConfig().getString("tag-format", "%tag%");
        String formattedTag = tagFormat.replace("%tag%", tag);
        formattedTag = ChatColor.translateAlternateColorCodes('&', formattedTag);

        long lastChanged = databaseManager.getLastTagChange(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        long cooldownDays = plugin.getConfig().getLong("tag-change-cooldown");
        long cooldown = cooldownDays * 86400000; // Convertir días a milisegundos

        if (currentTime - lastChanged < cooldown) {
            long remainingTime = cooldown - (currentTime - lastChanged);
            long remainingDays = remainingTime / 86400000;
            long remainingHours = (remainingTime % 86400000) / (60 * 60 * 1000);

            if (remainingDays > 0) {
                player.sendMessage(Component.text("").append(prefix)
                        .append(Component.text(" You must wait " + remainingDays + " day(s) and " + remainingHours + " hour(s) before changing the tag again.").color(NamedTextColor.RED)));
            } else {
                player.sendMessage(Component.text("").append(prefix)
                        .append(Component.text(" You must wait " + remainingHours + " hour(s) before changing the tag again.").color(NamedTextColor.RED)));
            }
            return true;
        }

        if (plugin.getConfig().getBoolean("tag-change-confirmation")) {
            pendingTags.put(player.getUniqueId(), tag);
            sender.sendMessage(Component.text(""));
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Preview of your tag: ").color(NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" "))
                    .append(Component.text(formattedTag)));
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Use /confirmtag to apply it.").color(NamedTextColor.YELLOW)));
            sender.sendMessage(Component.text(""));
            return true;
        }

        applyTag(player, tag);
        return true;
    }

    private static String removeColorCodes(String text) {
        return text.replaceAll("(?i)&[0-9a-fk-or]", "");
    }

    private boolean isValidTag(String tag) {
        String cleanedTag = removeColorCodes(tag).toLowerCase();
        String[] forbiddenWords = plugin.getConfig().getStringList("forbidden-words").stream().map(String::toLowerCase).toArray(String[]::new);
        for (String word : forbiddenWords) {
            if (cleanedTag.contains(word)) {
                return false;
            }
        }
        return true;
    }

    public void applyTag(Player player, String tag) {
        long lastChanged = databaseManager.getLastTagChange(player.getUniqueId());
        long currentTime = System.currentTimeMillis();
        long cooldownDays = plugin.getConfig().getLong("tag-change-cooldown");
        long cooldown = cooldownDays * 86400000; // Convertir días a milisegundos

        if (currentTime - lastChanged < cooldown) {
            long remainingTime = cooldown - (currentTime - lastChanged);
            long remainingDays = remainingTime / 86400000;
            long remainingHours = (remainingTime % 86400000) / (60 * 60 * 1000);

            if (remainingDays > 0) {
                player.sendMessage(Component.text("").append(prefix)
                        .append(Component.text(" You must wait " + remainingDays + " day(s) and " + remainingHours + " hour(s) before changing the tag again.").color(NamedTextColor.RED)));
            } else {
                player.sendMessage(Component.text("").append(prefix)
                        .append(Component.text(" You must wait " + remainingHours + " hour(s) before changing the tag again.").color(NamedTextColor.RED)));
            }
            return;
        }

        String tagFormat = plugin.getConfig().getString("tag-format", "%tag%");
        String formattedTag = tagFormat.replace("%tag%", tag);
        formattedTag = ChatColor.translateAlternateColorCodes('&', formattedTag);

        String setTagCommand = "lp user " + player.getName() + " meta setprefix \"" + formattedTag + "\"";
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), setTagCommand);

        databaseManager.updatePlayerTag(player.getUniqueId(), tag, currentTime);
        player.sendMessage(Component.text("").append(prefix)
                .append(Component.text(" Your tag has been changed to ").color(NamedTextColor.GREEN))
                .append(Component.text(formattedTag)));
    }

    public boolean confirmTag(Player player) {
        UUID playerId = player.getUniqueId();
        if (!pendingTags.containsKey(playerId)) {
            return false;
        }

        String tag = pendingTags.get(playerId);
        applyTag(player, tag);
        pendingTags.remove(playerId);
        return true;
    }
}