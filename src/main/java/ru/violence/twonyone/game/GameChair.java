package ru.violence.twonyone.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.coreapi.common.api.util.Check;
import ru.violence.twonyone.LangKeys;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.util.Holo;

import java.util.List;
import java.util.stream.Collectors;

public class GameChair {
    private final @NotNull GameTable table;
    private final @NotNull World world;
    private final @NotNull Location sitLocation;
    private final @NotNull List<Location> locations;

    private ArmorStand sitAssist;
    private GamePlayer gamePlayer;
    private Location lastPlayerLocation;

    private Holo scoreHolo1;
    private Holo scoreHolo2;
    private @Getter @Setter @Nullable Holo secretCardHolo;

    public GameChair(@NotNull GameTable table, @NotNull List<Location> locations) {
        this.table = table;
        this.sitLocation = locations.get(0);
        this.world = Check.notNull(sitLocation.getWorld());
        this.locations = locations;
    }

    boolean sit(@NotNull GamePlayer gamePlayer) {
        if (isOccupied()) return false;

        sitAssist = world.spawn(sitLocation.clone().add(0.5, 0.35, 0.5), ArmorStand.class, as -> {
            as.setSmall(true);
            as.setVisible(false);
            as.setBasePlate(false);
            as.setGravity(false);
            as.setMarker(true);
        });

        Player player = gamePlayer.getPlayer();

        this.gamePlayer = gamePlayer;
        this.lastPlayerLocation = player.getLocation();

        // Sit player
        player.teleport(sitLocation.clone().add(0.5, 0, 0.5));
        sitAssist.addPassenger(player);

        gamePlayer.reservedItems();
        gamePlayer.giveLeaveItem();

        return true;
    }

    boolean stand() {
        if (!isOccupied()) return false;

        Player player = gamePlayer.getPlayer();
        Location lastLocation = lastPlayerLocation;

        sitAssist.removePassenger(player);
        // Don't schedule on shutdown
        if (TwonyOnePlugin.getInstance().isEnabled()) {
            Bukkit.getScheduler().runTaskLater(TwonyOnePlugin.getInstance(), () -> {
                if (player.isOnline()) {
                    player.teleport(lastLocation);
                }
            }, 1);
        }

        player.closeInventory();
        gamePlayer.getBackItems();

        gamePlayer = null;
        lastPlayerLocation = null;

        sitAssist.remove();
        sitAssist = null;

        if (scoreHolo1 != null) {
            scoreHolo1.destroy();
            scoreHolo1 = null;
        }

        if (scoreHolo2 != null) {
            scoreHolo2.destroy();
            scoreHolo2 = null;
        }

        // Do not destroy because it will be destroyed on game reset
        secretCardHolo = null;

        return true;
    }

    void reset() {
        stand();
    }

    void updateScoreText(boolean revealClosed) {
        if (scoreHolo1 == null) {
            scoreHolo1 = new Holo(getTable().getScoreHoloLoc());
            scoreHolo1.setCanViewFilter(getPlayer().getUniqueId());
        }
        if (scoreHolo2 == null) {
            scoreHolo2 = new Holo(getTable().getScoreHoloLoc().add(0, Holo.LINE_OFFSET, 0));
            scoreHolo2.setCanViewFilter(getPlayer().getUniqueId());
        }

        scoreHolo1.setText(LangKeys.HOLO_SCORE_YOU.setArgs(getTable().getScore().getTotal(this), GameTable.WIN_POINTS));
        if (revealClosed) {
            scoreHolo2.setText(LangKeys.HOLO_SCORE_OPPONENT_REVEAL.setArgs(getTable().getScore().getTotal(getOpposite()), GameTable.WIN_POINTS));
        } else {
            scoreHolo2.setText(LangKeys.HOLO_SCORE_OPPONENT.setArgs(getTable().getScore().getPublicTotal(getOpposite()), GameTable.WIN_POINTS));
        }
    }

    public boolean isOccupied() {
        return gamePlayer != null;
    }

    public @NotNull GameChair getOpposite() {
        return table.getOppositeChair(this);
    }

    public boolean canSit(@NotNull Player player) {
        return !isOccupied() && sitLocation.clone().add(0.5, 0.5, 0.5).distance(player.getLocation()) < 4;
    }

    public @Nullable GamePlayer getGamePlayer() {
        return gamePlayer;
    }

    public @Nullable Player getPlayer() {
        return gamePlayer != null ? gamePlayer.getPlayer() : null;
    }

    public @NotNull GameTable getTable() {
        return table;
    }

    public @NotNull Block getSitBlock() {
        return world.getBlockAt(sitLocation);
    }

    public @NotNull List<Block> getBlocks() {
        return locations.stream().map(world::getBlockAt).collect(Collectors.toList());
    }

    public @NotNull ChairNumber getNumber() {
        return table.getChairOne().equals(this) ? ChairNumber.ONE : ChairNumber.TWO;
    }

    public int getTotalScore() {
        return table.getScore().getTotal(this);
    }
}
