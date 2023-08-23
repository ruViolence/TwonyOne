package ru.violence.twonyone.game;

import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.util.ItemBuilder;
import ru.violence.coreapi.bukkit.api.util.RendererHelper;
import ru.violence.coreapi.common.api.util.Check;
import ru.violence.twonyone.LangKeys;

public class GamePlayer {
    private final @Getter @NotNull Player player;
    private final @Getter @NotNull GameChair chair;
    private ItemStack[] reservedItems;

    public GamePlayer(@NotNull Player player, @NotNull GameChair chair) {
        this.player = player;
        this.chair = chair;
    }

    public void giveLeaveItem() {
        player.getInventory().setItem(8, new ItemBuilder(Material.DARK_OAK_DOOR_ITEM).display(RendererHelper.legacy(player, LangKeys.GAME_ITEM_LEAVE)).build());
    }

    public void giveTurnItems() {
        player.getInventory().setItem(0, new ItemBuilder(Material.WOOL, 1, (short) 1).display(RendererHelper.legacy(player, LangKeys.GAME_ITEM_DRAW)).build());
        player.getInventory().setItem(1, new ItemBuilder(Material.WOOL, 1, (short) 8).display(RendererHelper.legacy(player, LangKeys.GAME_ITEM_KEEP)).build());
    }

    public void hideTurnItems() {
        player.getInventory().setItem(0, null);
        player.getInventory().setItem(1, null);
    }

    public void reservedItems() {
        Check.isTrue(reservedItems == null);
        this.reservedItems = player.getInventory().getContents().clone();
        player.getInventory().clear();
    }

    public void getBackItems() {
        player.getInventory().clear();
        player.getInventory().setContents(reservedItems);
    }

    public @NotNull GameTable getTable() {
        return getChair().getTable();
    }
}
