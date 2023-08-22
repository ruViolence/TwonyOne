package ru.violence.twonyone.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.BukkitHelper;
import ru.violence.coreapi.bukkit.api.menu.Menu;
import ru.violence.coreapi.bukkit.api.menu.MenuHelper;
import ru.violence.coreapi.bukkit.api.menu.button.Buttons;
import ru.violence.coreapi.bukkit.api.util.MessageUtil;
import ru.violence.coreapi.bukkit.util.ItemBuilder;
import ru.violence.coreapi.common.user.NotEnoughCoinsException;
import ru.violence.coreapi.common.user.User;
import ru.violence.coreapi.common.util.Check;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.GameChair;

public class BetConfirmMenu extends Menu {
    private final GameChair chair;

    public BetConfirmMenu(@NotNull Player player, @NotNull GameChair chair) {
        super(player, MessageUtil.renderLegacy(player, LangKeys.MENU_BET_CONFIRM_TITLE), 54);
        this.chair = chair;
    }

    @Override
    public void onInitialize() {
        setButton(8, Buttons.makeDummy(new ItemBuilder(Material.EMPTY_MAP).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_GAME_HELP))));

        setButton(22, Buttons.makeDummy(new ItemBuilder(Material.PAPER).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_CONFIRM_INFO.setArgs(this.chair.getTable().getBet().getChair().getPlayer().getName(), this.chair.getTable().getBet().getAmount())).split("\n"))));
        setButton(29, Buttons.makeSimple(
                new ItemBuilder(Material.WOOL, 1, (short) 4).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_CONFIRM_CONFIRM.setArgs(this.chair.getTable().getBet().getChair().getPlayer().getName(), this.chair.getTable().getBet().getAmount())).split("\n")),
                (player, menu, button, click) -> {
                    try {
                        player.closeInventory();

                        if (this.chair.isOccupied()) {
                            getUser().sendMessage(LangKeys.CHAIR_OCCUPIED);
                            return;
                        }

                        if (this.chair.getTable().getBet() == null) {
                            getUser().sendMessage(LangKeys.OPPONENT_LEAVED);
                            return;
                        }

                        if (getUser().getDonateCoins() < this.chair.getTable().getBet().getAmount()) {
                            throw new NotEnoughCoinsException(getUser(), this.chair.getTable().getBet().getAmount());
                        }

                        TwonyOnePlugin.getInstance().getGameManager().addToGame(player, chair);
                    } catch (NotEnoughCoinsException e) {
                        e.tellAboutIt();
                    }
                }
        ));
        setButton(33, Buttons.makeSimple(
                new ItemBuilder(Material.WOOL, 1, (short) 8).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_CONFIRM_CANCEL).split("\n")),
                (player, menu, button, click) -> player.closeInventory()
        ));

        MenuHelper.fillBorder(this);
    }

    private @NotNull User getUser() {
        return Check.notNull(BukkitHelper.getUser(this.player), () -> "User of player " + this.player.getName() + " was not found");
    }
}
