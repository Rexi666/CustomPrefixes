package me.rexi.customprefixes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfirmTagCommand implements CommandExecutor {

    Component prefix = Component.text("[").color(NamedTextColor.GRAY).append(Component.text("Custom").color(NamedTextColor.LIGHT_PURPLE)).append(Component.text("Prefixes").color(NamedTextColor.DARK_PURPLE)).append(Component.text("]").color(NamedTextColor.GRAY));

    private final TagCommand tagCommand;
    private final CustomPrefixes plugin;

    public ConfirmTagCommand(TagCommand tagCommand, CustomPrefixes plugin) {
        this.tagCommand = tagCommand;
        this.plugin = plugin;
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
                    .append(Component.text(" You don't have permission to use this command.").color(NamedTextColor.RED)));
            return false;
        }

        if (!plugin.getConfig().getBoolean("tag-change-confirmation", true)) {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" The use of /confirmtag is disabled in the configuration.").color(NamedTextColor.RED)));
            return false;
        }

        boolean confirmed = tagCommand.confirmTag(player);

        if (!confirmed) {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Failed to confirm tag. No pending tag found").color(NamedTextColor.RED)));
        } else {
            sender.sendMessage(Component.text("").append(prefix)
                    .append(Component.text(" Your tag has been successfully confirmed and applied.").color(NamedTextColor.GREEN)));
        }
        return true;
    }
}
