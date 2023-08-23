package ru.violence.twonyone.menu;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.violence.coreapi.bukkit.api.menu.Menu;
import ru.violence.coreapi.bukkit.api.menu.MenuHelper;
import ru.violence.coreapi.bukkit.api.menu.button.Button;
import ru.violence.coreapi.bukkit.api.menu.listener.ClickListener;
import ru.violence.coreapi.bukkit.api.util.ItemBuilder;
import ru.violence.coreapi.bukkit.api.util.RendererHelper;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.GameTable;

@UtilityClass
public class QuitConfirmMenu {
    public void createAndOpen(Player player, GameTable table) {
        Menu.newBuilder(TwonyOnePlugin.getInstance())
                .title(RendererHelper.legacy(player, LangKeys.MENU_QUIT_TITLE))
                .size(27)
                .clickListener(ClickListener.cancel())
                .openListener(openEvent -> {
                    Menu menu = openEvent.getMenu();

                    menu.setButton(11, Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 14).display(RendererHelper.legacy(player, LangKeys.MENU_QUIT_CONFIRM)).build()).action(clickEvent -> {
                        player.closeInventory();
                        TwonyOnePlugin.getInstance().getGameManager().removeFromGame(player);
                    }));
                    menu.setButton(15, Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 8).display(RendererHelper.legacy(player, LangKeys.MENU_QUIT_CANCEL)).build()).action(clickEvent -> player.closeInventory()));

                    MenuHelper.fillBorder(menu);
                })
                .build()
                .open(player);
    }
}
