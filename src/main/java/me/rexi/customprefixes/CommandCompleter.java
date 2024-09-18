package me.rexi.customprefixes;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("admintag")) {
            // Manejar subcomandos
            if (args.length == 1) {
                List<String> subcommands = Arrays.asList("resetcooldown", "reload");
                return filterByStart(subcommands, args[0]);
            }

            // Manejar el segundo argumento para "resetcooldown"
            if (args.length == 2 && "resetcooldown".equalsIgnoreCase(args[0])) {
                return getPlayerNamesStartingWith(args[1]);
            }
        }
        return new ArrayList<>();
    }

    private List<String> filterByStart(List<String> options, String start) {
        List<String> filteredOptions = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase().startsWith(start.toLowerCase())) {
                filteredOptions.add(option);
            }
        }
        return filteredOptions;
    }

    private List<String> getPlayerNamesStartingWith(String start) {
        List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(start.toLowerCase())) {
                playerNames.add(player.getName());
            }
        }
        return playerNames;
    }
}