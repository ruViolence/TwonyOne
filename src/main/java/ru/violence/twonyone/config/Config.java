package ru.violence.twonyone.config;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.file.FileConfiguration;
import ru.violence.twonyone.TwonyOnePlugin;

@UtilityClass
public class Config {
    public int BET_MIN;
    public int BET_MAX;
    public int BET_DEFAULT;
    public double BET_FEE;

    public void load(TwonyOnePlugin plugin) {
        FileConfiguration config = plugin.getConfig();

        BET_MIN = config.getInt("bet.min");
        BET_MAX = config.getInt("bet.max");
        BET_DEFAULT = config.getInt("bet.default");
        BET_FEE = config.getInt("bet.fee");
    }
}
