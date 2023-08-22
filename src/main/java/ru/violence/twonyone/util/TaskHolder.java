package ru.violence.twonyone.util;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public class TaskHolder {
    private @Nullable BukkitTask task;

    public TaskHolder() {}

    public TaskHolder(@Nullable BukkitTask task) {
        this.task = task;
    }

    public @Nullable BukkitTask getTask() {
        return this.task;
    }

    public void setTask(@Nullable BukkitTask task) {
        this.task = task;
    }

    public boolean cancel() {
        if (task == null) return false;
        task.cancel();
        this.task = null;
        return true;
    }
}
