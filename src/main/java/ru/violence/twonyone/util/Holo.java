package ru.violence.twonyone.util;

import lombok.Data;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.violence.coreapi.common.api.message.MessageKey;
import ru.violence.coreapi.common.api.util.Check;
import ru.violence.xholo.api.ArmorStandDataBuilder;
import ru.violence.xholo.api.CustomName;
import ru.violence.xholo.api.VirtualArmorStand;
import ru.violence.xholo.api.XHolo;

import java.util.UUID;
import java.util.function.Predicate;

@Data
public class Holo {
    public static final double LINE_OFFSET = -0.25;

    private @NotNull Location location;
    private @Getter @Nullable VirtualArmorStand armorStand;
    private @Nullable Predicate<Player> filter;

    public Holo(@NotNull Location location) {
        this.location = location.clone();
    }

    public void setText(@NotNull String text) {
        setText(CustomName.text(text));
    }

    public void setText(@NotNull MessageKey key) {
        setText(CustomName.key(key));
    }

    public void setText(@NotNull CustomName name) {
        Check.notNull(name);

        if (isExists()) {
            armorStand.setData(armorStand.getData().modify().customName(name).build());
        } else {
            spawn(name);
        }
    }

    public void destroy() {
        if (isExists()) {
            armorStand.manager().unregister();
            armorStand = null;
        }
    }

    public void setLocation(@NotNull Location location) {
        this.location = location.clone();

        if (isExists()) {
            armorStand.setLocation(this.location);
        }
    }

    public void setCanViewFilter(@NotNull UUID playerUniqueId) {
        setCanViewFilter(player -> player.getUniqueId().equals(playerUniqueId));
    }

    public void setCanViewFilter(@Nullable Predicate<Player> filter) {
        this.filter = filter;
        if (isExists()) this.armorStand.manager().setCanSeeFilter(filter);
    }

    public boolean isExists() {
        return armorStand != null;
    }

    public @NotNull Location getLocation() {
        return location.clone();
    }

    private void spawn(@NotNull CustomName name) {
        destroy();
        createHologram();
        setText(name);
    }

    private void createHologram() {
        this.armorStand = XHolo.builder()
                .data(ArmorStandDataBuilder.builder()
                        .marker(true)
                        .visible(false)
                        .small(false)
                        .hasArms(false)
                        .hasBasePlate(false)
                        .build())
                .location(location)
                .build();
        this.armorStand.manager().setCanSeeFilter(filter);
        this.armorStand.manager().register();
    }
}
