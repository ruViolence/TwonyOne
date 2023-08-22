package ru.violence.twonyone.game.registry;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.twonyone.game.GamePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class GamePlayerRegistry {
    private final Map<UUID, GamePlayer> gamePlayers = new HashMap<>();

    public @Nullable GamePlayer put(@NotNull Player player, @NotNull GamePlayer gamePlayer) {
        return gamePlayers.put(player.getUniqueId(), gamePlayer);
    }

    public @Nullable GamePlayer remove(@NotNull Player player) {
        return gamePlayers.remove(player.getUniqueId());
    }

    public boolean contains(@NotNull Player player) {
        return gamePlayers.containsKey(player.getUniqueId());
    }

    public Optional<GamePlayer> getGamePlayer(@NotNull Player player) {
        return Optional.ofNullable(gamePlayers.get(player.getUniqueId()));
    }

    public void clear() {
        gamePlayers.clear();
    }
}
