package ru.violence.twonyone.menu;

import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.menu.Menu;
import ru.violence.coreapi.bukkit.api.menu.MenuHelper;
import ru.violence.coreapi.bukkit.api.menu.button.Button;
import ru.violence.coreapi.bukkit.api.menu.listener.ClickListener;
import ru.violence.coreapi.bukkit.api.util.BukkitHelper;
import ru.violence.coreapi.bukkit.api.util.ItemBuilder;
import ru.violence.coreapi.bukkit.api.util.RendererHelper;
import ru.violence.coreapi.common.api.user.NotEnoughCoinsException;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.GameChair;

@UtilityClass
public class BetConfirmMenu {
    public void createAndOpen(@NotNull Player player, @NotNull GameChair chair) {
        Menu.newBuilder(TwonyOnePlugin.getInstance())
                .title(RendererHelper.legacy(player, LangKeys.MENU_BET_CONFIRM_TITLE))
                .size(54)
                .clickListener(ClickListener.cancel())
                .openListener(openEvent -> {
                    Menu menu = openEvent.getMenu();

                    menu.setButton(8, Button.simple(new ItemBuilder(Material.EMPTY_MAP).display(RendererHelper.legacy(player, LangKeys.MENU_GAME_HELP)).build()));

                    menu.setButton(22, Button.simple(new ItemBuilder(Material.PAPER).display(RendererHelper.legacy(player, LangKeys.MENU_BET_CONFIRM_INFO.setArgs(chair.getTable().getBet().getChair().getPlayer().getName(), chair.getTable().getBet().getAmount())).split("\n")).build()));
                    menu.setButton(29, Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 4).display(RendererHelper.legacy(player, LangKeys.MENU_BET_CONFIRM_CONFIRM.setArgs(chair.getTable().getBet().getChair().getPlayer().getName(), chair.getTable().getBet().getAmount())).split("\n")).build()).action(clickEvent -> {
                                try {
                                    player.closeInventory();

                                    if (chair.isOccupied()) {
                                        BukkitHelper.getUser(player).get().sendMessage(LangKeys.CHAIR_OCCUPIED);
                                        return;
                                    }

                                    if (chair.getTable().getBet() == null) {
                                        BukkitHelper.getUser(player).get().sendMessage(LangKeys.OPPONENT_LEAVED);
                                        return;
                                    }

                                    if (BukkitHelper.getUser(player).get().getDonateCoins() < chair.getTable().getBet().getAmount()) {
                                        throw new NotEnoughCoinsException(BukkitHelper.getUser(player).get(), chair.getTable().getBet().getAmount());
                                    }

                                    TwonyOnePlugin.getInstance().getGameManager().addToGame(player, chair);
                                } catch (NotEnoughCoinsException e) {
                                    e.tellAboutIt();
                                }
                            }
                    ));
                    menu.setButton(33, Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 8).display(RendererHelper.legacy(player, LangKeys.MENU_BET_CONFIRM_CANCEL).split("\n")).build()).action(clickEvent -> player.closeInventory()));

                    MenuHelper.fillBorder(menu);
                })
                .build()
                .open(player);
    }
}
