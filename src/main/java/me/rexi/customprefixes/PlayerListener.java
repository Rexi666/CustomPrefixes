package me.rexi.customprefixes;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.util.Tristate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    private final DatabaseManager databaseManager;
    private final LuckPerms luckPerms;
    private final CustomPrefixes plugin;

    // Constructor que acepta DatabaseManager y LuckPerms
    public PlayerListener(DatabaseManager databaseManager, LuckPerms luckPerms, CustomPrefixes plugin) {
        this.databaseManager = databaseManager;
        this.luckPerms = luckPerms;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Verifica si luckPerms está inicializado
        if (luckPerms == null) {
            Bukkit.getLogger().warning("LuckPerms is not initialized in PlayerListener.");
            return;
        }

        // Obtiene el UserManager y User
        UserManager userManager = luckPerms.getUserManager();
        userManager.loadUser(player.getUniqueId()).thenAcceptAsync(user -> {
            if (user == null) {
                return;
            }

            // Verifica permisos del usuario
            Tristate hasTagPermission = user.getCachedData().getPermissionData().checkPermission("customprefixes.tag");

            // Obtiene el tag actual del jugador
            String currentTag = databaseManager.getPlayerTag(player.getUniqueId());

            if (currentTag != null && hasTagPermission != Tristate.TRUE) {
                Bukkit.getLogger().info("Removing tag for player " + player.getName());
                removeTag(player);
                databaseManager.removeTag(player.getUniqueId());
            }
        });

    }

    private void removeTag(Player player) {
        String removeCommand = "lp user " + player.getName() + " meta clear";
        Bukkit.getScheduler().runTask(plugin, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), removeCommand));
    }
}
