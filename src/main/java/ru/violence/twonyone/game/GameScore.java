package ru.violence.twonyone.game;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.coreapi.common.util.Check;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameScore {
    private final @NotNull GameTable table;
    private final Map<GameChair, List<Card>> cardMap = new HashMap<>();

    public GameScore(@NotNull GameTable table) {
        this.table = table;
    }

    public @NotNull Card addRandomCard(@NotNull GameChair chair) {
        Check.notNull(chair);
        while (true) {
            Card randomCard = Card.getRandom();
            if (isRolled(randomCard)) continue;

            cardMap.computeIfAbsent(chair, c -> new ArrayList<>()).add(randomCard);
            return randomCard;
        }
    }

    public @NotNull List<Card> getCards(@NotNull GameChair chair) {
        Check.notNull(chair);
        return new ArrayList<>(cardMap.getOrDefault(chair, Collections.emptyList()));
    }

    public int getPublicTotal(@NotNull GameChair chair) {
        Check.notNull(chair);
        return cardMap.getOrDefault(chair, Collections.emptyList())
                .stream()
                .skip(1) // Skip the first secret card
                .mapToInt(Card::getPoints)
                .sum();
    }

    public int getTotal(@NotNull GameChair chair) {
        Check.notNull(chair);
        return cardMap.getOrDefault(chair, Collections.emptyList())
                .stream()
                .mapToInt(Card::getPoints)
                .sum();
    }

    public @Nullable Card getSecretCard(@NotNull GameChair chair) {
        List<Card> cards = cardMap.get(chair);
        return cards == null || cards.isEmpty() ? null : cards.get(0);
    }

    public @NotNull GameTable getTable() {
        return table;
    }

    private boolean isRolled(@NotNull Card card) {
        return cardMap.values()
                .stream()
                .flatMap(Collection::stream)
                .anyMatch(card::equals);
    }
}
