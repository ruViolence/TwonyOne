package ru.violence.twonyone.menu;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import ru.violence.coreapi.common.util.CommonUtil;
import ru.violence.coreapi.common.util.MathUtil;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.config.Config;
import ru.violence.twonyone.game.Bet;
import ru.violence.twonyone.game.GameChair;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BetAdjustMenu extends Menu {
    private static final Cache<UUID, Integer> MULTIPLIER_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();
    private final GameChair chair;
    private int amount;

    public BetAdjustMenu(@NotNull Player player, @NotNull GameChair chair) {
        super(player, MessageUtil.renderLegacy(player, LangKeys.MENU_BET_ADJUST_TITLE), 45);
        this.chair = chair;
        this.amount = CommonUtil.orElse(MULTIPLIER_CACHE.getIfPresent(player.getUniqueId()), Config.BET_DEFAULT);
    }

    @Override
    public void onInitialize() {
        setButton(8, Buttons.makeDummy(new ItemBuilder(Material.EMPTY_MAP).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_GAME_HELP))));

        setReduceButton(10, 25);
        setReduceButton(11, 15);
        setReduceButton(12, 5);

        // Info button
        setButton(13, Buttons.makeDummy(new ItemBuilder(Material.GOLD_INGOT).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_ADJUST_INFO.setArgs(amount)))));

        setIncreaseButton(14, 5);
        setIncreaseButton(15, 15);
        setIncreaseButton(16, 25);

        // Confirm button
        setButton(30, Buttons.makeSimple(new ItemBuilder(Material.WOOL, 1, (short) 4).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_ADJUST_CONFIRM.setArgs(amount))), (player, menu, button, clickType) -> {
            player.closeInventory();

            if (chair.getTable().getBet() != null) {
                BukkitHelper.getUser(player).sendMessage(LangKeys.TABLE_GOT_OCCUPIED);
                return;
            }

            try {
                if (getUser().getDonateCoins() < amount) {
                    throw new NotEnoughCoinsException(getUser(), amount);
                }

                MULTIPLIER_CACHE.put(player.getUniqueId(), amount);
                TwonyOnePlugin.getInstance().getGameManager().addToGame(player, chair, new Bet(chair, amount));
            } catch (NotEnoughCoinsException e) {
                e.tellAboutIt();
            }
        }));

        // Cancel button
        setButton(32, Buttons.makeSimple(new ItemBuilder(Material.WOOL, 1, (short) 8).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_ADJUST_CANCEL)), (player, menu, button, clickType) -> player.closeInventory()));

        MenuHelper.fillBorder(this);
    }

    private void setReduceButton(int slot, int amount) {
        if (!canChangeBet(-amount)) {
            setButton(slot, null);
            return;
        }
        setButton(slot, Buttons.makeSimple(new ItemBuilder(Material.WOOL, amount, (short) 14).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_ADJUST_REDUCE.setArgs(this.amount, amount))), (p, m, b, c) -> {
            this.amount = clampBet(this.amount + (-amount));
            onInitialize();
        }));
    }

    private void setIncreaseButton(int slot, int amount) {
        if (!canChangeBet(amount)) {
            setButton(slot, null);
            return;
        }
        setButton(slot, Buttons.makeSimple(new ItemBuilder(Material.WOOL, amount, (short) 5).setDescription(MessageUtil.renderLegacy(player, LangKeys.MENU_BET_ADJUST_INCREASE.setArgs(this.amount, amount))), (p, m, b, c) -> {
            this.amount = clampBet(this.amount + amount);
            onInitialize();
        }));
    }

    private boolean canChangeBet(int amount) {
        int calculated = this.amount + amount;
        return calculated == clampBet(calculated);
    }

    private @NotNull User getUser() {
        return Check.notNull(BukkitHelper.getUser(this.player), () -> "User of player " + this.player.getName() + " was not found");
    }

    private static int clampBet(int amount) {
        return MathUtil.clamp(amount, Config.BET_MIN, Config.BET_MAX);
    }
}
