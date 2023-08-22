package ru.violence.twonyone.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.violence.twonyone.TwonyOnePlugin;

public class QuitInGameListener implements Listener {
    private final TwonyOnePlugin plugin;

    public QuitInGameListener(TwonyOnePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getGameManager().removeFromGame(player);
    }
}
