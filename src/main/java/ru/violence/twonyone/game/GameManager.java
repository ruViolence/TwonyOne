package ru.violence.twonyone.game;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.registry.GamePlayerRegistry;
import ru.violence.twonyone.util.LocationUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class GameManager {
    private final TwonyOnePlugin plugin;
    private final List<GameTable> tables = new ArrayList<>();
    private final @Getter GamePlayerRegistry gamePlayerRegistry = new GamePlayerRegistry();

    public GameManager(TwonyOnePlugin plugin) {
        this.plugin = plugin;
        loadGames();
    }

    private void loadGames() {
        FileConfiguration config = plugin.getConfig();
        for (String tableKey : config.getConfigurationSection("games").getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection("games." + tableKey);

            BlockFace direction = BlockFace.valueOf(section.getString("direction"));
            List<Location> chairOneLocs = section.getStringList("chair-one").stream().map(LocationUtil::stringToLocation).collect(Collectors.toList());
            List<Location> chairTwoLocs = section.getStringList("chair-two").stream().map(LocationUtil::stringToLocation).collect(Collectors.toList());
            Location scoreHoloLoc = LocationUtil.stringToLocation(section.getString("score-hologram-location"));
            int broadcastNearbyRadius = section.getInt("broadcast-nearby-radius");

            tables.add(new GameTable(direction, chairOneLocs, chairTwoLocs, scoreHoloLoc, broadcastNearbyRadius));
        }
    }

    public boolean addToGame(@NotNull Player player, @NotNull GameChair chair) {
        return addToGame(player, chair, null);
    }

    public boolean addToGame(@NotNull Player player, @NotNull GameChair chair, @Nullable Bet bet) {
        if (isInGame(player)) return false;

        GamePlayer gamePlayer = new GamePlayer(player, chair);

        gamePlayerRegistry.put(player, gamePlayer);
        chair.getTable().addPlayer(gamePlayer, chair, bet);

        return true;
    }

    public boolean removeFromGame(Player player) {
        return removeFromGame(player, true);
    }

    boolean removeFromGame(Player player, boolean triggerReset) {
        GamePlayer gamePlayer = getGamePlayer(player).orElse(null);
        if (gamePlayer == null) return false;

        gamePlayerRegistry.remove(player);
        gamePlayer.getTable().removePlayer(player, triggerReset);
        return true;
    }

    public boolean isInGame(@NotNull Player player) {
        return gamePlayerRegistry.contains(player);
    }

    public Optional<GamePlayer> getGamePlayer(@NotNull Player player) {
        return gamePlayerRegistry.getGamePlayer(player);
    }

    public @NotNull List<GameTable> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public Optional<GameChair> getGameChair(@NotNull Block block) {
        return getTables()
                .stream()
                .map(table -> table.getChair(block))
                .filter(Objects::nonNull)
                .findFirst();
    }
}
