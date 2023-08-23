package ru.violence.twonyone.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.common.api.util.MathUtil;
import ru.violence.twonyone.TwonyOnePlugin;
import ru.violence.twonyone.game.Card;
import ru.violence.twonyone.game.ChairNumber;
import ru.violence.twonyone.game.GameChair;
import ru.violence.twonyone.game.GameTable;

import java.util.UUID;

@UtilityClass
public class CardHoloHelper {
    private final double NEXT_OFFSET = 0.3;
    private final double FROM_VIEWER_OFFSET = 0.8;
    private final double TO_SIDE_OFFSET = 1.25;
    private final double SPAWN_OFFSET = 2;

    public void addClosed(@NotNull GameChair chair, @NotNull Card card) {
        UUID viewerUUID = chair.getPlayer().getUniqueId();
        GameTable table = chair.getTable();
        int points = card.getPoints();

        Holo holo = new Holo(getSpawnLocation(chair));
        holo.setText(player -> player == null || !player.getUniqueId().equals(viewerUUID) ? "§9" + "?" : "§9" + points);

        table.addHolo(holo);
        chair.setSecretCardHolo(holo);

        startSlideAnimation(table, holo, getLocationForNext(chair));
    }

    public void addOpen(@NotNull GameChair chair, @NotNull Card card) {
        GameTable table = chair.getTable();

        Holo holo = new Holo(getSpawnLocation(chair));
        holo.setText(getNextCardColor(chair) + card.getPoints());
        table.addHolo(holo);

        startSlideAnimation(table, holo, getLocationForNext(chair));
    }

    public void revealSecretCard(GameTable table) {
        for (GameChair chair : table.getChairs()) {
            Holo holo = chair.getSecretCardHolo();
            if (holo == null) return;
            Card secretCard = chair.getTable().getScore().getSecretCard(chair);
            if (secretCard == null) return;
            holo.setText("§9" + secretCard.getPoints());
        }
    }

    private void startSlideAnimation(@NotNull GameTable table, @NotNull Holo holo, @NotNull Location targetLocation) {
        table.addTask(new BukkitRunnable() {
            @Override
            public void run() {
                holo.setLocation(targetLocation);
            }
        }.runTaskLater(TwonyOnePlugin.getInstance(), 1));
    }

    private @NotNull Location getLocationForNext(@NotNull GameChair chair) {
        boolean isOpposite = chair.getNumber() == ChairNumber.TWO;
        BlockFace tableFace = chair.getTable().getDirection();
        BlockFace tableFaceCCW = FaceHelper.getCCW(tableFace);
        BlockFace viewerFace = isOpposite ? tableFace.getOppositeFace() : tableFace;

        double nextOffset = NEXT_OFFSET * MathUtil.clamp(
                chair.getTable().getScore().getCards(chair).size() - 1,
                0, Integer.MAX_VALUE);
        BlockFace nextFace = FaceHelper.getCW(tableFace);

        Location location = chair.getSitBlock().getLocation();

        // Base push up
        location.add(0.5, 1, 0.5);

        // Move forward from viewer
        location.add(viewerFace.getModX() * FROM_VIEWER_OFFSET, 0, viewerFace.getModZ() * FROM_VIEWER_OFFSET);

        // Base move to side
        location.add(tableFaceCCW.getModX() * TO_SIDE_OFFSET, 0, tableFaceCCW.getModZ() * TO_SIDE_OFFSET);

        // Next card offset
        location.add(nextFace.getModX() * nextOffset, 0, nextFace.getModZ() * nextOffset);

        return location;
    }

    private @NotNull Location getSpawnLocation(@NotNull GameChair chair) {
        boolean isOpposite = chair.getNumber() == ChairNumber.TWO;
        BlockFace tableFace = chair.getTable().getDirection();
        BlockFace tableFaceCW = FaceHelper.getCW(tableFace);
        BlockFace viewerFace = isOpposite ? tableFace.getOppositeFace() : tableFace;

        Location location = chair.getSitBlock().getLocation();

        // Base push up
        location.add(0.5, 1, 0.5);

        // Move forward from viewer
        location.add(viewerFace.getModX() * FROM_VIEWER_OFFSET, 0, viewerFace.getModZ() * FROM_VIEWER_OFFSET);

        // Base move to side
        location.add(tableFaceCW.getModX() * SPAWN_OFFSET, 0, tableFaceCW.getModZ() * SPAWN_OFFSET);

        return location;
    }

    private @NotNull String getNextCardColor(@NotNull GameChair chair) {
        return chair.getTable().getScore().getCards(chair).size() % 2 == 1 ? "§6" : "§e";
    }
}
