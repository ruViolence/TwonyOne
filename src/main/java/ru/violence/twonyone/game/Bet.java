package ru.violence.twonyone.game;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.common.api.util.MathUtil;
import ru.violence.twonyone.config.Config;

public class Bet {
    private final @Getter @NotNull GameChair chair;
    private final int amount;

    public Bet(@NotNull GameChair chair, int amount) {
        this.chair = chair;
        this.amount = MathUtil.clamp(amount, Config.BET_MIN, Config.BET_MAX);
    }

    public int getAmount() {
        return amount;
    }
}
