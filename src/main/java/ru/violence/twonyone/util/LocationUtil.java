package ru.violence.twonyone.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import ru.violence.coreapi.common.util.Check;

@UtilityClass
public class LocationUtil {
    public Location stringToLocation(String sLoc) {
        return stringToLocation(sLoc, ";", null);
    }

    public Location stringToLocation(String sLoc, World inputWorld) {
        return stringToLocation(sLoc, ";", inputWorld);
    }

    public Location stringToLocation(String sLoc, String splitter, World inputWorld) {
        Check.notEmpty(sLoc, "String is empty");
        Check.notEmpty(splitter, "Splitter is empty");
        String[] args = sLoc.split(splitter);

        World world = (inputWorld != null) ? inputWorld : Bukkit.getWorld(args[0]);
        Check.notNull(world, () -> "World \"" + args[0] + "\" not found");

        double x = Double.parseDouble(args[1]);
        double y = Double.parseDouble(args[2]);
        double z = Double.parseDouble(args[3]);
        float yaw = 0;
        float pitch = 0;

        if (args.length > 4) {
            yaw = Float.parseFloat(args[4]);
            pitch = Float.parseFloat(args[5]);
        }

        return new Location(world, x, y, z, yaw, pitch);
    }
}
