package ru.violence.twonyone.game;

import org.jetbrains.annotations.NotNull;
import ru.violence.twonyone.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum Card {
    C_1(1),
    C_2(2),
    C_3(3),
    C_4(4),
    C_5(5),
    C_6(6),
    C_7(7),
    C_8(8),
    C_9(9),
    C_10(10),
    C_11(11);

    private final static Card[] VALUES = values();
    private final int points;

    Card(int points) {
        this.points = points;
    }

    public static @NotNull Card getRandom() {
        List<Card> list = new ArrayList<>(Arrays.asList(VALUES));
        Collections.shuffle(list);
        return list.get(Utils.RANDOM.nextInt(list.size()));
    }

    public int getPoints() {
        return points;
    }
}
