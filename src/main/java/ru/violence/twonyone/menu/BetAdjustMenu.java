package ru.violence.twonyone.menu;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import ru.violence.coreapi.common.api.util.MathUtil;
import ru.violence.coreapi.common.api.util.NullUtil;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.config.Config;
import ru.violence.twonyone.game.Bet;
import ru.violence.twonyone.game.GameChair;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@UtilityClass
public class BetAdjustMenu {
    private final Cache<UUID, Integer> MULTIPLIER_CACHE = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build();

    public void createAndOpen(@NotNull Player player, @NotNull GameChair chair) {
        createAndOpen(player, chair, NullUtil.orElse(MULTIPLIER_CACHE.getIfPresent(player.getUniqueId()), Config.BET_DEFAULT));
    }

    public void createAndOpen(@NotNull Player player, @NotNull GameChair chair, int amount) {
        Menu.newBuilder(TwonyOnePlugin.getInstance())
                .title(RendererHelper.legacy(player, LangKeys.MENU_BET_ADJUST_TITLE))
                .size(45)
                .clickListener(ClickListener.cancel())
                .openListener(openEvent -> {
                    Menu menu = openEvent.getMenu();

                    menu.setButton(8, Button.simple(new ItemBuilder(Material.EMPTY_MAP).display(RendererHelper.legacy(player, LangKeys.MENU_GAME_HELP)).build()));

                    setReduceButton(menu, player, chair, amount, 10, 25);
                    setReduceButton(menu, player, chair, amount, 11, 15);
                    setReduceButton(menu, player, chair, amount, 12, 5);

                    // Info button
                    menu.setButton(13, Button.simple(new ItemBuilder(Material.GOLD_INGOT).display(RendererHelper.legacy(player, LangKeys.MENU_BET_ADJUST_INFO.setArgs(amount))).build()));

                    setIncreaseButton(menu, player, chair, amount, 14, 5);
                    setIncreaseButton(menu, player, chair, amount, 15, 15);
                    setIncreaseButton(menu, player, chair, amount, 16, 25);

                    // Confirm button
                    menu.setButton(30, Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 4).display(RendererHelper.legacy(player, LangKeys.MENU_BET_ADJUST_CONFIRM.setArgs(amount))).build()).action(clickEvent -> {
                        player.closeInventory();

                        if (chair.getTable().getBet() != null) {
                            BukkitHelper.getUser(player).get().sendMessage(LangKeys.TABLE_GOT_OCCUPIED);
                            return;
                        }

                        try {
                            if (BukkitHelper.getUser(player).get().getDonateCoins() < amount) {
                                throw new NotEnoughCoinsException(BukkitHelper.getUser(player).get(), amount);
                            }

                            MULTIPLIER_CACHE.put(player.getUniqueId(), amount);
                            TwonyOnePlugin.getInstance().getGameManager().addToGame(player, chair, new Bet(chair, amount));
                        } catch (NotEnoughCoinsException e) {
                            e.tellAboutIt();
                        }
                    }));

                    // Cancel button
                    menu.setButton(32, Button.simple(new ItemBuilder(Material.WOOL, 1, (short) 8).display(RendererHelper.legacy(player, LangKeys.MENU_BET_ADJUST_CANCEL)).build()).action(clickEvent -> player.closeInventory()));

                    MenuHelper.fillBorder(menu);
                })
                .build()
                .open(player);
    }

    private void setReduceButton(@NotNull Menu menu, @NotNull Player player, @NotNull GameChair chair, int currentAmount, int slot, int amount) {
        if (!canChangeBet(currentAmount, -amount)) {
            menu.setButton(slot, (Button) null);
            return;
        }
        menu.setButton(slot, Button.simple(new ItemBuilder(Material.WOOL, amount, (short) 14).display(RendererHelper.legacy(player, LangKeys.MENU_BET_ADJUST_REDUCE.setArgs(currentAmount, amount))).build()).action(clickEvent -> {
            int newAmount = clampBet(currentAmount + (-amount));
            createAndOpen(player, chair, newAmount);
        }));
    }

    private void setIncreaseButton(@NotNull Menu menu, @NotNull Player player, @NotNull GameChair chair, int currentAmount, int slot, int amount) {
        if (!canChangeBet(currentAmount, amount)) {
            menu.setButton(slot, (Button) null);
            return;
        }
        menu.setButton(slot, Button.simple(new ItemBuilder(Material.WOOL, amount, (short) 5).display(RendererHelper.legacy(player, LangKeys.MENU_BET_ADJUST_INCREASE.setArgs(currentAmount, amount))).build()).action(clickEvent -> {
            int newAmount = clampBet(currentAmount + amount);
            createAndOpen(player, chair, newAmount);
        }));
    }

    private boolean canChangeBet(int currentAmount, int amount) {
        int calculated = currentAmount + amount;
        return calculated == clampBet(calculated);
    }

    private int clampBet(int amount) {
        return MathUtil.clamp(amount, Config.BET_MIN, Config.BET_MAX);
    }
}
