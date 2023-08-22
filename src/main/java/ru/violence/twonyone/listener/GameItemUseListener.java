package ru.violence.twonyone.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.GameTable;
import ru.violence.twonyone.game.State;
import ru.violence.twonyone.menu.QuitConfirmMenu;

public class GameItemUseListener implements Listener {
    private final TwonyOnePlugin plugin;

    public GameItemUseListener(TwonyOnePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.hasItem()) return;

        Player player = event.getPlayer();
        plugin.getGameManager().getGamePlayer(player).ifPresent(gamePlayer -> {
            int heldItemSlot = event.getPlayer().getInventory().getHeldItemSlot();
            boolean isHandled = false;
            switch (heldItemSlot) {
                case 0:
                    isHandled = true;
                    handleTurnDraw(player, gamePlayer.getTable());
                    break;
                case 1:
                    isHandled = true;
                    handleTurnKeep(player, gamePlayer.getTable());
                    break;
                case 8:
                    isHandled = true;
                    handleLeave(player, gamePlayer.getTable());
                    break;
            }

            if (isHandled) {
                event.setCancelled(true);
            }
        });
    }

    private void handleTurnDraw(Player player, GameTable table) {
        table.onTurnDraw();
    }

    private void handleTurnKeep(Player player, GameTable table) {
        table.onTurnKeep();
    }

    private void handleLeave(Player player, GameTable table) {
        if (table.getState() == State.PLAYING) {
            new QuitConfirmMenu(player, table).open();
        } else {
            plugin.getGameManager().removeFromGame(player);
        }
    }
}
