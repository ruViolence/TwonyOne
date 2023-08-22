package ru.violence.twonyone.listener;

import net.blackscarx.betterchairs.event.PreSitEvent;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Set;

public class ChairSitPreventListener implements Listener {
    private final Set<Block> blocks;

    public ChairSitPreventListener(Set<Block> blocks) {
        this.blocks = blocks;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreSit(PreSitEvent event) {
        if (event.getBlock() != null && blocks.contains(event.getBlock())) {
            event.setCancelled(true);
        }
    }
}
