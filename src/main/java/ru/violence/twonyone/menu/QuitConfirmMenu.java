package ru.violence.twonyone.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.menu.Menu;
import ru.violence.coreapi.bukkit.api.menu.MenuHelper;
import ru.violence.coreapi.bukkit.api.menu.button.Buttons;
import ru.violence.coreapi.bukkit.api.util.MessageUtil;
import ru.violence.coreapi.bukkit.util.ItemBuilder;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.GameTable;

public class QuitConfirmMenu extends Menu {
    private final GameTable table;

    public QuitConfirmMenu(@NotNull Player player, GameTable table) {
        super(player, MessageUtil.renderLegacy(player, LangKeys.MENU_QUIT_TITLE), 27);
        this.table = table;
    }

    @Override
    public void onInitialize() {
        setButton(11, Buttons.makeSimple(new ItemBuilder(Material.WOOL, 1, (short) 14).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_QUIT_CONFIRM)), (player, menu, button, clickType) -> {
            player.closeInventory();
            TwonyOnePlugin.getInstance().getGameManager().removeFromGame(player);
        }));
        setButton(15, Buttons.makeSimple(new ItemBuilder(Material.WOOL, 1, (short) 8).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_QUIT_CANCEL)), (player, menu, button, clickType) -> player.closeInventory()));

        MenuHelper.fillBorder(this);
    }
}
