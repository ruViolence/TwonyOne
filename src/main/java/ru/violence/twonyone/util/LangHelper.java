package ru.violence.twonyone.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.violence.coreapi.bukkit.api.BukkitHelper;
import ru.violence.coreapi.common.message.LegacyPrinter;
import ru.violence.coreapi.common.message.MessageKey;
import ru.violence.coreapi.common.user.User;
import ru.violence.coreapi.common.util.Check;
import ru.violence.twonyone.TwonyOnePlugin;

@UtilityClass
public class LangHelper {
    public void sendTitle(@NotNull Player player, @NotNull MessageKey message) {
        User user = BukkitHelper.getUser(player);
        Check.notNull(user);

        try { // In case the message is set up incorrectly
            String[] split = LegacyPrinter.print(message.setLang(user.getLanguage())).split("\n", 5);
            String title = split[0];
            String subtitle = split[1];
            int fadeIn = Integer.parseInt(split[2]);
            int stay = Integer.parseInt(split[3]);
            int fadeOut = Integer.parseInt(split[4]);

            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            TwonyOnePlugin.getInstance().getLogger().warning("Title " + message.getKey() + " is set up incorrectly");
        }
    }

    public void sendMessage(@NotNull Player player, @NotNull MessageKey message) {
        BukkitHelper.getUser(player).sendMessage(message);
    }
}
