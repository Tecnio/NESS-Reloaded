package com.github.ness;

import org.bukkit.plugin.java.JavaPlugin;

public class NessPlugin extends JavaPlugin {

    private NessAnticheat ness;

    @Override
    public synchronized void onEnable() {
        if (ness != null) {
            throw new IllegalStateException("Already enabled and running");
        }
        NessAnticheat ness = new NessAnticheat(this, getDataFolder().toPath());
        ness.start();
        getCommand("ness").setExecutor(new NessCommands(ness));
        this.ness = ness;
    }

    @Override
    public synchronized void onDisable() {
        if (ness == null) {
            getLogger().warning("No running instance of NESS. Did an error occur at startup?");
            return;
        }
        try {
            ness.close();
        } finally {
            ness = null;
        }
    }

}
