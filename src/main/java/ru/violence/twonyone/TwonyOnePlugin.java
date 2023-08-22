package ru.violence.twonyone;

import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import ru.violence.twonyone.config.Config;
import ru.violence.twonyone.game.EndReason;
import ru.violence.twonyone.game.GameManager;
import ru.violence.twonyone.game.GameTable;
import ru.violence.twonyone.game.State;
import ru.violence.twonyone.listener.ChairClickListener;
import ru.violence.twonyone.listener.ChairSitPreventListener;
import ru.violence.twonyone.listener.DismountPreventListener;
import ru.violence.twonyone.listener.GameItemUseListener;
import ru.violence.twonyone.listener.InventoryMovePreventListener;
import ru.violence.twonyone.listener.QuitInGameListener;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class TwonyOnePlugin extends JavaPlugin {
    private static TwonyOnePlugin instance;
    private GameManager gameManager;

    public static TwonyOnePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        Config.load(this);

        this.gameManager = new GameManager(this);

        getServer().getPluginManager().registerEvents(new ChairClickListener(this), this);
        getServer().getPluginManager().registerEvents(new DismountPreventListener(this), this);
        getServer().getPluginManager().registerEvents(new GameItemUseListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryMovePreventListener(this), this);
        getServer().getPluginManager().registerEvents(new QuitInGameListener(this), this);

        if (getServer().getPluginManager().isPluginEnabled("BetterChairs")) {
            Set<Block> preventedBlocks = gameManager.getTables().stream()
                    .flatMap(table -> Arrays.stream(table.getChairs()))
                    .flatMap(chair -> chair.getBlocks().stream())
                    .collect(Collectors.toSet());
            if (!preventedBlocks.isEmpty()) {
                getServer().getPluginManager().registerEvents(new ChairSitPreventListener(preventedBlocks), this);
            }
        }
    }

    @Override
    public void onDisable() {
        for (GameTable table : gameManager.getTables()) {
            if (table.getState() == State.PLAYING) {
                table.stop(EndReason.PLUGIN_DISABLE, null);
            } else {
                table.reset(true);
            }
        }

        instance = null;
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
