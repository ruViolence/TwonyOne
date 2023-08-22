package ru.violence.twonyone.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.spigotmc.event.entity.EntityDismountEvent;
import ru.violence.twonyone.TwonyOnePlugin;

public class DismountPreventListener implements Listener {
    private final TwonyOnePlugin plugin;

    public DismountPreventListener(TwonyOnePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (plugin.getGameManager().isInGame(player)) {
            event.setCancelled(true);
        }
    }
}
