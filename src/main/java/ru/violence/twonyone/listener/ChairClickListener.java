package ru.violence.twonyone.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import ru.violence.coreapi.bukkit.api.BukkitHelper;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.State;
import ru.violence.twonyone.menu.BetAdjustMenu;
import ru.violence.twonyone.menu.BetConfirmMenu;

public class ChairClickListener implements Listener {
    private final TwonyOnePlugin plugin;

    public ChairClickListener(TwonyOnePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.hasBlock()) return;
        if (event.hasItem()) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        // Already in game
        if (plugin.getGameManager().isInGame(player)) return;

        plugin.getGameManager().getGameChair(block).ifPresent(chair -> {
            // Wrong game state
            if (chair.getTable().getState() != State.WAITING) return;

            if (chair.isOccupied()) {
                BukkitHelper.getUser(player).sendMessage(LangKeys.CHAIR_OCCUPIED);
                return;
            }

            // Can't reach
            if (!chair.canSit(player)) return;

            if (chair.getTable().isBetSet()) {
                new BetConfirmMenu(player, chair).open();
            } else {
                new BetAdjustMenu(player, chair).open();
            }
        });
    }
}
