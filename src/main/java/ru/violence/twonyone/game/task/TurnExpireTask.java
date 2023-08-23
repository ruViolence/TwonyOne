package ru.violence.twonyone.game.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.util.RendererHelper;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.game.GameChair;
import ru.violence.twonyone.game.State;

public class TurnExpireTask extends BukkitRunnable {
    private final @NotNull GameChair chair;
    private int seconds;

    public TurnExpireTask(@NotNull GameChair chair, int seconds) {
        this.chair = chair;
        this.seconds = seconds;
    }

    @Override
    public void run() {
        Player player = chair.getPlayer();
        if (player == null || chair.getTable().getState() != State.PLAYING) {
            cancel();
            return;
        }

        if (seconds <= 0) {
            chair.getTable().onTurnExpire();
            cancel();
            return;
        }

        player.sendActionBar(RendererHelper.legacy(player, LangKeys.GAME_TIME_LEFT_TO_THINK.setArgs(seconds)));

        seconds -= 1;
    }
}
